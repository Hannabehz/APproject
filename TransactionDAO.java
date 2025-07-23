package DAO;


import entity.Transaction;
import entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import util.HibernateUtil;

import java.util.List;
import java.util.UUID;

public class TransactionDAO {
    private final SessionFactory sessionFactory;

    public TransactionDAO() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public void saveTransaction(Transaction transaction) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(transaction);
            session.getTransaction().commit();
        }
    }

    public List<Transaction> findByUserId(UUID userId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Transaction t WHERE t.user.id = :userId", Transaction.class)
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }
}