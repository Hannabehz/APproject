package DAO;

import entity.Order;
import entity.User;
import org.hibernate.Session;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;
import java.util.UUID;

public class CourierDAO {
    public User findCourierByToken(String token) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("Attempting to validate token: " + token);
            String subject = util.JwtUtil.validateToken(token);
            System.out.println("Extracted subject (UUID): " + subject);
            UUID courierId = UUID.fromString(subject);

            // کوئری HQL برای پیدا کردن کاربر با id و role = 'courier'
            String hql = "FROM User u WHERE u.id = :courierId AND u.role = :role";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("courierId", courierId);
            query.setParameter("role", "courier");
            User courier = query.uniqueResult();

            if (courier == null) {
                System.err.println("No courier found for ID: " + courierId + " with role = 'courier'");
                throw new RuntimeException("Courier not found for ID: " + courierId);
            }
            System.out.println("Found courier: ID=" + courier.getId() + ", Phone=" + courier.getPhone() + ", Status=" + courier.getStatus());
            return courier;
        } catch (Exception e) {
            System.err.println("Error in findCourierByToken: " + e.getMessage());
            throw new RuntimeException("Failed to find courier: " + e.getMessage());
        }
    }
    public List<Order> findAvailableOrders() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            String hql = "FROM Order o WHERE o.deliveryStatus IS NULL AND o.status = :status";
            Query<Order> query = session.createQuery(hql, Order.class);
            query.setParameter("status", "submitted"); // فرض می‌کنیم سفارش‌های "submitted" در دسترس هستند
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

    public void saveCourier(User courier) {
        try (Session session =HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(courier);
            session.getTransaction().commit();
        }
    }
    public List<Order> findDeliveryHistory(UUID courierId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Order o WHERE o.courierId = :courierId AND o.deliveryStatus = 'delivered' " +
                    "ORDER BY o.createdAt DESC";

            Query<Order> query = session.createQuery(hql, Order.class);
            query.setParameter("courierId", courierId);

            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch delivery history: " + e.getMessage());
        }
    }
}
