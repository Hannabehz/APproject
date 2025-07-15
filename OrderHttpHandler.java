package httpRequestHandler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.*;
import io.jsonwebtoken.JwtException;
import service.OrderService;
import util.JwtUtil;
import service.RatingService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;

public class OrderHttpHandler implements HttpHandler {

    private final OrderService orderService;
    private final Gson gson = new Gson();
    private RatingService ratingService;
    public OrderHttpHandler() {
        this.orderService = new OrderService();
        this.ratingService = new RatingService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("/orders".equals(path) && "POST".equals(method)) {
            handleSubmitOrder(exchange);
        }
        else if ("/orders/history".equals(path) && "GET".equals(method)) {
            handleOrderHistory(exchange);
        }
     else if (path.startsWith("/favorites/") && "PUT".equals(method)) {
        handleAddFavorite(exchange);
    } else if (path.startsWith("/favorites/") && "DELETE".equals(method)) {
        handleRemoveFavorite(exchange);
    }
     else if(path.startsWith("/favorites/") && "GET".equals(method)) {
         handleGetFavorites(exchange);
     }
     else if(path.equals("/ratings")&& "POST".equals(method)) {
         handleRating(exchange);
        }
        else {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        }
    }

    private void handleOrderHistory(HttpExchange exchange) throws IOException {
        // Extract Authorization header
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
            return;
        }

        // Extract query parameters
        URI uri = exchange.getRequestURI();
        String query = uri.getQuery();
        String search = null;
        String vendor = null;
        if (query != null) {
            Map<String, String> queryParams = parseQuery(query);
            search = queryParams.get("search");
            vendor = queryParams.get("vendor");
        }

        // Extract buyerId from token
        String buyerIdStr;
        try {
            buyerIdStr = JwtUtil.validateToken(authHeader.substring(7));
        } catch (JwtException e) {
            sendResponse(exchange, 401, "{\"error\": \"Invalid or expired token\"}");
            return;
        }
        UUID buyerId = UUID.fromString(buyerIdStr);

        // Fetch order history
        try {
            List<OrderResponseDTO> orderHistory = orderService.getOrderHistory(buyerId, search, vendor);
            String response = gson.toJson(orderHistory);
            sendResponse(exchange, 200, response);
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }

    }
    private void handleSubmitOrder(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        JsonObject json = new JsonParser().parse(requestBody.toString()).getAsJsonObject();
        String deliveryAddress = json.get("delivery_address").getAsString();
        Long vendorId = json.get("vendor_id").getAsLong();
        JsonArray itemsArray = json.getAsJsonArray("items");

        List<OrderItemDTO> items = new ArrayList<>();
        for (JsonElement itemElement : itemsArray) {
            JsonObject item = itemElement.getAsJsonObject();
            UUID itemId = UUID.fromString(item.get("item_id").getAsString());
            int quantity = item.get("quantity").getAsInt();
            items.add(new OrderItemDTO(itemId, quantity));
        }

        try {
            OrderDTO order = orderService.submitOrder(authHeader.substring(7), deliveryAddress, vendorId, items);
            String response = gson.toJson(order);
            sendResponse(exchange, 200, response);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "Invalid order");
        } catch (RuntimeException e) {
            sendResponse(exchange, 400, e.getMessage());
        }
    }
    private void handleAddFavorite(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange,401,"{\"error\": \"Unauthorized\"}");
            return;
        }
        // Extract restaurantId from path
        String path = exchange.getRequestURI().getPath();
        String restaurantIdStr = path.substring(path.lastIndexOf('/') + 1);
        UUID restaurantId;
        try {
            restaurantId = UUID.fromString(restaurantIdStr);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"Restaurant not found\"}");
            return;
        }
        // Call service to add favorite
        try {
            String message = orderService.addFavorite(authHeader.substring(7), restaurantId);
            sendResponse(exchange, 200, "{\"message\": \"" + message + "\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (RuntimeException e) {
            sendResponse(exchange, 404, "{\"error\": \"Restaurant not found\"}");
        }
    }
    private void handleRemoveFavorite(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401,"{\"error\": \"Unauthorized\"}");
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String restaurantIdStr = path.substring(path.lastIndexOf('/') + 1);
        UUID restaurantId;
        try{
            restaurantId = UUID.fromString(restaurantIdStr);
        }
        catch(IllegalArgumentException e){
            sendResponse(exchange, 400, "{\"error\": \"Restaurant not found\"}");
            return;
        }
        try{
            String message=orderService.removeFavorite(authHeader.substring(7),restaurantId);
            sendResponse(exchange, 200, "{\"message\": \"" + message + "\"}");
        }
        catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        }   catch (RuntimeException e) {
        sendResponse(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    private void handleGetFavorites(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
            return;
        }

        try {
            List<RestaurantDTO> favorites = orderService.getFavorites(authHeader.substring(7));
            String response = gson.toJson(favorites);
            sendResponse(exchange, 200, response);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (RuntimeException e) {
            sendResponse(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    private void handleRating(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (!isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
        }
        StringBuilder requestBody =new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line = reader.readLine();
            while (line != null) {
                requestBody.append(line);
            }
        }
        JsonObject json = new JsonParser().parse(requestBody.toString()).getAsJsonObject();
        String orderId = json.get("orderId").getAsString();
        int rating = json.get("rating").getAsInt();
        String comment = json.get("comment").getAsString();
        String imageBase64 = json.has("imageBase64") ? json.get("imageBase64").getAsString() : null;

        // اعتبارسنجی اولیه
        if (orderId == null || orderId.trim().isEmpty() || rating < 1 || rating > 5 || comment == null || comment.trim().isEmpty()) {
            sendResponse(exchange, 400, "Invalid input");
            return;
        }

        try {
            ratingService.submitRating(authHeader.substring(7), orderId, rating, comment, imageBase64);
            sendResponse(exchange, 200, "Rating submitted");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "Invalid input");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                sendResponse(exchange, 404, "Order not found");
            } else {
                sendResponse(exchange, 400, e.getMessage());
            }
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
    private Map<String, String> parseQuery(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return queryParams;
    }
}
