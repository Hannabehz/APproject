package util;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    public static SessionFactory buildSessionFactory() {
        try {
            System.out.println("Loading hibernate.cfg.xml");
            Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
            System.out.println("hibernate.cfg.xml loaded successfully");
            return configuration.buildSessionFactory();
        } catch (Exception e) {
            System.err.println("Failed to initialize Hibernate: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError("Failed to initialize Hibernate: " + e.getMessage());
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            System.err.println("SessionFactory is null");
            throw new IllegalStateException("SessionFactory has not been initialized");
        }
        return sessionFactory;
    }
}
