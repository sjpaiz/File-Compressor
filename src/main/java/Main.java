//Archivo raiz del proyecto
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import compression.Huffman;
import compression.LZ77;
import compression.Tupla;
import core.FileManager;

public class Main {
    static final String cadenaInicial = "Data compression is the process of reducing the size of data to save storage space or transmission time. Huffman coding is one of the most popular methods for lossless compression. It assigns shorter binary codes to characters that appear more frequently and longer codes to those that appear less frequently. This technique is widely used in file compression formats such as ZIP, GZIP, and many image or text compression systems. Understanding Huffman coding helps in learning how information theory and efficient data representation work together to optimize digital communication and storage.";
    public static void main(String[] args) {
        Huffman hf = new Huffman();
        LZ77 lz = new LZ77();
        /* 
        FileManager.readBinaryFile("output.bin");
        String header = FileManager.getHeader();
        String bytesLeidos = FileManager.getBytes();
        System.out.println(lz.descomprimir(lz.stringATuplas(hf.descomprimir(bytesLeidos, header))));
        List<String> comprimido = hf.comprimir(cadenaInicial);
        System.out.println(comprimido);
        String header = hf.getHeader();
        FileManager.writeBinaryFile(comprimido, header, "output.bin"); 
        FileManager reader = new FileManager();
        reader.readBinaryFile("output.bin");
        String newHeader = reader.getHeader();
        String newBytes = reader.getBytes();
        Huffman instancia2 = new Huffman();
        System.out.println(instancia2.descomprimir(newBytes, newHeader));       
        //String encode = FileManager.readBinaryFile("output.bin");
        //System.out.println(encode);
        //String lol = hf.descomprimir(encode);
        //System.out.println(lz.descomprimir(lz.stringATuplas(lol)));
        //Spark.staticFileLocation("../resources/web/");
        //Spark.get("/hello", (req, res) -> "Hello world xd");
        */
        String testFilePath = "test3.txt";
        int chunkSizeBytes = 5120 * 5120;
        List<List<String>> bloquesCompresion = new ArrayList<>();
        String header1 = "";
        //Paso 1, Crear el lector
        try(BufferedReader reader = FileManager.crearLectorUTF8(testFilePath)){
            if(reader == null) return;
            
            char[] buffer = new char[chunkSizeBytes];
            int charsRead;
            int contadorBloques = 0;
            
            //Paso 2, leer en un bucle hasta que se acaben los caracteres
            while((charsRead = reader.read(buffer, 0, chunkSizeBytes)) != -1){
                contadorBloques++;
                String bloque = new String(buffer, 0, charsRead);
                List<Tupla> tupla = lz.comprimir(bloque,262272 , 4096);
                System.out.println(tupla);
                bloquesCompresion.add(hf.comprimir(lz.tuplasAString(tupla)));
                header1 = hf.getHeader();
                //System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
                //System.out.printf("Bloque %d ledio: %d caracteres, Contenido total: %s", contadorBloques, charsRead, bloque);
            }
            
            System.out.println(bloquesCompresion.size());
            FileManager.writeBinaryFile(bloquesCompresion.get(0), header1, "output.bin");
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
