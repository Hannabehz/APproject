package DAO;

import entity.ShoppingCart;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.Optional;
import java.util.UUID;



public class ShoppingCartDAO {
    private final SessionFactory sessionFactory;

    public ShoppingCartDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Optional<ShoppingCart> findByUserId(UUID userId) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.createQuery("FROM ShoppingCart sc JOIN FETCH sc.orderItems WHERE sc.user.id = :userId", ShoppingCart.class)
                    .setParameter("userId", userId)
                    .uniqueResult());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find shopping cart: " + e.getMessage());
        }
    }

    public void saveOrUpdate(ShoppingCart cart) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(cart);
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save or update shopping cart: " + e.getMessage());
        }
    }

}