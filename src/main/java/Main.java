import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import compression.HuffmanCompressor;
import compression.LZ77Compressor;
import compression.Tupla;
import core.FileManager;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        HuffmanCompressor hf = new HuffmanCompressor();
        LZ77Compressor lz = new LZ77Compressor();

        System.out.println("============== MENÚ DE COMPRESIÓN ==============");
        System.out.println("1. Comprimir archivo");
        System.out.println("2. Descomprimir archivo");
        System.out.print("Seleccione una opción: ");
        int opcion = sc.nextInt();
        sc.nextLine(); // limpiar buffer

        switch (opcion) {
            case 1 :
                System.out.print("Ingrese el nombre del archivo a comprimir (ej: test3.txt): ");
                String archivoEntrada = sc.nextLine();
                comprimirArchivo(archivoEntrada, lz, hf);
                break;
            case 2 :
                System.out.print("Ingrese el nombre del archivo a descomprimir (ej: output.bin): ");
                String archivoComprimido = sc.nextLine();
                descomprimirArchivo(archivoComprimido, lz, hf);
                break;
            default : System.out.println("Opción no válida.");
        }

        sc.close();
    }

    private static void comprimirArchivo(String rutaArchivo, LZ77Compressor lz, HuffmanCompressor hf) {
        final int chunkSizeBytes = 10240 * 10240;
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
                    String nombreArchivo = rutaArchivo + (i + 1) + ".comp";
                    FileManager.writeBinaryFile(bloquesCompresion.get(i), headerFinal, nombreArchivo);
                    System.out.printf("Bloque %d guardado como %s%n", i + 1, nombreArchivo);
                }
                System.out.println("\nArchivo comprimido guardado como: output.bin");
            } else {
                System.out.println("No se generaron bloques de compresión.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


private static void descomprimirArchivo(String archivoComprimido, LZ77Compressor lz, HuffmanCompressor hf) {
    System.out.println("\nDescomprimiendo todos los bloques...");
    StringBuilder resultadoFinal = new StringBuilder();

    final int tamVentana = 16384; // debe coincidir con el usado en compresión

    int bloque = 1;
    while (bloque < 2) {
        String nombreArchivo = archivoComprimido;
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
            parteNueva = bloqueConContexto; // primer bloque
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
