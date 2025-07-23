package DAO;

import entity.User;
import entity.UserWallet;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import util.HibernateUtil;

import java.util.Optional;
import java.util.UUID;

public class UserWalletDAO {
    private final SessionFactory sessionFactory;

    public UserWalletDAO() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<UserWallet> findByUserId(UUID userId) {
        try (Session session = sessionFactory.openSession()) {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            return Optional.ofNullable(
                    session.createQuery("FROM UserWallet w WHERE w.user.id = :userId", UserWallet.class)
                            .setParameter("userId", userId)
                            .uniqueResult()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to find UserWallet by userId: " + userId, e);
        }
    }

    public void saveOrUpdate(UserWallet wallet) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            if (wallet == null) {
                throw new IllegalArgumentException("UserWallet cannot be null");
            }
            if (wallet.getUser() == null || wallet.getUser().getId() == null) {
                throw new IllegalStateException("User or User ID cannot be null in UserWallet");
            }
            // اطمینان از merge کردن شیء User برای جلوگیری از مشکلات detached entity
            wallet.setUser((User) session.merge(wallet.getUser()));
            session.saveOrUpdate(wallet);
            session.getTransaction().commit();

    } catch (Exception e) {
            e.printStackTrace();
        }
}
}