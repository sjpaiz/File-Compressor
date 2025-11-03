//Archivo raiz del proyecto
import spark.Spark;

public class Main {
    public static void main(String[] args) {
        Spark.staticFileLocation("../resources/web/");
        Spark.get("/hello", (req, res) -> "Hello world xd");
    }
}
