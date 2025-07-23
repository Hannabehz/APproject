package httpRequestHandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.OrderDTO;
import dto.OrderResponseDTO;
import entity.Order;
import io.jsonwebtoken.JwtException;
import service.DeliveryService;
import util.JwtUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryHttpHandler implements HttpHandler {
    private final DeliveryService deliveryService;
    private final Gson gson = new Gson();
    public DeliveryHttpHandler() {
        deliveryService = new DeliveryService();
    }
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if(method.equals("GET")&&path.equals("/delivery/available")) {
            handleGetAvailableDeliveries(exchange);
        }
        if (path.matches("/deliveries/[^/]+") && "PATCH".equals(method)) {
            handlePatchDeliveryStatus(exchange);
        }
        if ("/deliveries/history".equals(path) && "GET".equals(method)) {
            handleDeliveryHistory(exchange);
        }
        else {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        }
    }
    private void handleGetAvailableDeliveries(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\", \"status\": 401}");
            return;
        }
        try {
            String token = authHeader.substring(7); // حذف "Bearer "
            List<Order> availableOrders = deliveryService.getAvailableOrders(token);
            String response = gson.toJson(availableOrders);
            sendResponse(exchange, 200, response);
        } catch (RuntimeException e) {
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\", \"status\": 400}");
        }
    }
    private void handlePatchDeliveryStatus(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "Unauthorized");
        }
        String orderId = exchange.getRequestURI().getPath().replaceFirst("/deliveries/", "");
        if (orderId == null || orderId.trim().isEmpty() || !orderId.matches("[0-9a-fA-F\\-]{36}")) {
            sendResponse(exchange, 400, "Invalid delivery ID");
            return;
        }
        StringBuilder requestBody =new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line = reader.readLine();
            while (line != null) {
                requestBody.append(line);
            }
        }
        JsonObject json = new JsonParser().parse(requestBody.toString()).getAsJsonObject();
        String status = json.get("status").getAsString();
        try {
            var result = deliveryService.updateDeliveryStatus(authHeader.substring(7), orderId, status);
            String response = gson.toJson(result);
            sendResponse(exchange, 200, response);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, e.getMessage());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                sendResponse(exchange, 404, "Order not found");
            } else if (e.getMessage().contains("invalid")) {
                sendResponse(exchange, 403, "Order status change is not valid");
            } else if (e.getMessage().contains("assigned")) {
                sendResponse(exchange, 409, "Delivery already assigned");
            } else {
                sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        }
    }
    private void handleDeliveryHistory(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
            return;
        }

        try {
            List<OrderResponseDTO> history = deliveryService.getDeliveryHistory(authHeader.substring(7));
            String response = gson.toJson(history);
            sendResponse(exchange, 200, response);
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    private boolean isAuthenticated(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Auth header is invalid");
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
