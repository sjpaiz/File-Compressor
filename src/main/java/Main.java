//Archivo raiz del proyecto
import java.util.ArrayList;
import java.util.List;

import compression.Huffman;
import compression.LZ77;
import compression.Tupla;
import spark.Spark;

public class Main {
    static final String cadenaInicial = "Data compression is the process of reducing the size of data to save storage space or transmission time. Huffman coding is one of the most popular methods for lossless compression. It assigns shorter binary codes to characters that appear more frequently and longer codes to those that appear less frequently. This technique is widely used in file compression formats such as ZIP, GZIP, and many image or text compression systems. Understanding Huffman coding helps in learning how information theory and efficient data representation work together to optimize digital communication and storage.";
    public static void main(String[] args) {
        List<String> tuplasFinales = new ArrayList<>();
        Huffman hf = new Huffman();
        LZ77 lz = new LZ77();
        System.out.println("Cadena Inicial: \n" + cadenaInicial);
        List<Tupla> cadena1 = lz.comprimir(cadenaInicial, 1024, 32);
        System.out.println("Cadena comprimida con LZ77: \n" + cadena1);
        for(Tupla cadenas: cadena1){
            tuplasFinales.add(hf.comprimir(cadenas.toString()));
        }
        System.out.println("Cadena comprimida con Huffman: \n" + tuplasFinales);
        System.out.println("Iniciando procesamiento inverso: " );
        System.out.println("Tuplas decodificadas de huffman a LZ77: ");
        List<Tupla> nuevasTuplas = new ArrayList();
        StringBuilder sb = new StringBuilder();
        for(String tupla: tuplasFinales){
            System.out.println(lz.stringATuplas(hf.descomprimir(tupla)));
        }
        //System.out.println(futurasTuplas);
        Spark.staticFileLocation("../resources/web/");
        Spark.get("/hello", (req, res) -> "Hello world xd");
    }
}
