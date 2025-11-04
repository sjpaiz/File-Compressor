package core;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Clase coherente para escritura y lectura de archivos binarios
 * usados por el algoritmo Huffman.
 * 
 * Estructura de archivo:
 *   [bits como texto "0" y "1"] // [footer en texto]
 * 
 * Ejemplo de contenido del archivo:
 *   0110101010110//A:0;B:10;C:11
 * 
 * Ambos métodos writeBinaryFile() y readBinaryFile() son compatibles entre sí.
 */
public class FileManager {

    /**
     * Escribe una cadena de texto (bits + footer) en un archivo binario.
     * El contenido se escribe tal cual en bytes UTF-8, sin alteraciones.
     */
    public static void writeBinaryFile(String content, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            byte[] data = content.getBytes(StandardCharsets.UTF_8);
            fos.write(data);
            fos.flush();
            System.out.println("✅ Archivo binario escrito correctamente: " + filePath);
        } catch (IOException e) {
            System.err.println("❌ Error al escribir el archivo binario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lee un archivo binario escrito con writeBinaryFile()
     * y devuelve exactamente la misma cadena de texto.
     *
     * Devuelve algo como:
     * "0110101101010//A:0;B:10;C:11"
     *
     * Así tu descompresor puede seguir usando split("//") sin problemas.
     */
    public static String readBinaryFile(String filePath) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("❌ Error al leer el archivo binario: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    // === Pequeña prueba ===
    public static void main(String[] args) {
        String original = "0110100101010110//A:0;B:10;C:11";
        String path = "output.bin";

        // Escribir
        writeBinaryFile(original, path);

        // Leer
        String leido = readBinaryFile(path);

        System.out.println("🔸 Original: " + original);
        System.out.println("🔹 Leído   : " + leido);

        if (original.equals(leido)) {
            System.out.println("✅ Coinciden perfectamente, sistema coherente.");
        } else {
            System.out.println("⚠️ Diferencia detectada entre escritura y lectura.");
        }
    }
}
