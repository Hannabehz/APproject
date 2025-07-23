package service;

import DAO.OrderDAO;
import DAO.ShoppingCartDAO;
import DAO.UserDAO;
import dto.*;
import entity.*;
import io.jsonwebtoken.JwtException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import util.HibernateUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import util.JwtUtil;


public class OrderService {

    private final OrderDAO orderDAO;
    private final ShoppingCartDAO cartDAO;
    private final UserDAO userdao;
    private final SessionFactory sessionFactory;

    public OrderService(OrderDAO orderDAO, ShoppingCartDAO cartDAO, UserDAO userdao, SessionFactory sessionFactory) {
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.userdao = userdao;
        this.sessionFactory = sessionFactory;
    }

    public Map<String, Object> submitOrder(String token, String deliveryAddress, String vendorId, List<OrderItemDTO> items) {
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty() || vendorId == null || vendorId.trim().isEmpty() || items == null || items.isEmpty()) {
            return new HashMap<>(Map.of("error", "Invalid `delivery_address`, `vendor_id`, or `items`", "status", 400));
        }

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // پیدا کردن کاربر
            User buyer = userdao.findUserByToken(token);
            if (buyer == null) {
                return new HashMap<>(Map.of("error", "Unauthorized", "status", 401));
            }

            // پیدا کردن رستوران
            UUID restaurantUuid;
            try {
                restaurantUuid = UUID.fromString(vendorId);
            } catch (IllegalArgumentException e) {
                return new HashMap<>(Map.of("error", "Invalid `vendor_id` format", "status", 400));
            }

            Restaurant restaurant = session.get(Restaurant.class, restaurantUuid);
            if (restaurant == null) {
                return new HashMap<>(Map.of("error", "Restaurant not found", "status", 400));
            }

            // اعتبارسنجی آیتم‌ها و بررسی موجودی
            List<OrderItem> orderItems = items.stream().map(itemDTO -> {
                UUID itemId;
                try {
                    itemId = itemDTO.getItemId();
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid `item_id` format");
                }
                Food food = session.get(Food.class, itemId);
                if (food == null || !food.getRestaurant().getId().equals(restaurant.getId())) {
                    throw new RuntimeException("Invalid item or item not from restaurant");
                }
                if (itemDTO.getQuantity() > food.getSupply()) {
                    throw new RuntimeException("Requested quantity for item " + food.getName() + " exceeds available supply: " + food.getSupply());
                }
                OrderItem orderItem = new OrderItem();
                orderItem.setQuantity(itemDTO.getQuantity());
                orderItem.setOrderedItemQuantity(itemDTO.getQuantity());
                orderItem.setUnitPrice((int) food.getPrice());
                orderItem.setFood(food);
                orderItem.setRestaurant(restaurant);
                // کسر موجودی
                food.setSupply(food.getSupply() - itemDTO.getQuantity());
                session.update(food);
                return orderItem;
            }).collect(Collectors.toList());

            // محاسبه قیمت‌ها
            int rawPrice = orderItems.stream()
                    .mapToInt(item -> item.getQuantity() * item.getUnitPrice())
                    .sum();
            int taxFee = (int) (rawPrice * 0.1); // فرض: 10% مالیات
            int courierFee = 5000; // فرض: هزینه ثابت پیک
            int payPrice = rawPrice + taxFee + courierFee;

            // ایجاد سفارش
            Order order = new Order();
            order.setDeliveryAddress(deliveryAddress);
            order.setCustomer(buyer);
            order.setRestaurant(restaurant);
            order.setOrderItems(orderItems);
            order.setRawPrice(rawPrice);
            order.setTaxFee(taxFee);
            order.setCourierFee(courierFee);
            order.setPayPrice(payPrice);
            order.setCourierId(null);
            order.setStatus("submitted");
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            // ذخیره سفارش
            Order savedOrder = orderDAO.saveOrder(order);

            // خالی کردن سبد خرید
            Optional<ShoppingCart> cartOptional = cartDAO.findByUserId(buyer.getId());
            if (cartOptional.isPresent()) {
                ShoppingCart cart = cartOptional.get();
                cart.getOrderItems().clear();
                cart.setTotalPrice(0);
                cartDAO.saveOrUpdate(cart);
            }

            session.getTransaction().commit();

            // آماده‌سازی پاسخ
            List<String> itemIds = orderItems.stream()
                    .map(item -> item.getFood().getId().toString())
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedOrder.getId().toString());
            response.put("delivery_address", savedOrder.getDeliveryAddress());
            response.put("customer_id", savedOrder.getCustomer().getId().toString());
            response.put("vendor_id", savedOrder.getRestaurant().getId().toString());
            response.put("item_ids", itemIds);
            response.put("raw_price", savedOrder.getRawPrice());
            response.put("tax_fee", savedOrder.getTaxFee());
            response.put("courier_fee", savedOrder.getCourierFee());
            response.put("pay_price", savedOrder.getPayPrice());
            response.put("courier_id", savedOrder.getCourierId() != null ? savedOrder.getCourierId().toString() : null);
            response.put("status", savedOrder.getStatus());
            response.put("created_at", savedOrder.getCreatedAt().toString());
            response.put("updated_at", savedOrder.getUpdatedAt().toString());

            return response;

        } catch (Exception e) {
            return new HashMap<>(Map.of("error", e.getMessage(), "status", 500));
        }
    }
    public List<OrderResponseDTO> getOrderHistory(UUID buyerId, String search, String vendor) {
        // Fetch orders
        List<Order> orders = orderDAO.getOrderHistory(buyerId, search, vendor);

        // Map to DTO
        return orders.stream().map(order -> {
            OrderResponseDTO responseDTO = new OrderResponseDTO();
            responseDTO.setOrderId(order.getId());
            responseDTO.setBuyerId(order.getCustomer().getId());
            responseDTO.setVendorId(order.getRestaurant().getId());
            responseDTO.setRestaurantName(order.getRestaurant().getName()); // اضافه کردن نام رستوران
            responseDTO.setDeliveryAddress(order.getDeliveryAddress());
            responseDTO.setStatus(order.getStatus());
            responseDTO.setDeliveryStatus(order.getDeliveryStatus());
            responseDTO.setPayPrice(order.getPayPrice()); // فرض بر این است که Order فیلد payPrice دارد
            responseDTO.setCreatedAt(order.getCreatedAt()); // فرض بر این است که Order فیلد createdAt دارد
            responseDTO.setItems(order.getOrderItems().stream()
                    .map(item -> new OrderItemDTO(item.getFood().getId(), item.getQuantity()))
                    .collect(Collectors.toList()));
            return responseDTO;
        }).collect(Collectors.toList());
    }
    public String addFavorite(String token, UUID restaurantId) {
        if(token==null||token.trim().isEmpty())
            throw new IllegalArgumentException("Token is required");
        else if(restaurantId==null)
            throw new IllegalArgumentException("Restaurant id is required");
        UUID userId;
        try {
            userId = UUID.fromString(JwtUtil.validateToken(token));
        } catch (JwtException e) {
            throw new RuntimeException("Invalid or expired token");
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                throw new RuntimeException("Restaurant not found");
            }
            User user = session.get(User.class, userId);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            if (user.getFavorites().stream().anyMatch(r -> r.getId().equals(restaurantId))) {
                throw new IllegalArgumentException("Restaurant is already a favorite");
            }

            orderDAO.addFavorite(userId, restaurantId);
            return "Restaurant added to favorites";
        }
    }
    public List<RestaurantDTO> getFavorites(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }

        UUID userId;
        try {
            userId = UUID.fromString(JwtUtil.validateToken(token));
        } catch (JwtException e) {
            throw new RuntimeException("Invalid or expired token");
        }

        try {
            List<Restaurant> favorites = orderDAO.getFavorites(userId);
            return favorites.stream()
                    .map(restaurant -> new RestaurantDTO(restaurant.getName(),restaurant.getAddress(), restaurant.getPhone()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch favorites: " + e.getMessage());
        }
    }
    public String removeFavorite(String token, UUID restaurantId) {
        if(token==null||token.trim().isEmpty())
            throw new IllegalArgumentException("Token is required");
        if(restaurantId==null)
            throw new IllegalArgumentException("Restaurant id is required");
        UUID userId;
        try{
            userId = UUID.fromString(JwtUtil.validateToken(token));
        }
        catch(JwtException e){
            throw new RuntimeException("Invalid or expired token");
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                throw new RuntimeException("Restaurant not found");
            }
            User user = session.get(User.class, userId);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            if (!user.getFavorites().stream().anyMatch(r -> r.getId().equals(restaurantId))) {
                throw new IllegalArgumentException("Restaurant is not in favorites");
            }

            orderDAO.removeFavorite(userId, restaurantId);
            return "Restaurant removed from favorites";
        }
    }
}


