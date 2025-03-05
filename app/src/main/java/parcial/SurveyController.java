package parcial;

import io.javalin.Javalin;
import io.javalin.http.Context;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.Date;
import java.util.List;
import java.util.Map;
import parcial.config.DatabaseConfig;
import parcial.models.LoginRequest;
import parcial.models.RegisterRequest;
import parcial.Survey; // Updated import to match Survey location

public class SurveyController {
    private final EntityManager entityManager;

    public SurveyController(Javalin app) {
        this.entityManager = DatabaseConfig.getEntityManager();

        // Add authentication middleware
        app.before("/api/*", ctx -> {
            if (!ctx.path().equals("/api/auth/login") && !ctx.path().equals("/api/auth/register")) {
                String token = ctx.header("Authorization");
                if (token == null || !token.startsWith("Bearer ")) {
                    ctx.status(401).result("Unauthorized");
                }
            }
        });

        // Define routes with consistent API paths
        app.post("/api/auth/login", this::login);
        app.post("/api/auth/register", this::register);
        app.post("/api/surveys", this::createSurvey);
        app.get("/api/surveys", this::getAllSurveys);
        app.get("/api/surveys/{id}", this::getSurveyById);
        app.put("/api/surveys/{id}", this::updateSurvey);
        app.delete("/api/surveys/{id}", this::deleteSurvey);
    }

    private void login(Context ctx) {
        try {
            LoginRequest loginRequest = ctx.bodyAsClass(LoginRequest.class);
            // Simple authentication for demo purposes
            if ("admin".equals(loginRequest.getUsername()) && "password".equals(loginRequest.getPassword())) {
                String token = generateToken(loginRequest.getUsername());
                ctx.json(Map.of(
                        "token", token,
                        "username", loginRequest.getUsername()));
            } else {
                ctx.status(401).result("Invalid credentials");
            }
        } catch (Exception e) {
            ctx.status(400).result("Invalid request format");
        }
    }

    private void register(Context ctx) {
        try {
            RegisterRequest registerRequest = ctx.bodyAsClass(RegisterRequest.class);
            // Simple registration logic for demo purposes
            if (registerRequest.getUsername() != null && registerRequest.getPassword() != null) {
                // Save user to database (this is a simple example, you should hash passwords in a real app)
                // For now, we just return a success message
                ctx.status(201).result("User registered successfully");
            } else {
                ctx.status(400).result("Invalid registration details");
            }
        } catch (Exception e) {
            ctx.status(400).result("Invalid request format");
        }
    }

    private void createSurvey(Context ctx) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            Survey survey = ctx.bodyAsClass(Survey.class);
            survey.setTimestamp(new Date());

            transaction.begin();
            entityManager.persist(survey);
            transaction.commit();

            ctx.status(201).json(survey);
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
                    .createQuery("SELECT s FROM Survey s ORDER BY s.timestamp DESC", Survey.class)
                    .getResultList();
            ctx.json(surveys);
        } catch (Exception e) {
            ctx.status(500).result("Error fetching surveys: " + e.getMessage());
        }
    }

    private void getSurveyById(Context ctx) {
        try {
            Long id = ctx.pathParamAsClass("id", Long.class).get();
            Survey survey = entityManager.find(Survey.class, id);

            if (survey != null) {
                ctx.json(survey);
            } else {
                ctx.status(404).result("Survey not found");
            }
        } catch (Exception e) {
            ctx.status(400).result("Invalid survey ID");
        }
    }

    private void updateSurvey(Context ctx) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            Long id = ctx.pathParamAsClass("id", Long.class).get();
            Survey updatedSurvey = ctx.bodyAsClass(Survey.class);
            updatedSurvey.setId(id);

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
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            Long id = ctx.pathParamAsClass("id", Long.class).get();

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

    private String generateToken(String username) {
        // Simple token generation for demo purposes
        return "token-" + username + "-" + System.currentTimeMillis();
    }
}
