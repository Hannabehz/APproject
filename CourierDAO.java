package DAO;

import entity.Courier;
import entity.Order;
import org.hibernate.Session;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;
import java.util.UUID;

public class CourierDAO {
    public Courier findCourierByToken(String token) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String subject = util.JwtUtil.validateToken(token);
            return session.get(Courier.class, java.util.UUID.fromString(subject));
        } catch (Exception e) {
            System.err.println("Error in findCourierByToken: " + e.getMessage());
            throw new RuntimeException("Failed to find courier: " + e.getMessage());
        }
    }
    public List<Order> findAvailableOrders(String courierRegion) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            String hql = "FROM Order o WHERE o.status = :status AND o.deliveryAddress LIKE :region";
            Query<Order> query = session.createQuery(hql, Order.class);
            query.setParameter("status", "Pending");
            query.setParameter("region", "%" + courierRegion + "%");
            List<Order> availableOrders = query.getResultList();
            session.getTransaction().commit();
            return availableOrders;
        } catch (Exception e) {
            System.err.println("Error in findAvailableOrders: " + e.getMessage());
            throw new RuntimeException("Failed to find available orders: " + e.getMessage());
        }
    }
    public Order findOrderById(String orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Order.class, UUID.fromString(orderId));
        } catch (Exception e) {
            System.err.println("Error in findOrderById: " + e.getMessage());
            throw new RuntimeException("Failed to find order: " + e.getMessage());
        }
    }

    public void saveOrder(Order order) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.merge(order); // استفاده از merge برای به‌روزرسانی
            session.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Error in saveOrder: " + e.getMessage());
            throw new RuntimeException("Failed to save order: " + e.getMessage());
        }
    }
}
