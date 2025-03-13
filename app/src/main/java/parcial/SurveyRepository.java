package parcial;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import parcial.config.DatabaseConfig;

import java.util.List;

public class SurveyRepository {
    private final SessionFactory sessionFactory;

    public SurveyRepository() {
        this.sessionFactory = DatabaseConfig.getSessionFactory();
    }

    public Survey save(Survey survey) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(survey);
            transaction.commit();
            return survey;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public Survey findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Survey.class, id);
        }
    }

    public List<Survey> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Survey", Survey.class).list();
        }
    }

    public void delete(Long id) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Survey survey = session.get(Survey.class, id);
            if (survey != null) {
                session.remove(survey);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public Survey update(Survey survey) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Survey updatedSurvey = session.merge(survey);
            transaction.commit();
            return updatedSurvey;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
