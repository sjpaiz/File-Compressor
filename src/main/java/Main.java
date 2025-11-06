import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import compression.HuffmanCompressor;
import compression.LZ77Compressor;
import compression.Tupla;
import encryption.RSA;
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

    // ============================================================
    // MÉTODO PARA COMPRIMIR ARCHIVO
    // ============================================================
    private static void comprimirArchivo(String rutaArchivo, LZ77Compressor lz, HuffmanCompressor hf) {
        final int chunkSizeBytes = 5120 * 5120; // 25MB aprox.
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

            System.out.println("\n🗜️ Iniciando compresión...");
            while ((charsRead = reader.read(buffer, 0, chunkSizeBytes)) != -1) {
                contadorBloques++;
                String bloque = new String(buffer, 0, charsRead);

                // --- LZ77 ---
                List<Tupla> tuplas = lz.comprimir(bloque, 8192, 64);
                String tuplasStr = lz.tuplasAString(tuplas);

                // --- Huffman ---
                List<String> bloqueComprimido = hf.comprimir(tuplasStr);
                headerFinal = hf.getHeader();

                bloquesCompresion.add(bloqueComprimido);
                System.out.printf("✅ Bloque %d comprimido (%d caracteres leídos)%n", contadorBloques, charsRead);
            }

            // Guardar solo el primer bloque (puedes ampliar a todos si lo deseas)
            if (!bloquesCompresion.isEmpty()) {
                FileManager.writeBinaryFile(bloquesCompresion.get(0), headerFinal, "output.bin");
                System.out.println("\n📦 Archivo comprimido guardado como: output.bin");
            } else {
                System.out.println("No se generaron bloques de compresión.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // MÉTODO PARA DESCOMPRIMIR ARCHIVO
    // ============================================================
    private static void descomprimirArchivo(String archivoComprimido, LZ77Compressor lz, HuffmanCompressor hf) {
        try {
            System.out.println("\n🔍 Leyendo archivo comprimido...");
            FileManager.readBinaryFile(archivoComprimido);

            String header = FileManager.getHeader();
            String bytesLeidos = FileManager.getBytes();

            System.out.println("🧩 Descomprimiendo Huffman...");
            String datosDescomprimidos = hf.descomprimir(bytesLeidos, header);

            System.out.println("🧩 Descomprimiendo LZ77...");
            String resultadoFinal = lz.descomprimir(lz.stringATuplas(datosDescomprimidos));
            FileManager.writePlainTextFile(resultadoFinal, "descomprimido.txt");
            System.out.println("\n📜 Resultado de la descompresión:");
            System.out.println("-------------------------------------------------------------");
            System.out.println(resultadoFinal.substring(0, Math.min(1000, resultadoFinal.length())) + "...");
            System.out.println("-------------------------------------------------------------");
            System.out.println("✅ Descompresión completada con éxito.");

        } catch (Exception e) {
            System.err.println("❌ Error durante la descompresión:");
            e.printStackTrace();
        }
    }
}
