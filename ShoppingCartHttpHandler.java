package httpRequestHandler;

import DAO.UserDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.JwtException;
import DAO.ShoppingCartDAO;
import service.ShoppingCartService;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import util.JwtUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ShoppingCartHttpHandler implements HttpHandler {
    private final ShoppingCartService cartService;
    private final UserDAO userdao;
    private final Gson gson = new Gson();

    public ShoppingCartHttpHandler() {
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        this.userdao = new UserDAO();
        this.cartService = new ShoppingCartService(new ShoppingCartDAO(sessionFactory), userdao, sessionFactory);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("/auth/cart".equals(path) && "POST".equals(method)) {
            handleAddToCart(exchange);
        } else if ("/auth/cart".equals(path) && "GET".equals(method)) {
            handleGetCart(exchange);
        } else if ("/auth/cart".equals(path) && "PUT".equals(method)) {
            handleRemoveFromCart(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Not found:(\"}");
        }
    }

    private void handleAddToCart(HttpExchange exchange) throws IOException {
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
        Map<String, Object> itemDTO = new HashMap<>();
        if (json.has("item_id")) {
            itemDTO.put("item_id", json.get("item_id").getAsString());
        }
        if (json.has("quantity")) {
            itemDTO.put("quantity", json.get("quantity").getAsInt());
        }

        try {
            Map<String, Object> response = cartService.addToCart(authHeader.substring(7), itemDTO);
            sendResponse(exchange, response.containsKey("error") ? (int) response.get("status") : 200, gson.toJson(response));
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handleRemoveFromCart(HttpExchange exchange) throws IOException {
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
        Map<String, Object> itemDTO = new HashMap<>();
        if (json.has("item_id")) {
            itemDTO.put("item_id", json.get("item_id").getAsString());
        }

        try {
            Map<String, Object> response = cartService.removeFromCart(authHeader.substring(7), itemDTO);
            sendResponse(exchange, response.containsKey("error") ? (int) response.get("status") : 200, gson.toJson(response));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handleGetCart(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized:>\"}");
            return;
        }

        try {
            Map<String, Object> response = cartService.getCart(authHeader.substring(7));
            sendResponse(exchange, response.containsKey("error") ? (int) response.get("status") : 200, gson.toJson(response));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private boolean isAuthenticated(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        try {
            JwtUtil.validateToken(authHeader.substring(7));
            return true;
        } catch (JwtException e) {
            System.err.println("Authentication failed: " + e.getMessage());
            return false;
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
        exchange.close();
    }
}
