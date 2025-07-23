package service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.CartItem;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CartService {
    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public CompletableFuture<Map<String, Object>> getCart(String token) {
        if (token == null || token.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Map.of(
                    "status", 400,
                    "message", "Token is required"
            ));
        }

        System.out.println("Sending token to /auth/cart: " + token);
        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/cart"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String body = response.body();
                    System.out.println("Raw response from /auth/cart: " + body);
                    Map<String, Object> result = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());
                    if (response.statusCode() == 200) {
                        Map<String, Object> cartResult = new HashMap<>();
                        cartResult.put("status", 200);
                        cartResult.put("cart", result);
                        return cartResult;
                    } else {
                        return Map.of(
                                "status", response.statusCode(),
                                "message", result != null ? result.getOrDefault("message", "Cart fetch failed") : "Cart fetch failed"
                        );
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Cart fetch error: " + throwable.getMessage());
                    return Map.of(
                            "status", 500,
                            "message", "Internal server error: " + throwable.getMessage()
                    );
                });
    }

    public CompletableFuture<List<model.CartItem>> getCartItems(String token) {
        if (token == null || token.trim().isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<model.CartItem>());
        }

        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cart"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String body = response.body();
                    System.out.println("Raw response from /cart: " + body);
                    if (response.statusCode() == 200) {
                        // تبدیل JSON به لیست model.CartItem
                        List<model.CartItem> cartItems = gson.fromJson(body, new TypeToken<List<model.CartItem>>(){}.getType());
                        return cartItems != null ? cartItems : new ArrayList<model.CartItem>();
                    } else {
                        return new ArrayList<model.CartItem>();
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Get cart items error: " + throwable.getMessage());
                    return new ArrayList<CartItem>();
                });
    }
    public CompletableFuture<Map<String, Object>> addToCart(String token, String itemId, int quantity) {
        if (token == null || token.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Map.of(
                    "status", 400,
                    "message", "Token is required"
            ));
        }
        if (itemId == null || itemId.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Map.of(
                    "status", 400,
                    "message", "Item ID is required"
            ));
        }
        if (quantity <= 0) {
            return CompletableFuture.completedFuture(Map.of(
                    "status", 400,
                    "message", "Quantity must be positive"
            ));
        }

        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("item_id", itemId);
        requestBody.put("quantity", quantity);

        String jsonBody = gson.toJson(requestBody);
        System.out.println("Request body to /auth/cart: " + jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/cart"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String body = response.body();
                    System.out.println("Raw response from /auth/cart: " + body);
                    Map<String, Object> result = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());
                    return Map.of(
                            "status", response.statusCode(),
                            "message", result != null ? result.getOrDefault("message", "Add to cart failed") : "Add to cart failed",
                            "data", result != null ? result : new HashMap<>()
                    );
                })
                .exceptionally(throwable -> {
                    System.err.println("Add to cart error: " + throwable.getMessage());
                    return Map.of(
                            "status", 500,
                            "message", "Internal server error: " + throwable.getMessage()
                    );
                });
    }

    public CompletableFuture<Map<String, Object>> removeFromCart(String token, String itemId) {
        if (token == null || token.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Map.of(
                    "status", 400,
                    "message", "Token is required"
            ));
        }
        if (itemId == null || itemId.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Map.of(
                    "status", 400,
                    "message", "Item ID is required"
            ));
        }

        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("item_id", itemId);

        String jsonBody = gson.toJson(requestBody);
        System.out.println("Request body to /auth/cart: " + jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/cart"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .method("PUT", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String body = response.body();
                    System.out.println("Raw response from /auth/cart: " + body);
                    Map<String, Object> result = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());
                    return Map.of(
                            "status", response.statusCode(),
                            "message", result != null ? result.getOrDefault("message", "Remove from cart failed") : "Remove from cart failed",
                            "data", result != null ? result : new HashMap<>()
                    );
                })
                .exceptionally(throwable -> {
                    System.err.println("Remove from cart error: " + throwable.getMessage());
                    return Map.of(
                            "status", 500,
                            "message", "Internal server error: " + throwable.getMessage()
                    );
                });
    }
    public CompletableFuture<Integer> getCartItemQuantity(String token, String itemId) {
        if (token == null || token.trim().isEmpty() || itemId == null || itemId.trim().isEmpty()) {
            return CompletableFuture.completedFuture(0);
        }

        return getCartItems(token).thenApply(items -> {
            return items.stream()
                    .filter(item -> item.getItemId().equals(itemId))
                    .mapToInt(CartItem::getQuantity)
                    .sum();
        });
    }
    public CompletableFuture<Map<String, Object>> topUpWallet(String token, String method, double amount, String bankName, String accountNumber) {
        if (token == null || token.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Map.of("status", 400, "message", "Token is required"));
        }
        if (method == null || !method.matches("online|card")) {
            return CompletableFuture.completedFuture(Map.of("status", 400, "message", "Invalid method"));
        }
        if (amount <= 0) {
            return CompletableFuture.completedFuture(Map.of("status", 400, "message", "Invalid amount"));
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("method", method);
        requestBody.put("amount", amount);
        if (bankName != null) requestBody.put("bankName", bankName);
        if (accountNumber != null) requestBody.put("accountNumber", accountNumber);

        String jsonBody = gson.toJson(requestBody);
        System.out.println("Request body to /wallet/top-up: " + jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/wallet/top-up"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String body = response.body();
                    System.out.println("Response from /wallet/top-up: " + body);
                    Map<String, Object> result = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());
                    return Map.of(
                            "status", response.statusCode(),
                            "message", result != null ? result.getOrDefault("message", "Top-up failed") : "Top-up failed",
                            "data", result != null ? result : new HashMap<>()
                    );
                })
                .exceptionally(throwable -> Map.of(
                        "status", 500,
                        "message", "Internal server error: " + throwable.getMessage()
                ));
    }

    public CompletableFuture<Map<String, Object>> makeOnlinePayment(String token, String orderId, String method, String bankName, String accountNumber) {
        if (token == null || token.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Map.of("status", 400, "message", "Token is required"));
        }
        if (orderId == null || orderId.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Map.of("status", 400, "message", "Order ID is required"));
        }
        if (method == null || !method.matches("wallet|paywall")) {
            return CompletableFuture.completedFuture(Map.of("status", 400, "message", "Invalid method"));
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", orderId);
        requestBody.put("method", method);
        if (bankName != null) requestBody.put("bankName", bankName);
        if (accountNumber != null) requestBody.put("accountNumber", accountNumber);

        String jsonBody = gson.toJson(requestBody);
        System.out.println("Request body to /payment/online: " + jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/payment/online"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String body = response.body();
                    System.out.println("Response from /payment/online: " + body);
                    Map<String, Object> result = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());
                    return Map.of(
                            "status", response.statusCode(),
                            "message", result != null ? result.getOrDefault("message", "Payment failed") : "Payment failed",
                            "data", result != null ? result : new HashMap<>()
                    );
                })
                .exceptionally(throwable -> Map.of(
                        "status", 500,
                        "message", "Internal server error: " + throwable.getMessage()
                ));
    }
    public CompletableFuture<Map<String, Object>> submitOrder(String token, Map<String, Object> requestBody) {
        if (token == null || token.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Map.of(
                    "status", 400,
                    "error", "Token is required"
            ));
        }

        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        String jsonBody = gson.toJson(requestBody);
        System.out.println("Request body to /orders: " + jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/orders"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String body = response.body();
                    System.out.println("Raw response from /orders: " + body);
                    if (response.statusCode() != 200) {
                        Map<String, Object> result = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());
                        return Map.of(
                                "status", response.statusCode(),
                                "error", result != null ? result.getOrDefault("error", "Submit order failed") : "Submit order failed",
                                "data", new HashMap<>()
                        );
                    }
                    Map<String, Object> result = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());
                    return Map.of(
                            "status", response.statusCode(),
                            "message", result != null ? result.getOrDefault("message", "Order submitted successfully") : "Order submitted successfully",
                            "data", result != null ? result : new HashMap<>()
                    );
                })
                .exceptionally(throwable -> {
                    System.err.println("Submit order error: " + throwable.getMessage());
                    return Map.of(
                            "status", 500,
                            "error", "Internal server error: " + throwable.getMessage(),
                            "data", new HashMap<>()
                    );
                });
    }
}