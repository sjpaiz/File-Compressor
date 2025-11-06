package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import compression.HuffmanCompressor;
import compression.LZ77Compressor;
import compression.Tupla;
//Encargado de recibir el archivo como strings y coordinar compresion y encripcion

public class FileProcessor{

    public static void comprimirArchivo(String rutaArchivo, LZ77Compressor lz, HuffmanCompressor hf) {
        final int chunkSizeBytes = 10240 * 10240; // 25MB aprox.
        List<List<String>> bloquesCompresion = new ArrayList<>();
        String headerFinal = "";

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
                List<Tupla> tuplas = lz.comprimir(bloque, 16384, 1024);
                String tuplasStr = lz.tuplasAString(tuplas);

                // HUFFMAN
                List<String> bloqueComprimido = hf.comprimir(tuplasStr);
                headerFinal = hf.getHeader();

                bloquesCompresion.add(bloqueComprimido);
                System.out.printf("Bloque %d comprimido (%d caracteres leídos)%n", contadorBloques, charsRead);
            }

            if (!bloquesCompresion.isEmpty()) {
                for (int i = 0; i < bloquesCompresion.size(); i++) {
                    String nombreArchivo = rutaArchivo + ".comp";
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

public static void descomprimirArchivo(String archivoComprimido, LZ77Compressor lz, HuffmanCompressor hf) {
    System.out.println("\nDescomprimiendo todos los bloques...");
    StringBuilder resultadoFinal = new StringBuilder();

    final int tamVentana = 16384; // debe coincidir con el usado en compresión

    int bloque = 1;
    while (bloque < 2) {
        String nombreArchivo = archivoComprimido;
        System.out.println(nombreArchivo);
        File archivo = new File(nombreArchivo);
        if (!archivo.exists()) break;

        FileManager.readBinaryFile(nombreArchivo);
        String header = FileManager.getHeader();
        String bytesLeidos = FileManager.getBytes();

        String datosDescomprimidos = hf.descomprimir(bytesLeidos, header);
        // Convertir string a lista de tuplas
        List<Tupla> tuplas = lz.stringATuplas(datosDescomprimidos);

        String contexto = "";
        if (resultadoFinal.length() > 0) {
            int start = Math.max(0, resultadoFinal.length() - tamVentana);
            contexto = resultadoFinal.substring(start);
        }

        String bloqueConContexto = lz.descomprimir(tuplas, contexto);

        String parteNueva;
        if (contexto.isEmpty()) {
            parteNueva = bloqueConContexto;
        } else {
            parteNueva = bloqueConContexto.substring(contexto.length());
        }

        resultadoFinal.append(parteNueva);

        System.out.printf("Bloque %d descomprimido correctamente (añadidos %d caracteres)%n", bloque, parteNueva.length());
        bloque++;
    }

    FileManager.writePlainTextFile(resultadoFinal.toString(), "descomprimido.txt");
    System.out.println("\nDescompresión completa. Archivo final: descomprimido.txt");
}
}