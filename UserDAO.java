package DAO;

import entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import util.HibernateUtil;
import util.JwtUtil;

import java.util.Optional;
import java.util.UUID;


public class UserDAO {
    private static final SessionFactory sessionFactory= HibernateUtil.buildSessionFactory();

    public UserDAO() {
    }

    public void save(User user) {
        System.out.println("Saving user: " + user.getPhone());
        System.out.println("Saving user: " + user.getPhone());
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();


            Query<User> query = session.createQuery("FROM User WHERE phone = :phone", User.class);
            query.setParameter("phone", user.getPhone());
            User existingUser = query.uniqueResult();
            if (existingUser != null) {
                session.getTransaction().rollback();
                throw new RuntimeException("Phone number already exists");
            }

            session.save(user);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Error in save: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    public void delete(User user){
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(user);
            session.getTransaction().commit();
        }
    }
    public void update(User user) {
        if (user.getPassword() == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(user);
            session.getTransaction().commit();
        }
    }
    public boolean isPhoneTaken(String phone) {
        System.out.println("Checking if phone is taken: " + phone);
        try{
            return findByPhone(phone).isPresent();
        }catch(Exception e){
            return true;
        }

    }
    public Optional<User> findByPhone(String phone) {
        System.out.println("Finding user by phone: " + phone);
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE phone = :phone",
                    User.class);
            query.setParameter("phone", phone);
            User user = query.uniqueResult();
            return user != null ? Optional.of(user) : Optional.empty();
        }
    }
    public Optional<User> findById(UUID id) {
        System.out.println("Searching for user with id: " + id);
        try (Session session = sessionFactory.openSession()) {
            User user = session.get(User.class, id);
            return user != null ? Optional.of(user) : Optional.empty();
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
}
