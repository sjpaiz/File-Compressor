//Archivo raiz del proyecto
import compression.Huffman;
import compression.LZ77;
import core.FileManager;
import spark.Spark;

public class Main {
    static final String cadenaInicial = "Data compression is the process of reducing the size of data to save storage space or transmission time. Huffman coding is one of the most popular methods for lossless compression. It assigns shorter binary codes to characters that appear more frequently and longer codes to those that appear less frequently. This technique is widely used in file compression formats such as ZIP, GZIP, and many image or text compression systems. Understanding Huffman coding helps in learning how information theory and efficient data representation work together to optimize digital communication and storage.";
    public static void main(String[] args) {
        Huffman hf = new Huffman();
        LZ77 lz = new LZ77();
        //String comprimido = hf.comprimir(lz.tuplasAString(lz.comprimir(cadenaInicial, 4096, 64)));
        //FileManager.writeBinaryFile(comprimido, "output.bin");
        String encode = FileManager.readBinaryFile("output.bin");
        //System.out.println(encode);
        String lol = hf.descomprimir(encode);
        System.out.println(lz.descomprimir(lz.stringATuplas(lol)));
        Spark.staticFileLocation("../resources/web/");
        Spark.get("/hello", (req, res) -> "Hello world xd");
    }
}
