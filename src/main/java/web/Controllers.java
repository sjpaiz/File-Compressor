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

            // === CASO 1: múltiples archivos (carpeta) ===
            if (parts.size() > 1) {
                System.out.println("Procesando carpeta con varios archivos...");
                Path tempFolder = Files.createTempDirectory("inputFolder_");
                Path outputFolder = Files.createTempDirectory("outputFolder_");

                LZ77Compressor lz = new LZ77Compressor();
                HuffmanCompressor hf = new HuffmanCompressor();

                int count = 0;
                for (Part part : parts) {
                    String submitted = part.getSubmittedFileName();
                    if (submitted == null || submitted.isBlank()) continue;

                    String cleanName = Paths.get(submitted).getFileName().toString();
                    if (!cleanName.endsWith(".txt")) {
                        System.out.println("Ignorado: " + cleanName);
                        continue;
                    }

                    Path inputFile = tempFolder.resolve(cleanName);
                    try (InputStream in = part.getInputStream()) {
                        Files.copy(in, inputFile, StandardCopyOption.REPLACE_EXISTING);
                    }

                    System.out.println("Comprimiendo: " + cleanName);
                    try {
                        FileProcessor.comprimirArchivo(inputFile.toString(), lz, hf);
                        Path compressed = Paths.get(inputFile.toString() + ".comp");
                        if (Files.exists(compressed)) {
                            Files.move(compressed, outputFolder.resolve(compressed.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                            count++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Error al comprimir " + cleanName + ": " + e.getMessage());
                    }
                }

                if (count == 0) {
                    res.status(400);
                    return "No se comprimió ningún archivo válido.";
                }

                System.out.println("Archivos comprimidos correctamente en: " + outputFolder.toAbsolutePath());
                res.status(200);
                res.type("text/plain");
                return "Archivos comprimidos disponibles en: " + outputFolder.toAbsolutePath().toString();
            }

            // === CASO 2: archivo único ===
            Part singlePart = parts.iterator().next();
            String fileName = singlePart.getSubmittedFileName();

            if (fileName == null || fileName.isBlank()) {
                res.status(400);
                return "El archivo no tiene nombre válido.";
            }

            Path inputFile = Files.createTempFile("upload_", "_" + fileName);
            try (InputStream in = singlePart.getInputStream()) {
                Files.copy(in, inputFile, StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("Archivo recibido: " + fileName + " → acción: " + action);
            String processed = processFile(inputFile.toString(), action);

            res.header("Content-Disposition", "attachment; filename=" + Paths.get(processed).getFileName());
            res.type("application/octet-stream");
            return new BufferedInputStream(Files.newInputStream(Paths.get(processed)));

        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            return "Error en el servidor: " + e.getMessage();
        }
    }

    private static String detectAction(String name) {
        if (name.endsWith(".comp")) return "decompress";
        if (name.endsWith(".enc")) return "decrypt";
        return "compress";
    }

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
