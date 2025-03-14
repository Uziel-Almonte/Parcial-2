package parcial;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsContext;
import org.eclipse.jetty.websocket.api.Session;
import io.javalin.plugin.bundled.CorsPluginConfig;
import parcial.config.DatabaseConfig;
import org.h2.tools.Server;
//import org.hibernate.Session;
//import org.hibernate.SessionFactory;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class App {
    private static final String STATIC_FILES_DIR = "/public";
    private static final int PORT = 7000;
    private static final Set<WsContext> connectedClients = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        // Initialize the sessionFactory
        //SessionFactory sessionFactory = DatabaseConfig.getSessionFactory();

        // Initialize database first
        DatabaseConfig.init();

        // Create H2 Console Server
        Server h2Server = null;
        try {
            h2Server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082", "-ifNotExists");
            h2Server.start();
            System.out.println("H2 Console available at http://localhost:8082");
        } catch (SQLException e) {
            System.err.println("Failed to start H2 Console: " + e.getMessage());
            e.printStackTrace();
        }

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.directory = STATIC_FILES_DIR;
                staticFiles.location = Location.CLASSPATH;
                staticFiles.hostedPath = "/";
            });

            // Configure CORS with new syntax
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        }).start(PORT);

        // WebSocket endpoint
        app.ws("/ws/surveys", ws -> {
            ws.onConnect(ctx -> {
                System.out.println("Client connected: " + ctx.sessionId());
                connectedClients.add(ctx);
            });

            ws.onClose(ctx -> {
                System.out.println("Client disconnected: " + ctx.sessionId());
                connectedClients.remove(ctx);
            });

            ws.onMessage(ctx -> {
                System.out.println("Message received: " + ctx.message());
            });
        });
        /* 
        // Broadcast survey updates
        app.post("/api/surveys", ctx -> {
            Survey survey = ctx.bodyAsClass(Survey.class);
            // Save survey to the database (existing logic)
            // Broadcast the new survey to all connected WebSocket clients
            for (WsContext client : connectedClients) {
                client.send(survey.toJson());
            }
            ctx.status(201).result("Survey created and broadcasted.");
        });
        */
        
        // Initialize controllers
        new SurveyController(app);

        System.out.println("Server running on http://localhost:" + PORT);

        // Add shutdown hook for cleanup
        final Server finalH2Server = h2Server;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (finalH2Server != null) {
                finalH2Server.stop();
            }
            DatabaseConfig.shutdown();
        }));
    }

    public String getGreeting() {
        return "Hello World!";
    }
}
