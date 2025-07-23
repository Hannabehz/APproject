package httpRequestHandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.FoodDTO;
import dto.MenuResponseDTO;
import dto.RestaurantDTO;
import service.RestaurantService;
import util.JwtUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class VendorHttpHandler implements HttpHandler {
    private final RestaurantService restaurantService = new RestaurantService();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String[] pathParts = path.split("/");

        System.out.println("Handling request: " + method + " " + path);

        if (pathParts.length == 2 && pathParts[1].equals("vendors") && "POST".equals(method)) {
            handleGetRestaurantsForBuyer(exchange);
        } else if (pathParts.length == 4 && pathParts[1].equals("vendors") && pathParts[3].equals("items") && "GET".equals(method)) {
            handleGetMenusAndItems(exchange, pathParts[2]);
        }
        else if(path.equals("/items") && "POST".equals(method)) {
            handleSearchItems(exchange);
        }
        else {
            sendResponse(exchange, 405, "{\"error\": \"Method not allowed: " + method + " for path " + path + "\"}");
        }
    }
        private void handleGetRestaurantsForBuyer(HttpExchange exchange) throws IOException {
        // اعتبارسنجی توکن
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendResponse(exchange, 401, "{\"error\": \"Missing or invalid Authorization header\"}");
            return;
        }
            String token = authHeader.replace("Bearer ", "");
        try {
            JwtUtil.validateToken(token); // اعتبارسنجی توکن
        } catch (Exception e) {
            sendResponse(exchange, 401, "{\"error\": \"Invalid token: " + e.getMessage() + "\"}");
            return;
        }

        // خواندن بدنه درخواست
        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            String requestBody = br.lines().collect(Collectors.joining());
            Map<String, Object> request = gson.fromJson(requestBody, new TypeToken<Map<String, Object>>(){}.getType());

            String search = request != null && request.containsKey("search") ? (String) request.get("search") : null;
            List<String> categories = request != null && request.containsKey("categories") ?
                    (List<String>) request.get("categories") : null;

            // دریافت رستوران‌ها
            List<RestaurantDTO> restaurants = restaurantService.getRestaurants(search, categories);

            // ارسال پاسخ
            String response = gson.toJson(restaurants);
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Server error: " + e.getMessage() + "\"}");
        }
    }
    private void handleGetMenusAndItems(HttpExchange exchange, String restaurantIdStr) throws IOException {
        // اعتبارسنجی توکن
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendResponse(exchange, 401, "{\"error\": \"Missing or invalid Authorization header\"}");
            return;
        }

        String token = authHeader.replace("Bearer ", "");
        try {
            JwtUtil.validateToken(token);
        } catch (Exception e) {
            sendResponse(exchange, 401, "{\"error\": \"Invalid token: " + e.getMessage() + "\"}");
            return;
        }

        // تبدیل restaurantId به UUID
        UUID restaurantId;
        try {
            restaurantId = UUID.fromString(restaurantIdStr);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid restaurant ID format\"}");
            return;
        }

        // دریافت منوها و آیتم‌ها
        try {
            MenuResponseDTO menuResponse = restaurantService.getMenusAndItems(restaurantId);
            String response = gson.toJson(menuResponse);
            sendResponse(exchange, 200, response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Restaurant not found")) {
                sendResponse(exchange, 404, "{\"error\": \"Vendor not found\"}");
            } else {
                System.err.println("Error in handleGetMenusAndItems: " + e.getMessage());
                sendResponse(exchange, 500, "{\"error\": \"Server error: " + e.getMessage() + "\"}");
            }
        }
    }
    public void handleSearchItems(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            // خواندن توکن از هدر
            String token = exchange.getRequestHeaders().getFirst("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                sendResponse(exchange, 401, "Unauthorized");
                return;
            }

            // خواندن بدنه درخواست
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

            // استخراج پارامترها
            String search = json.has("search") ? json.get("search").getAsString() : null;
            List<String> categories = new ArrayList<>();
            if (json.has("categories")) {
                json.getAsJsonArray("categories").forEach(e -> categories.add(e.getAsString()));
            }
            UUID restaurantId = UUID.fromString(json.get("restaurantId").getAsString());

            // احراز هویت و اعتبارسنجی
            String userId = JwtUtil.validateToken(token.substring(7));
            if (userId == null) {
                sendResponse(exchange, 401, "Unauthorized");
                return;
            }

            // جستجوی غذاها
            List<FoodDTO> foods = restaurantService.searchFoods(restaurantId, search, categories);

            // ارسال پاسخ
            Gson gson = new Gson();
            String response = gson.toJson(foods);
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            System.err.println("Error in handleSearchItems: " + e.getMessage());
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
