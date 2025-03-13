package parcial;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import parcial.User;
import parcial.config.DatabaseConfig;

import java.util.UUID;

public class UserService {
    private final SessionFactory sessionFactory;

    public UserService() {
        this.sessionFactory = DatabaseConfig.getSessionFactory();
    }

    public Session getSession() {
        return sessionFactory.openSession();
    }

    public Long authenticate(String username, String password) {
        Session session = null;
        try {
            session = getSession();
            User user = session.createQuery("FROM User WHERE username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();

            if (user != null && user.authenticate(password)) {
                return user.getId();
            }
        } catch (Exception e) {
            if (session != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public String generateRememberMeToken(User user) {
        String token = UUID.randomUUID().toString();
        user.setRememberMeToken(token);
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        session.merge(user);
        tx.commit();
        session.close();
        return token;
    }
}