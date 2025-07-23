package service;

import DAO.UserDAO;
import entity.Food;
import entity.OrderItem;
import entity.ShoppingCart;
import entity.User;

import DAO.ShoppingCartDAO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import util.HibernateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShoppingCartService {
    private final ShoppingCartDAO cartDAO;
    private final UserDAO userDAO;

    public ShoppingCartService(ShoppingCartDAO cartDAO, UserDAO userDAO, SessionFactory sessionFactory) {
        this.cartDAO = cartDAO;
        this.userDAO = userDAO;
    }

    public Map<String, Object> addToCart(String token, Map<String, Object> itemDTO) {
        String itemIdStr = (String) itemDTO.get("item_id");
        Integer quantity = (Integer) itemDTO.get("quantity");
        if (itemIdStr == null || itemIdStr.trim().isEmpty()) {
            return new HashMap<>(Map.of("error", "Invalid `item_id`", "status", 400));
        }
        if (quantity == null || quantity <= 0) {
            return new HashMap<>(Map.of("error", "Invalid `quantity`", "status", 400));
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            // پیدا کردن کاربر
            User user = userDAO.findUserByToken(token);
            if (user == null) {
                return new HashMap<>(Map.of("error", "Unauthorized", "status", 401));
            }

            // پیدا کردن یا ایجاد سبد خرید
            Optional<ShoppingCart> cartOptional = cartDAO.findByUserId(user.getId());
            ShoppingCart cart = cartOptional.orElseGet(() -> {
                ShoppingCart newCart = new ShoppingCart();
                newCart.setUser(user);
                return newCart;
            });

            // پیدا کردن غذا
            UUID itemId;
            try {
                itemId = UUID.fromString(itemIdStr);
            } catch (IllegalArgumentException e) {
                return new HashMap<>(Map.of("error", "Invalid `item_id` format", "status", 400));
            }

            Food food = session.get(Food.class, itemId);
            if (food == null) {
                return new HashMap<>(Map.of("error", "Invalid `item_id`", "status", 400));
            }

            // بررسی موجودی
            int currentQuantity = cart.getOrderItems().stream()
                    .filter(item -> item.getFood().getId().equals(itemId))
                    .mapToInt(OrderItem::getQuantity)
                    .sum();
            if (currentQuantity + quantity > food.getSupply()) {
                return new HashMap<>(Map.of("error", "Requested quantity exceeds available supply for item: " + food.getName(), "status", 400));
            }

            // بررسی وجود آیتم در سبد خرید
            Optional<OrderItem> existingItem = cart.getOrderItems().stream()
                    .filter(item -> item.getFood().getId().equals(itemId))
                    .findFirst();

            if (existingItem.isPresent()) {
                // افزایش تعداد
                OrderItem orderItem = existingItem.get();
                orderItem.setQuantity(orderItem.getQuantity() + quantity);
            } else {
                // اضافه کردن آیتم جدید
                OrderItem orderItem = new OrderItem();
                orderItem.setFood(food);
                orderItem.setQuantity(quantity);
                orderItem.setUnitPrice(food.getPrice());
                orderItem.setRestaurant(food.getRestaurant());
                cart.getOrderItems().add(orderItem);
            }

            // آپدیت totalPrice
            int totalPrice = cart.getOrderItems().stream()
                    .mapToInt(item -> item.getQuantity() * item.getUnitPrice())
                    .sum();
            cart.setTotalPrice(totalPrice);

            // ذخیره سبد خرید
            cartDAO.saveOrUpdate(cart);

            session.getTransaction().commit();

            // آماده‌سازی پاسخ
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Item added to cart successfully");
            response.put("cart_id", cart.getId().toString());
            response.put("total_price", cart.getTotalPrice());
            response.put("status", 200);
            return response;

        } catch (Exception e) {
            return new HashMap<>(Map.of("error", e.getMessage(), "status", 500));
        }
    }

    public Map<String, Object> removeFromCart(String token, Map<String, Object> itemDTO) {
        String itemIdStr = (String) itemDTO.get("item_id");
        if (itemIdStr == null || itemIdStr.trim().isEmpty()) {
            return new HashMap<>(Map.of("error", "Invalid `item_id`", "status", 400));
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            // پیدا کردن کاربر
            User user = userDAO.findUserByToken(token);
            if (user == null) {
                return new HashMap<>(Map.of("error", "Unauthorized", "status", 401));
            }

            // پیدا کردن سبد خرید
            Optional<ShoppingCart> cartOptional = cartDAO.findByUserId(user.getId());
            if (cartOptional.isEmpty()) {
                return new HashMap<>(Map.of("error", "Cart not found", "status", 400));
            }
            ShoppingCart cart = cartOptional.get();

            // پیدا کردن UUID
            UUID itemId;
            try {
                itemId = UUID.fromString(itemIdStr);
            } catch (IllegalArgumentException e) {
                return new HashMap<>(Map.of("error", "Invalid `item_id` format", "status", 400));
            }

            // پیدا کردن آیتم
            Optional<OrderItem> itemOptional = cart.getOrderItems().stream()
                    .filter(item -> item.getFood().getId().equals(itemId))
                    .findFirst();

            if (itemOptional.isEmpty()) {
                return new HashMap<>(Map.of("error", "Item not found in cart", "status", 400));
            }

            OrderItem orderItem = itemOptional.get();
            if (orderItem.getQuantity() > 1) {
                // کاهش تعداد
                orderItem.setQuantity(orderItem.getQuantity() - 1);
            } else {
                // حذف آیتم
                cart.getOrderItems().remove(orderItem);
            }

            // آپدیت totalPrice
            int totalPrice = cart.getOrderItems().stream()
                    .mapToInt(item -> item.getQuantity() * item.getUnitPrice())
                    .sum();
            cart.setTotalPrice(totalPrice);

            // ذخیره سبد خرید
            cartDAO.saveOrUpdate(cart);

            session.getTransaction().commit();

            // آماده‌سازی پاسخ
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Item removed from cart successfully");
            response.put("cart_id", cart.getId().toString());
            response.put("total_price", cart.getTotalPrice());
            response.put("status", 200);
            return response;

        } catch (Exception e) {
            return new HashMap<>(Map.of("error", e.getMessage(), "status", 500));
        }
    }
    public int getCartItemQuantity(UUID userId, UUID itemId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            ShoppingCart cart = session.createQuery("FROM ShoppingCart sc JOIN FETCH sc.orderItems WHERE sc.user.id = :userId", ShoppingCart.class)
                    .setParameter("userId", userId)
                    .uniqueResult();
            if (cart == null) {
                return 0;
            }
            return cart.getOrderItems().stream()
                    .filter(item -> item.getFood().getId().equals(itemId))
                    .mapToInt(item -> item.getQuantity())
                    .findFirst()
                    .orElse(0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get cart item quantity: " + e.getMessage());
        }
    }

        public ShoppingCartService(UserDAO userDAO, ShoppingCartDAO cartDAO) {
            this.userDAO = userDAO;
            this.cartDAO = cartDAO;
        }

        public Map<String, Object> getCart(String token) {
            try {
                // پیدا کردن کاربر
                User user = userDAO.findUserByToken(token);
                if (user == null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("error", "Unauthorized");
                    response.put("status", 401);
                    return response;
                }

                // پیدا کردن سبد خرید
                Optional<ShoppingCart> cartOptional = cartDAO.findByUserId(user.getId());
                if (cartOptional.isEmpty()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Cart is empty");
                    response.put("cart_id", null); // مقدار null مجاز است
                    response.put("items", List.of());
                    response.put("total_price", 0.0);
                    response.put("status", 200);
                    return response;
                }

                ShoppingCart cart = cartOptional.get();
                List<Map<String, Object>> items = cart.getOrderItems().stream()
                        .map(item -> {
                            Map<String, Object> itemMap = new HashMap<>();
                            if (item.getFood() != null) {
                                itemMap.put("name", item.getFood().getName());
                                itemMap.put("imageBase64", item.getFood().getImageBase64());
                                itemMap.put("description", item.getFood().getDescription());
                                itemMap.put("price", item.getFood().getPrice());
                                itemMap.put("supply", item.getFood().getSupply());
                                itemMap.put("categories", item.getFood().getCategories());
                                itemMap.put("quantity", item.getQuantity());
                            }
                            return itemMap;
                        })
                        .collect(Collectors.toList());

                // آماده‌سازی پاسخ
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Cart retrieved successfully");
                response.put("cart_id", cart.getId() != null ? cart.getId().toString() : null);
                response.put("items", items);
                response.put("total_price", cart.getTotalPrice());
                response.put("status", 200);
                return response;

            } catch (Exception e) {
                e.printStackTrace();
                Map<String, Object> response = new HashMap<>();
                response.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error");
                response.put("status", 500);
                return response;
            }
        }
    }