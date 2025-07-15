package service;

import DAO.OrderDAO;
import DAO.RatingDAO;
import entity.Order;
import entity.Rating;
import entity.User;

public class RatingService {
    private final RatingDAO ratingDAO = new RatingDAO();

    public void submitRating(String token, String orderId, int rating, String comment, String imageBase64) {
        // اعتبارسنجی اولیه
        if (orderId == null || orderId.trim().isEmpty() || rating < 1 || rating > 5 || comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid input: rating must be between 1 and 5, and orderId/comment cannot be empty");
        }

        // پیدا کردن کاربر و سفارش
        User user = ratingDAO.findUserByToken(token);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Order order = ratingDAO.findOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }

        // ایجاد و تنظیم Rating
        Rating ratingEntity = new Rating();
        ratingEntity.setRating(rating);
        ratingEntity.setComment(comment);
        ratingEntity.setImageBase64(imageBase64);
        ratingEntity.setOrder(order);
        ratingEntity.setUser(user);

        // ذخیره Rating
        ratingDAO.saveRating(ratingEntity);
    }

    public Double getAverageRatingForFood(java.util.UUID foodId) {
        return ratingDAO.getAverageRatingForFood(foodId);
    }
}
