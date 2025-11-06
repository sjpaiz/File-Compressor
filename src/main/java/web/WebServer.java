package web;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

public class WebServer {

    public static void main(String[] args) {
        // Primero se define el puerto
        port(4567);

        staticFiles.location("/web");

        enableCORS();

        // Rutas
        get("/ping", (req, res) -> "Servidor activo");

        post("/upload", Controllers::handleFileUpload);

        System.out.println("Servidor iniciado en http://localhost:4567/");
    }

    private static void enableCORS() {
        options("/*", (req, res) -> {
            String headers = req.headers("Access-Control-Request-Headers");
            if (headers != null) {
                res.header("Access-Control-Allow-Headers", headers);
            }

            String method = req.headers("Access-Control-Request-Method");
            if (method != null) {
                res.header("Access-Control-Allow-Methods", method);
            }

            return "OK";
        });

        // Añade encabezados CORS en cada respuesta
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
            res.header("Access-Control-Expose-Headers", "Content-Disposition");
        });
    }
}
