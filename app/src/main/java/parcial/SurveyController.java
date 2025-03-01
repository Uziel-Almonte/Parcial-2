package parcial;

import io.javalin.Javalin;
import io.javalin.http.Context;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.Date;
import parcial.config.DatabaseConfig;
import java.util.List;

public class SurveyController {
    private final EntityManager entityManager;

    public SurveyController(Javalin app) {
        this.entityManager = DatabaseConfig.getEntityManager();

        // Define routes
        app.post("/survey", this::createSurvey);
        app.get("/survey", this::getAllSurveys);
        app.get("/survey/{id}", this::getSurveyById);
        app.put("/survey/{id}", this::updateSurvey);
        app.delete("/survey/{id}", this::deleteSurvey);
    }

    private void createSurvey(Context ctx) {
        Survey survey = ctx.bodyAsClass(Survey.class);
        survey.setTimestamp(new Date());

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(survey);
            transaction.commit();
            ctx.json(survey);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            ctx.status(500).result("Error creating survey: " + e.getMessage());
        }
    }

    private void getAllSurveys(Context ctx) {
        try {
            List<Survey> surveys = entityManager
                    .createQuery("SELECT s FROM Survey s", Survey.class)
                    .getResultList();
            ctx.json(surveys);
        } catch (Exception e) {
            ctx.status(500).result("Error fetching surveys: " + e.getMessage());
        }
    }

    private void getSurveyById(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Survey survey = entityManager.find(Survey.class, id);

        if (survey != null) {
            ctx.json(survey);
        } else {
            ctx.status(404).result("Survey not found");
        }
    }

    private void updateSurvey(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Survey updatedSurvey = ctx.bodyAsClass(Survey.class);
        updatedSurvey.setId(id);

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Survey existingSurvey = entityManager.find(Survey.class, id);
            if (existingSurvey == null) {
                ctx.status(404).result("Survey not found");
                return;
            }
            entityManager.merge(updatedSurvey);
            transaction.commit();
            ctx.json(updatedSurvey);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            ctx.status(500).result("Error updating survey: " + e.getMessage());
        }
    }

    private void deleteSurvey(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Survey survey = entityManager.find(Survey.class, id);
            if (survey != null) {
                entityManager.remove(survey);
                transaction.commit();
                ctx.status(204);
            } else {
                ctx.status(404).result("Survey not found");
            }
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            ctx.status(500).result("Error deleting survey: " + e.getMessage());
        }
    }
}
