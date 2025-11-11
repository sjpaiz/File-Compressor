package web;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import compression.HuffmanCompressor;
import compression.LZ77Compressor;
import core.FileManager;
import core.FileProcessor;
import encryption.RSA;
import spark.Request;
import spark.Response;

public class Controllers {

    public static Object handleFileUpload(Request req, Response res) {
        System.out.println("Inicio de carga...");

        try {
            req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));
            Collection<Part> parts = req.raw().getParts();

            if (parts == null || parts.isEmpty()) {
                res.status(400);
                return "No se recibió ningún archivo.";
            }

            String action = req.queryParams("action");
            if (action == null || action.isEmpty()) action = "compress";

            // Carpetas de trabajo
            Path outputFolder = Paths.get("C:\\Comprimidos");
            if (!Files.exists(outputFolder)) Files.createDirectories(outputFolder);

            // Detectar si hay más de un archivo (carpeta)
            boolean esCarpeta = parts.size() > 1;
            System.out.println(esCarpeta ? "Procesando CARPETA con varios archivos..." : "Procesando ARCHIVO único...");

            LZ77Compressor lz = new LZ77Compressor();
            HuffmanCompressor hf = new HuffmanCompressor();
            int procesados = 0;

            for (Part part : parts) {
                String submitted = part.getSubmittedFileName();
                if (submitted == null || submitted.isBlank()) continue;

                String cleanName = Paths.get(submitted).getFileName().toString();
                Path tempInput = Files.createTempFile("input_", "_" + cleanName);

                try (InputStream in = part.getInputStream()) {
                    Files.copy(in, tempInput, StandardCopyOption.REPLACE_EXISTING);
                }

                System.out.println("Procesando: " + cleanName + " (" + action + ")");
                processFile(tempInput.toString(), action);
                procesados++;
            }

            if (procesados == 0) {
                res.status(400);
                return "No se procesó ningún archivo válido.";
            }

            // ✅ Mostrar mensaje claro en lugar de intentar descargar algo
            String mensaje = esCarpeta
                    ? "✅ Carpeta comprimida correctamente. Archivos disponibles en: " + outputFolder.toAbsolutePath()
                    : "✅ Archivo comprimido correctamente. Guardado en: " + outputFolder.toAbsolutePath();

            System.out.println(mensaje);
            res.status(200);
            res.type("text/plain");
            return mensaje;

        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            return "Error en el servidor: " + e.getMessage();
        }
    }

    // === Procesamiento según acción ===
    private static String processFile(String name, String action) throws Exception {
        LZ77Compressor lz = new LZ77Compressor();
        HuffmanCompressor hf = new HuffmanCompressor();

        switch (action) {
            case "compress":
                System.out.println("Acción: Comprimir");
                FileProcessor.comprimirArchivo(name, lz, hf);
                return name + ".comp";

            case "deCompress":
                System.out.println("Acción: Descomprimir");
                FileProcessor.descomprimirArchivo(name, lz, hf);
                return "descomprimido.txt";

            case "encrypt":
                System.out.println("Acción: Encriptar");
                String encrypted = RSA.encryptFileToString(name);
                byte[] binaryData = Base64.getDecoder().decode(encrypted);
                FileManager.writeCriptedBinaryFile(binaryData, name + ".enc");
                return name + ".enc";

            case "deEncrypt":
                System.out.println("Acción: Desencriptar");
                RSA.decryptStringToFile(FileManager.readBinaryFileToBase64Stream(name), "output.txt");
                return "output.txt";

            default:
                throw new IllegalArgumentException("Acción no reconocida: " + action);
        }
    }
}
