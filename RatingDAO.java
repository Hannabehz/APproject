package DAO;

import entity.Order;
import entity.Rating;
import entity.User;
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
