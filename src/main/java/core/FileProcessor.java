package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import compression.HuffmanCompressor;
import compression.LZ77Compressor;
import compression.Tupla;

public class FileProcessor {

    public static void comprimirArchivo(String rutaArchivo, LZ77Compressor lz, HuffmanCompressor hf) {
        final int chunkSizeBytes = 5120 * 5120; // 25MB aprox.
        List<List<String>> bloquesCompresion = new ArrayList<>();
        String headerFinal = "";

        // Carpeta fija para guardar los archivos comprimidos
        String carpetaSalida = "C:\\Comprimidos";
        File carpeta = new File(carpetaSalida);
        if (!carpeta.exists()) carpeta.mkdirs();

        try (BufferedReader reader = FileManager.crearLectorUTF8(rutaArchivo)) {
            if (reader == null) {
                System.err.println("No se pudo abrir el archivo.");
                return;
            }

            char[] buffer = new char[chunkSizeBytes];
            int charsRead;
            int contadorBloques = 0;

            System.out.println("\nIniciando compresión...");
            while ((charsRead = reader.read(buffer, 0, chunkSizeBytes)) != -1) {
                contadorBloques++;
                String bloque = new String(buffer, 0, charsRead);

                // LZ77
                List<Tupla> tuplas = lz.comprimir(bloque, 8192, 512);
                String tuplasStr = lz.tuplasAString(tuplas);

                // HUFFMAN
                List<String> bloqueComprimido = hf.comprimir(tuplasStr);
                headerFinal = hf.getHeader();

                bloquesCompresion.add(bloqueComprimido);
                System.out.printf("Bloque %d comprimido (%d caracteres leídos)%n", contadorBloques, charsRead);
            }

            if (!bloquesCompresion.isEmpty()) {
                String nombreBase = new File(rutaArchivo).getName().replaceAll("\\.[^.]*$", ""); // sin extensión
                for (int i = 0; i < bloquesCompresion.size(); i++) {
                    String nombreArchivo = carpetaSalida + "\\" + nombreBase + "_bloque" + (i + 1) + ".comp";
                    FileManager.writeBinaryFile(bloquesCompresion.get(i), headerFinal, nombreArchivo);
                    System.out.printf("Bloque %d guardado como %s%n", i + 1, nombreArchivo);
                }
            } else {
                System.out.println("No se generaron bloques de compresión.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Descompresión (sin cambios por ahora)
    public static void descomprimirArchivo(String archivoComprimido, LZ77Compressor lz, HuffmanCompressor hf) {
        System.out.println("\nDescomprimiendo todos los bloques...");
        StringBuilder resultadoFinal = new StringBuilder();
        final int tamVentana = 16384;
        int bloque = 1;

        while (bloque < 2) {
            String nombreArchivo = archivoComprimido;
            File archivo = new File(nombreArchivo);
            if (!archivo.exists()) break;

            FileManager.readBinaryFile(nombreArchivo);
            String header = FileManager.getHeader();
            String bytesLeidos = FileManager.getBytes();

            String datosDescomprimidos = hf.descomprimir(bytesLeidos, header);
            List<Tupla> tuplas = lz.stringATuplas(datosDescomprimidos);

            String contexto = "";
            if (resultadoFinal.length() > 0) {
                int start = Math.max(0, resultadoFinal.length() - tamVentana);
                contexto = resultadoFinal.substring(start);
            }

            String bloqueConContexto = lz.descomprimir(tuplas, contexto);
            String parteNueva = contexto.isEmpty() ? bloqueConContexto : bloqueConContexto.substring(contexto.length());
            resultadoFinal.append(parteNueva);

            System.out.printf("Bloque %d descomprimido correctamente (añadidos %d caracteres)%n",
                    bloque, parteNueva.length());
            bloque++;
        }

        FileManager.writePlainTextFile(resultadoFinal.toString(), "descomprimido.txt");
        System.out.println("\nDescompresión completa. Archivo final: descomprimido.txt");
    }
}
