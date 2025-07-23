package httpRequestHandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.WalletService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;


public class WalletHttpHandler implements HttpHandler {
    private final WalletService walletService = new WalletService();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            switch (path) {
                case "/wallet/top-up":
                    if ("POST".equalsIgnoreCase(method)) {
                        handleTopUpWallet(exchange);
                    } else {
                        sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                    }
                    break;
                case "/payment/online":
                    if ("POST".equalsIgnoreCase(method)) {
                        handleOnlinePayment(exchange);
                    } else {
                        sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                    }
                    break;
                    case "/wallet/balance":
                        if ("GET".equalsIgnoreCase(method)) {
                            handelGetBalance(exchange);
                        }
                        break;
                default:
                    sendResponse(exchange, 404, "{\"error\": \"Not found\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handleTopUpWallet(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
            return;
        }

        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        JsonObject json = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
        String method = json.has("method") ? json.get("method").getAsString() : null;
        double amount = json.has("amount") ? json.get("amount").getAsDouble() : 0.0;
        String bankName = json.has("bankName") ? json.get("bankName").getAsString() : null;
        String accountNumber = json.has("accountNumber") ? json.get("accountNumber").getAsString() : null;

        Map<String, Object> response = walletService.topUpWallet(authHeader.substring(7), method, amount, bankName, accountNumber);
        sendResponse(exchange, response.containsKey("error") ? (int) response.get("status") : 200, gson.toJson(response));
    }

    private void handleOnlinePayment(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
            return;
        }

        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        JsonObject json = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
        String orderId = json.has("orderId") ? json.get("orderId").getAsString() : null;
        String method = json.has("method") ? json.get("method").getAsString() : null;
        String bankName = json.has("bankName") ? json.get("bankName").getAsString() : null;
        String accountNumber = json.has("accountNumber") ? json.get("accountNumber").getAsString() : null;

        Map<String, Object> response = walletService.makeOnlinePayment(authHeader.substring(7), orderId, method, bankName, accountNumber);
        sendResponse(exchange, response.containsKey("error") ? (int) response.get("status") : 200, gson.toJson(response));
    }

    private boolean isAuthenticated(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    private void handelGetBalance(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
        }
        String token=authHeader.substring(7);
        Map<String,Object> response=walletService.getWalletBalance(token);
        sendResponse(exchange,200,gson.toJson(response));
    }
}