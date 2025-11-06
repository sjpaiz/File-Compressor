package web;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

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

    public static Object handleFileUpload(Request req, Response res) throws Exception {
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));

        Part part = req.raw().getPart("file");
        String uploadedFileName = part.getSubmittedFileName();
        Path outputPath = Paths.get(uploadedFileName);

        try (InputStream in = part.getInputStream()) {
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Detectar acción a realizar según la extensión
        String action = detectAction(uploadedFileName);
        System.out.println("Archivo recibido: " + uploadedFileName + " -> acción: " + action);

        // Procesar el archivo (compresión/descompresión/encriptación)
        String processedFile = processFile(uploadedFileName, action);

        res.header("Content-Disposition", "attachment; filename=" + Paths.get(processedFile).getFileName());
        res.type("application/octet-stream");

        return new BufferedInputStream(Files.newInputStream(Paths.get(processedFile)));
    }

    private static String detectAction(String name) {
        //if (name.endsWith(".enc")) return "decrypt";
        if (name.endsWith(".comp")) return "decompress";
        if (name.endsWith(".comp.enc") || name.endsWith(".enc")) return "decrypt+decompress";
        return "compress+encrypt";
    }

    private static String processFile(String name, String action) throws Exception {
        LZ77Compressor lz = new LZ77Compressor();
        HuffmanCompressor hf = new HuffmanCompressor();

        switch (action) {
            case "decrypt":
                RSA.decryptStringToFile(Files.readString(Paths.get(name)), "output.txt");
                return "output.txt";

            case "decompress":
                System.out.println("xd");
                FileProcessor.descomprimirArchivo(name, lz, hf);
                return "descomprimido.txt";

            case "decrypt+decompress":
                System.out.println(name);
                RSA.decryptStringToFile(FileManager.readBinaryFileToBase64Stream(name), "temp.comp");
                FileProcessor.descomprimirArchivo("temp.comp", lz, hf);
                return "descomprimido.txt";

            case "compress+encrypt":
            default:
                FileProcessor.comprimirArchivo(name, lz, hf);
                String encrypted = RSA.encryptFileToString(name + ".comp");
                //System.out.println(encrypted);
                byte[] binaryData = Base64.getDecoder().decode(encrypted);
                FileManager.writeCriptedBinaryFile(binaryData, name + ".comp.enc");
                return name + ".comp.enc";
        }
    }
}
