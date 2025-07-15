package service;

import DAO.OrderDAO;
import dto.*;
import io.jsonwebtoken.JwtException;
import org.hibernate.Session;
import util.HibernateUtil;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import entity.Food;
import entity.Order;
import entity.OrderItem;
import entity.Restaurant;
import entity.User;
import util.JwtUtil;


public class OrderService {

    private final OrderDAO orderDAO = new OrderDAO();

    public OrderDTO submitOrder(String token, String deliveryAddress, Long vendorId, List<OrderItemDTO> items) {
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty() || vendorId == null || items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Invalid order data");
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();


            User buyer = orderDAO.findUserByToken(token);
            if (buyer == null) {
                throw new RuntimeException("User not found");
            }


            Restaurant restaurant = session.get(Restaurant.class, java.util.UUID.fromString(vendorId.toString()));
            if (restaurant == null) {
                throw new RuntimeException("Vendor not found");
            }

            // ایجاد سفارش
            Order order = new Order();
            order.setDeliveryAddress(deliveryAddress);
            order.setCustomer(buyer);
            order.setRestaurant(restaurant);

            // تبدیل آیتم‌ها و اعتبارسنجی
            List<OrderItem> orderItems = items.stream().map(item -> {
                Food food = session.get(Food.class, item.getItemId());
                if (food == null || !food.getRestaurant().getId().equals(restaurant.getId())) {
                    throw new RuntimeException("Invalid item or item not from vendor");
                }
                OrderItem orderItem = new OrderItem();
                orderItem.setQuantity(item.getQuantity());
                orderItem.setFood(food);
                orderItem.setOrder(order);
                return orderItem;
            }).collect(Collectors.toList());

            order.setOrderItems(orderItems);

            // ذخیره سفارش
            Order savedOrder = orderDAO.saveOrder(order);
            session.getTransaction().commit();

            return new OrderDTO(
                    savedOrder.getId(),
                    savedOrder.getDeliveryAddress(),
                    savedOrder.getRestaurant().getId(),
                    items
            );
        } catch (Exception e) {
            System.err.println("Error in submitOrder: " + e.getMessage());
            throw new RuntimeException("Failed to submit order: " + e.getMessage());
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
            responseDTO.setVendorId(order.getVendorId());
            responseDTO.setDeliveryAddress(order.getDeliveryAddress());
            responseDTO.setStatus(order.getStatus());
            responseDTO.setItems(order.getOrderItems().stream()
                    .map(item -> {
                        OrderItemDTO itemDTO = new OrderItemDTO();
                        itemDTO.setItemId(item.getId());
                        itemDTO.setQuantity(item.getQuantity());
                        return itemDTO;
                    })
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


