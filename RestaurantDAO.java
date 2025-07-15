package DAO;

import entity.Restaurant;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.Arrays;
import java.util.List;

public class RestaurantDAO {

    public void save(Restaurant restaurant) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(restaurant);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Restaurant restaurant) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.delete(restaurant);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void update(Restaurant restaurant) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(restaurant);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public Restaurant findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Restaurant.class, id);
        }
    }

    public List<Restaurant> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT r FROM Restaurant r", Restaurant.class).getResultList();
        }
    }

    /*public Arrays findBySellerId(int sellerId) {
    }*/
    public List<Restaurant> findAllWithFilters(String search, List<String> categories) {
        System.out.println("Fetching restaurants with search: " + search + ", categories: " + categories);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Restaurant r JOIN FETCH r.seller s WHERE 1=1");
            if (search != null && !search.isEmpty()) {
                hql.append(" AND s.restaurantName LIKE :search");
            }
            if (categories != null && !categories.isEmpty()) {
                hql.append(" AND s.category IN :categories");
            }

            Query<Restaurant> query = session.createQuery(hql.toString(), Restaurant.class);
            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search + "%");
            }
            if (categories != null && !categories.isEmpty()) {
                query.setParameter("categories", categories);
            }

            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error in findAllWithFilters: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch restaurants: " + e.getMessage());
        }
    }
}

