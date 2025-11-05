//Archivo raiz del proyecto
import compression.Huffman;
import core.FileManager;

public class Main {
    static final String cadenaInicial = "Data compression is the process of reducing the size of data to save storage space or transmission time. Huffman coding is one of the most popular methods for lossless compression. It assigns shorter binary codes to characters that appear more frequently and longer codes to those that appear less frequently. This technique is widely used in file compression formats such as ZIP, GZIP, and many image or text compression systems. Understanding Huffman coding helps in learning how information theory and efficient data representation work together to optimize digital communication and storage.";
    public static void main(String[] args) {
        /* 
        Huffman hf = new Huffman();
        LZ77 lz = new LZ77();
        List<String> comprimido = hf.comprimir(cadenaInicial);
        System.out.println(comprimido);
        String header = hf.getHeader();
        FileManager.writeBinaryFile(comprimido, header, "output.bin"); 
        */
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
    }
}
