package parcial;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import parcial.User;
import parcial.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

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

        if (user != null) {
            try {
                // Check password using BCrypt
                if (BCrypt.checkpw(password, user.getPassword())) {
                    return user.getId();
                }
            } catch (IllegalArgumentException e) {
                // If BCrypt fails, check plain password and upgrade to BCrypt
                if (password.equals(user.getPassword())) {
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                    Transaction tx = session.beginTransaction();
                    user.setPassword(hashedPassword);
                    session.merge(user);
                    tx.commit();
                    return user.getId();
                }
            }
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
    return null; // Return null if authentication fails
    }

    public String generateRememberMeToken(Long userId) {
    String token = java.util.UUID.randomUUID().toString();
    Session session = null;
    try {
        session = getSession();
        session.beginTransaction();
        User user = session.get(User.class, userId);
        if (user != null) {
            user.setRememberMeToken(token);
            session.merge(user);
            session.getTransaction().commit();
        }
    } catch (Exception e) {
        e.printStackTrace();
        if (session != null && session.getTransaction().isActive()) {
            session.getTransaction().rollback();
        }
    } finally {
        if (session != null) {
            session.close();
        }
    }
    return token;
    }
}