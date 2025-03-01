package parcial;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import parcial.config.DatabaseConfig;
import java.util.List;

public class SurveyRepository {
    private final EntityManager entityManager;

    public SurveyRepository() {
        this.entityManager = DatabaseConfig.getEntityManager();
    }

    public Survey save(Survey survey) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(survey);
            transaction.commit();
            return survey;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public Survey findById(Long id) {
        return entityManager.find(Survey.class, id);
    }

    public List<Survey> findAll() {
        return entityManager.createQuery("SELECT s FROM Survey s", Survey.class)
                .getResultList();
    }

    public void delete(Long id) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Survey survey = findById(id);
            if (survey != null) {
                entityManager.remove(survey);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public Survey update(Survey survey) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Survey updatedSurvey = entityManager.merge(survey);
            transaction.commit();
            return updatedSurvey;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
