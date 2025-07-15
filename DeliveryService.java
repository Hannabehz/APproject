package service;

import DAO.CourierDAO;
import dto.OrderDTO;
import dto.OrderItemDTO;
import entity.Courier;
import entity.Order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeliveryService {
    private final CourierDAO courierDAO;
    public DeliveryService() {
        courierDAO = new CourierDAO();
    }
    public List<OrderDTO> getAvailableOrders(String token) {
        if (token == null || token.trim().isEmpty())
            throw new IllegalArgumentException("Token is required");
        // پیدا کردن پیک
        Courier courier = courierDAO.findCourierByToken(token);
        if (courier == null) {
            throw new RuntimeException("Courier not found");
        }

        // چک کردن وضعیت پیک
        if (!courier.getStatus().equals("Available")) {
            throw new RuntimeException("Courier is not available");
        }

        // دریافت سفارش‌های در دسترس بر اساس منطقه پیک
        List<Order> availableOrders = courierDAO.findAvailableOrders(courier.getWorkingRegion());

        // تبدیل Order به OrderDTO
        return availableOrders.stream().map(order -> new OrderDTO(
                order.getId(),
                order.getDeliveryAddress(),
                order.getRestaurant().getId(),
                order.getOrderItems().stream().map(item -> new OrderItemDTO(
                        item.getFood().getId(),
                        item.getQuantity()
                )).collect(Collectors.toList())
        )).collect(Collectors.toList());
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
        Courier courier = courierDAO.findCourierByToken(token);
        if (courier == null) {
            throw new RuntimeException("Courier not found");
        }

        // فرض می‌کنیم منطق دیتابیس توی DAO باشه، اینجا فقط به‌روزرسانی رو شبیه‌سازی می‌کنیم
        try {
            // پیدا کردن سفارش
            Order order = courierDAO.findOrderById(orderId);
            if (order == null) {
                throw new RuntimeException("not found");
            }

            // چک کردن وضعیت فعلی و قوانین تغییر
            String currentStatus = order.getStatus();
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
            Courier currentDeliveryMan = order.getDeliveryMan();
            if (!"Pending".equals(currentStatus) && !currentDeliveryMan.getId().equals(courier.getId())) { // فرض می‌کنیم assignedCourierId وجود داره
                throw new RuntimeException("assigned"); // قبلاً به پیک دیگه اختصاص داده شده
            }

            // به‌روزرسانی وضعیت
            order.setStatus(status);
            order.setDeliveryMan(courier); // جایگزین setAssignedCourierId
            courierDAO.saveOrder(order); // فرض می‌کنیم این متد توی DAO هست
            if(status.equals("delivered"))
               courier.setStatus("Available");
            else
                courier.setStatus("Busy");

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
        } catch (Exception e) {
            System.err.println("Error in updateDeliveryStatus: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
