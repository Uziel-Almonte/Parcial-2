package parcial.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import parcial.User;
import java.util.Map;
import java.util.HashMap;

public class DatabaseConfig {
    private static EntityManagerFactory entityManagerFactory;
    private static final String PERSISTENCE_UNIT_NAME = "survey-pu";

    public static void init() {
        try {
            if (entityManagerFactory == null) {
                entityManagerFactory = Persistence.createEntityManagerFactory("default", getProperties());
                EntityManager entityManager = entityManagerFactory.createEntityManager();
                entityManager.getTransaction().begin();
                entityManager.createNativeQuery("CREATE TABLE IF NOT EXISTS User (id VARCHAR(255) PRIMARY KEY, username VARCHAR(255), password VARCHAR(255))").executeUpdate();
                entityManager.getTransaction().commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing database", e);
        }
    }

    public static EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            init();
        }
        return entityManagerFactory.createEntityManager();
    }

    public static void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    private static Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        properties.put("jakarta.persistence.jdbc.user", "sa");
        properties.put("jakarta.persistence.jdbc.password", "");
        properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        return properties;
    }
}