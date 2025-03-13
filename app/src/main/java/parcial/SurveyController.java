package parcial;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import parcial.config.DatabaseConfig;
import parcial.User;

import javax.servlet.http.Cookie;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SurveyController {
    private final SessionFactory sessionFactory;
    private final UserService userService;

    public SurveyController(Javalin app) {
        this.sessionFactory = DatabaseConfig.getSessionFactory();
        this.userService = new UserService();

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
            User loginRequest = ctx.bodyAsClass(User.class);
            Long userID = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
            if (userID != null) {
                ctx.sessionAttribute("user_id", userID);
                String token = userService.generateRememberMeToken(loginRequest);
                ctx.cookie("rememberMeToken", token, 60 * 60 * 24 * 30);
                //ctx.res().addCookie(cookie);
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
            User registerRequest = ctx.bodyAsClass(User.class);
            if (registerRequest.getUsername() != null && registerRequest.getPassword() != null) {
                Session session = sessionFactory.openSession();
                Transaction transaction = session.beginTransaction();
                session.persist(registerRequest);
                transaction.commit();
                session.close();
                ctx.status(201).result("User registered successfully");
            } else {
                ctx.status(400).result("Invalid registration details");
            }
        } catch (Exception e) {
            ctx.status(400).result("Invalid request format");
        }
    }

    private void createSurvey(Context ctx) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            Survey survey = ctx.bodyAsClass(Survey.class);
            survey.setTimestamp(new Date());

            session.persist(survey);
            transaction.commit();

            ctx.status(201).json(survey);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            ctx.status(500).result("Error creating survey: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private void getAllSurveys(Context ctx) {
        Session session = sessionFactory.openSession();
        try {
            List<Survey> surveys = session.createQuery("FROM Survey ORDER BY timestamp DESC", Survey.class).list();
            ctx.json(surveys);
        } catch (Exception e) {
            ctx.status(500).result("Error fetching surveys: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private void getSurveyById(Context ctx) {
        Session session = sessionFactory.openSession();
        try {
            Long id = ctx.pathParamAsClass("id", Long.class).get();
            Survey survey = session.get(Survey.class, id);

            if (survey != null) {
                ctx.json(survey);
            } else {
                ctx.status(404).result("Survey not found");
            }
        } catch (Exception e) {
            ctx.status(400).result("Invalid survey ID");
        } finally {
            session.close();
        }
    }

    private void updateSurvey(Context ctx) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            Long id = ctx.pathParamAsClass("id", Long.class).get();
            Survey updatedSurvey = ctx.bodyAsClass(Survey.class);
            updatedSurvey.setId(id);

            Survey existingSurvey = session.get(Survey.class, id);
            if (existingSurvey == null) {
                ctx.status(404).result("Survey not found");
                return;
            }
            session.merge(updatedSurvey);
            transaction.commit();

            ctx.json(updatedSurvey);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            ctx.status(500).result("Error updating survey: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private void deleteSurvey(Context ctx) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            Long id = ctx.pathParamAsClass("id", Long.class).get();

            Survey survey = session.get(Survey.class, id);
            if (survey != null) {
                session.remove(survey);
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
        } finally {
            session.close();
        }
    }

    private String generateToken(String username) {
        // Simple token generation for demo purposes
        return "token-" + username + "-" + System.currentTimeMillis();
    }
}
