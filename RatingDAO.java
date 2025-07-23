package DAO;

import entity.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.UUID;

public class RatingDAO {

    public void saveRating(Rating rating) {
        System.out.println("Saving rating: " + rating.getId());
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(rating);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Error in saveRating: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save rating: " + e.getMessage());
        }
    }
    public void submitRating(String token, String orderId, int rating, String comment, String imageBase64) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            // پیدا کردن کاربر از توکن
            User user = findUserByToken(token);
            if (user == null) {
                throw new RuntimeException("User not found for token");
            }

            // پیدا کردن سفارش
            Order order = findOrderById(orderId);
            if (order == null) {
                throw new RuntimeException("Order not found for ID: " + orderId);
            }

            // ایجاد و ذخیره نظرسنجی
            Rating ratingEntity = new Rating();
            ratingEntity.setId(UUID.randomUUID());
            ratingEntity.setRating(rating);
            ratingEntity.setComment(comment);
            ratingEntity.setImageBase64(imageBase64);
            ratingEntity.setUser(user);
            ratingEntity.setOrder(order);
            session.persist(ratingEntity);

            // به‌روزرسانی فیلد rate برای تمام غذاهای مرتبط با سفارش
            for (OrderItem orderItem : order.getOrderItems()) {
                Food food = orderItem.getFood();
                if (food != null) {
                    // محاسبه میانگین امتیاز برای غذا
                    Double averageRating = getAverageRatingForFood(food.getId());
                    food.setRate(averageRating != null ? averageRating : rating);
                    session.update(food);
                }
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Error in submitRating: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to submit rating: " + e.getMessage(), e);
        }
    }

    public User findUserByToken(String token) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String subject = util.JwtUtil.validateToken(token);
            return session.get(User.class, java.util.UUID.fromString(subject));
        } catch (Exception e) {
            System.err.println("Error in findUserByToken: " + e.getMessage());
            throw new RuntimeException("Failed to find user: " + e.getMessage());
        }
    }

    public Order findOrderById(String orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Order.class, java.util.UUID.fromString(orderId));
        } catch (Exception e) {
            System.err.println("Error in findOrderById: " + e.getMessage());
            throw new RuntimeException("Failed to find order: " + e.getMessage());
        }
    }

    public Double getAverageRatingForFood(UUID foodId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT AVG(r.rating) FROM Rating r JOIN r.order o JOIN o.items i WHERE i.food.id = :foodId";
            Query<Double> query = session.createQuery(hql, Double.class);
            query.setParameter("foodId", foodId);
            Double average = query.uniqueResult();
            return average != null ? average : 0.0;
        } catch (Exception e) {
            System.err.println("Error in getAverageRatingForFood: " + e.getMessage());
            throw new RuntimeException("Failed to get average rating: " + e.getMessage());
        }
    }
}
