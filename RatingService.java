package service;

import DAO.RatingDAO;


public class RatingService {
    private final RatingDAO ratingDAO = new RatingDAO();

    public void submitRating(String token, String orderId, int rating, String comment, String imageBase64) {
        // اعتبارسنجی ورودی‌ها
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be null or empty");
        }

        // واگذاری به DAO
        ratingDAO.submitRating(token, orderId, rating, comment, imageBase64);
    }
}
