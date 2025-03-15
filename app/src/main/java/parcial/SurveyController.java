package parcial;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import parcial.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;
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
        //app.get("/api/surveys", this::getAllSurveys);
        app.get("/api/surveys", this::getAllSyncedSurveys);
        app.get("/api/surveys/{id}", this::getSurveyById);
        app.put("/api/surveys/{id}", this::updateSurvey);
        app.delete("/api/surveys/{id}", this::deleteSurvey);
    }

    private void login(Context ctx) {
        try {
            User loginRequest = ctx.bodyAsClass(User.class);
            Long userId = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
            if (userId != null) {
                // Generate or reuse the rememberMeToken
                String token = userService.generateRememberMeToken(userId);
    
                // Set session attribute and cookie
                ctx.sessionAttribute("user_id", userId);
                ctx.cookie("rememberMeToken", token, 60 * 60 * 24 * 30);
    
                // Respond with token and username
                ctx.json(Map.of(
                        "token", token,
                        "username", loginRequest.getUsername()
                ));
                System.out.println("LOGIN SUCCESSFUL");
            } else {
                ctx.status(401).result("Invalid credentials");
            }
        } catch (Exception e) {
            ctx.status(400).result("Invalid request format");
            e.printStackTrace();
        }
    }

    public User getUserByRememberMeToken(String token) {
    try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
        return session.createQuery("FROM User WHERE rememberMeToken = :token", User.class)
                .setParameter("token", token)
                .uniqueResult();
    }
}


    private void register(Context ctx) {
        try {
            User registerRequest = ctx.bodyAsClass(User.class);
            if (registerRequest.getUsername() != null && registerRequest.getPassword() != null) {
                // Hash the password before storing it
                String hashedPassword = BCrypt.hashpw(registerRequest.getPassword(), BCrypt.gensalt());
                registerRequest.setPassword(hashedPassword);
                
                Session session = sessionFactory.openSession();
                Transaction transaction = session.beginTransaction();
                session.persist(registerRequest);
                transaction.commit();
                session.close();
                
                ctx.status(201).json(Map.of("message", "User registered successfully"));
            } else {
                ctx.status(400).json(Map.of("error", "Invalid registration details"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(400).json(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    private void createSurvey(Context ctx) {
        try {
            Survey survey = ctx.bodyAsClass(Survey.class);
            
            // Provide default education level if null
            if (survey.getEducationLevel() == null) {
                survey.setEducationLevel(Survey.EducationLevel.BASIC); // Use BASIC as default
            }
            
            // Ensure synced is set to true when saving to database
            if (survey.getSynced() == null) {
                survey.setSynced(true);
            }

            try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
                Transaction transaction = session.beginTransaction();
                session.persist(survey);
                transaction.commit();
            }

            ctx.status(201).json(survey);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Failed to save survey."));
        }
    }

    private void getAllSurveys(Context ctx) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            List<Survey> surveys = session.createQuery("FROM Survey", Survey.class).list();
            ctx.json(surveys); // Send surveys as JSON response
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Failed to retrieve surveys."));
        }
    }

    private void getAllSyncedSurveys(Context ctx) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            // Query only surveys with synced = true
            List<Survey> surveys = session.createQuery("FROM Survey WHERE synced = true", Survey.class).list();
            ctx.json(surveys);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Failed to retrieve synced surveys"));
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
