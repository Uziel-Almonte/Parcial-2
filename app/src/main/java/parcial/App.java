package parcial;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class App {
    private static final String STATIC_FILES_DIR = "/public";
    private static final int PORT = 7070;

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(STATIC_FILES_DIR, Location.CLASSPATH);
            config.plugins.enableCors(cors -> cors.add(it -> {
                it.anyHost();
            }));
        }).start(PORT);

        // Initialize controllers
        new SurveyController(app);

        System.out.println("Server running on http://localhost:" + PORT);
    }
}
