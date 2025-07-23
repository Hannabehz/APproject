package service;

import DAO.CourierDAO;
import dto.OrderDTO;
import dto.OrderItemDTO;
import dto.OrderResponseDTO;
import entity.Order;
import entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeliveryService {
    private final CourierDAO courierDAO;
    public DeliveryService() {
        courierDAO = new CourierDAO();
    }
    public List<Order> getAvailableOrders(String token) {
        if (token == null || token.trim().isEmpty())
            throw new IllegalArgumentException("Token is required");
        // پیدا کردن پیک
        User courier = courierDAO.findCourierByToken(token);
        if (courier == null) {
            throw new RuntimeException("Courier not found");
        }

        // چک کردن وضعیت پیک
        if (!courier.getStatus().equals("Available")) {
            throw new RuntimeException("Courier is not available");
        }

        // دریافت سفارش‌های در دسترس بر اساس منطقه پیک
        List<Order> availableOrders = courierDAO.findAvailableOrders();

        // تبدیل Order به OrderDTO
        return availableOrders;

    }
    public Map<String, Object> updateDeliveryStatus(String token, String orderId, String status) {
        // اعتبارسنجی اولیه
        if (orderId == null || orderId.trim().isEmpty() || !orderId.matches("[0-9a-fA-F\\-]{36}")) {
            throw new IllegalArgumentException("Invalid order ID");
        }
        if (status == null || !status.matches("accepted|received|delivered")) {
            throw new IllegalArgumentException("Invalid status value");
        }

        // پیدا کردن پیک
        User courier = courierDAO.findCourierByToken(token);
        if (courier == null) {
            throw new RuntimeException("Courier not found");
        }

        // پیدا کردن سفارش
        Order order = courierDAO.findOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("not found");
        }

        // چک کردن وضعیت فعلی و قوانین تغییر
        String currentStatus = order.getDeliveryStatus();
        if ("delivered".equals(currentStatus)) {
            throw new RuntimeException("invalid"); // نمی‌شه وضعیت تحویل‌شده رو تغییر داد
        }
        if ("accepted".equals(currentStatus) && !"received".equals(status) && !"delivered".equals(status)) {
            throw new RuntimeException("invalid"); // فقط می‌تونه به received یا delivered بره
        }
        if ("received".equals(currentStatus) && !"delivered".equals(status)) {
            throw new RuntimeException("invalid"); // فقط می‌تونه به delivered بره
        }

        // چک کردن اختصاص به پیک دیگه
        UUID currentCourierId = order.getCourierId();
        if (currentStatus!=null && (currentCourierId == null || !currentCourierId.equals(courier.getId()))) {
            throw new RuntimeException("assigned"); // قبلاً به پیک دیگه اختصاص داده شده
        }

        // به‌روزرسانی وضعیت
        order.setStatus(status);
        order.setCourierId(courier.getId()); // Changed from setDeliveryMan to setCourierId
        courierDAO.saveOrder(order);
        if (status.equals("delivered")) {
            courier.setStatus("Available");
        } else {
            courier.setStatus("Busy");
        }
        courierDAO.saveCourier(courier); // فرض می‌کنیم متد saveCourier وجود داره

        // آماده‌سازی پاسخ
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Changed status successfully");
        response.put("order", new OrderDTO(
                order.getId(),
                order.getDeliveryAddress(),
                order.getRestaurant().getId(),
                order.getOrderItems().stream().map(item -> new OrderItemDTO(
                        item.getFood().getId(),
                        item.getQuantity()
                )).collect(Collectors.toList())
        ));
        return response;
    }
    public List<OrderResponseDTO> getDeliveryHistory(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }

        User courier = courierDAO.findCourierByToken(token);
        List<Order> orders = courierDAO.findDeliveryHistory(courier.getId());

        return orders.stream()
                .map(order -> {
                    OrderResponseDTO dto = new OrderResponseDTO();
                    dto.setOrderId(order.getId());
                    dto.setBuyerId(order.getCustomer().getId());
                    dto.setVendorId(order.getRestaurant().getId());
                    dto.setRestaurantName(order.getRestaurant().getName());
                    dto.setDeliveryAddress(order.getDeliveryAddress());
                    dto.setStatus(order.getStatus());
                    dto.setPayPrice(order.getPayPrice());
                    dto.setCreatedAt(order.getCreatedAt());
                    dto.setItems(order.getOrderItems().stream()
                            .map(item -> new OrderItemDTO(item.getFood().getId(), item.getQuantity()))
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
