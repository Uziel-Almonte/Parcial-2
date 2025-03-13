package parcial;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.CorsPluginConfig;
import parcial.config.DatabaseConfig;
import org.h2.tools.Server;
import org.hibernate.SessionFactory;
import java.sql.SQLException;

public class App {
    private static final String STATIC_FILES_DIR = "/public";
    private static final int PORT = 7000;

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
