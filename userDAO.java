package DAO;

import Entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

public class userDAO {
    public void save(User user){
        Transaction tx = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            tx = session.beginTransaction();
            session.save(user);
            tx.commit();
        }
        catch(Exception e){
            if(tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
    public void delete(User user){
        Transaction tx = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            tx = session.beginTransaction();
            session.delete(user);
            tx.commit();
        }
    }
    public void update(User user){
        Transaction tx = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            tx = session.beginTransaction();
            session.update(user);
            tx.commit();
        }
    }
    public boolean isPhoneTaken(String phone){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Long count=session.createQuery("SELECT COUNT(u) FROM User u WHERE phone= :phone",Long.class)
                    .setParameter("phone",phone).uniqueResult();
            return count !=null&&count>0;
        }
    }
}
