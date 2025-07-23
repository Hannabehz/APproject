package httpRequestHandler;

import DAO.UserDAO;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.*;
import service.UserService;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserHttpHandler implements HttpHandler {
    private final UserService userService;
    private final Gson gson;

    public UserHttpHandler() {
        this.userService = new UserService(new UserDAO());
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if (path.equals("/auth/register") && "POST".equals(method)) {
            handleSignUp(exchange);
        } else if (path.equals("/auth/login") && "POST".equals(method)) {
            handleLogin(exchange);
        } else if (path.equals("/auth/profile") && "GET".equals(method)) {
            handleGetProfile(exchange);
        } else if (path.equals("/auth/profile") && "PUT".equals(method)) {
            handleUpdateProfile(exchange);
        }
        else if (path.equals("/auth/logout") && "GET".equals(method)) {
            handleLogout(exchange);
        }
            else {
            sendErrorResponse(exchange, method.equals("POST") || method.equals("GET") || method.equals("PUT") ? 404 : 405,
                    method.equals("POST") || method.equals("GET") || method.equals("PUT") ? "Not Found" : "Method Not Allowed");
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
                return;
            }

            ServiceResult result = userService.logout(authHeader);
            exchange.getResponseHeaders().set("Content-Type", "application/json");

            String response = gson.toJson(new MessageResponse(result.getMessage()));
            sendResponse(exchange, result.getStatus(), response);
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
            try {
                StringBuilder requestBody = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        requestBody.append(line);
                    }
                }

                LoginRequest loginRequest;
                try {
                    loginRequest = gson.fromJson(requestBody.toString(), LoginRequest.class);
                } catch (com.google.gson.JsonSyntaxException e) {
                    sendErrorResponse(exchange, 400, "Invalid input: Malformed JSON");
                    return;
                }

                ServiceResult result = userService.login(loginRequest.getPhone(), loginRequest.getPassword());
                exchange.getResponseHeaders().set("Content-Type", "application/json");

                if (result instanceof LoginResult) {
                    LoginResult success = (LoginResult) result;
                    String response = gson.toJson(new LoginResponse(
                            success.getMessage(), success.getToken(), success.getUser()));
                    sendResponse(exchange, success.getStatus(), response);
                } else {
                    String response = gson.toJson(new ErrorResponse(result.getMessage()));
                    sendResponse(exchange, result.getStatus(), response);
                }
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
    }
    private static class MessageResponse {
        public String message;

        public MessageResponse(String message) {
            this.message = message;
        }
    }
    private static class LoginResponse {
        public String message;
        public String token;
        public UserDTO user;

        public LoginResponse(String message, String token, UserDTO user) {
            this.message = message;
            this.token = token;
            this.user = user;
        }
    }

    private void handleSignUp(HttpExchange exchange) throws IOException {
        try {
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            UserDTO userDTO;
            try {
                userDTO = gson.fromJson(requestBody.toString(), UserDTO.class);
            } catch (com.google.gson.JsonSyntaxException e) {
                sendErrorResponse(exchange, 400, "Invalid input: Malformed JSON");
                return;
            }

            if (userDTO == null) {
                sendErrorResponse(exchange, 400, "Invalid input: Request body is empty");
                return;
            }
            ServiceResult result = userService.save(userDTO);
            exchange.getResponseHeaders().set("Content-Type", "application/json");

            if (result instanceof RegistrationResult) {
                RegistrationResult success = (RegistrationResult) result;
                String response = gson.toJson(new SuccessResponse(
                        success.getMessage(),
                        success.getUserId(),
                        success.getToken()
                ));
                sendResponse(exchange, success.getStatus(), response);
            } else {
                String response = gson.toJson(new ErrorResponse(result.getMessage()));
                sendResponse(exchange, result.getStatus(), response);
            }

        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
     void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
    void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        String response = gson.toJson(new ErrorResponse(errorMessage));
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, statusCode, response);
    }
    static class SuccessResponse {
        public String message;
        public String user_id; // Match OpenAPI field name
        public String token;

        public SuccessResponse(String message, String userId, String token) {
            this.message = message;
            this.user_id = userId;
            this.token = token;
        }
    }

     static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }

private static class LoginRequest {
    private String phone;
    private String password;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
    private void handleGetProfile(HttpExchange exchange) throws IOException {
        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
                return;
            }

            String token = authHeader.substring(7); // حذف "Bearer "
            ServiceResult result = userService.getProfile(token);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            String response;
            if (result instanceof ProfileResult) {
                ProfileResult success = (ProfileResult) result;
                response = gson.toJson(success.getUser());
            } else {
                // مطمئن شو که پیام سفارشی برگرده
                response = gson.toJson(Map.of(
                        "status", result.getStatus(),
                        "message", result.getMessage()
                ));
            }
            sendResponse(exchange, result.getStatus(), response);
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleUpdateProfile(HttpExchange exchange) throws IOException {
        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(exchange, 401, "Unauthorized: Missing or invalid token");
                return;
            }

            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            UserDTO userDTO;
            try {
                userDTO = gson.fromJson(requestBody.toString(), UserDTO.class);
            } catch (com.google.gson.JsonSyntaxException e) {
                sendErrorResponse(exchange, 400, "Invalid input: Malformed JSON");
                return;
            }

            if (userDTO == null) {
                sendErrorResponse(exchange, 400, "Invalid input: Request body is empty");
                return;
            }

            ServiceResult result = userService.updateProfile(authHeader, userDTO);
            exchange.getResponseHeaders().set("Content-Type", "application/json");

            String response = gson.toJson(new MessageResponse(result.getMessage()));
            sendResponse(exchange, result.getStatus(), response);
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
}