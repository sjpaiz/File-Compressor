package core;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Clase coherente para escritura y lectura de archivos binarios
 * usados por el algoritmo Huffman.
 * 
 * Estructura de archivo:
 *   [bits como texto "0" y "1"] // [footer en texto]
 * 
 * Ejemplo de contenido del archivo:
 *   0110101010110//A:0;B:10;C:11
 * 
 * Ambos métodos writeBinaryFile() y readBinaryFile() son compatibles entre sí.
 */
public class FileManager {

    private static String headerLeido = "";
    private static String bytesLeidos = "";
    /**
     * Escribe una cadena de texto (bits + footer) en un archivo binario.
     */
    public static void writeBinaryFile(List<String> content, String header, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath); 
            DataOutputStream dos = new DataOutputStream(fos)) {
                dos.writeUTF(header);
                for(String byteActual : content){
                    //System.out.println(byteActual);
                    dos.writeByte(Integer.parseInt(byteActual, 2));
                }
            } catch (IOException e ) {
                System.err.println("Error al escribir el archivo .bin: " + e);
            }
    }

    /**
     * Lee un archivo binario escrito con writeBinaryFile()
     * y devuelve exactamente la misma cadena de texto.
     * Devuelve algo como:
     * "0110101101010//A:0;B:10;C:11"
     */
    public static void readBinaryFile(String filePath) {
        try(FileInputStream fis = new FileInputStream(filePath);
            DataInputStream dis = new DataInputStream(fis)){
                headerLeido = dis.readUTF();
                StringBuilder sb = new StringBuilder();
                //Leemos bytes hasta que el flujo no tenga mas datos
                while(true){
                    try{
                        byte b = dis.readByte();
                        String binario = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                        sb.append(binario);
                    }catch (EOFException e){
                        bytesLeidos = sb.toString();
                        break;
                    }
                }
            }catch(FileNotFoundException e){
                System.err.println(e);
            }catch(IOException e){
                e.printStackTrace();
            }
    }

    public static String readPlainTextFile(String filePath, int bufferSize){
        try {
            // Primer paso instanciar FileInputStream
            FileInputStream fis = new FileInputStream(filePath);

            // Paso 2, Instanciar InputStreamReader usando la instancia de FileInputStream (Sirve para los caracteres con tildes y demas caracteres especiales del espa;ol)
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);

            // Paso 3, Usar un BufferedReader para lectura eficiente
            BufferedReader reader = new BufferedReader(isr);

            char[] buffer = new char[bufferSize];

            // La parte importante usamos el readeer para leer la cantidad de caracteres aceptados por el bufferSize, estos se guardan en el arreglo buffer
            int charsRead = reader.read(buffer, 0, bufferSize);

            if(charsRead == -1){
                //Esta condicional se cumple solo si ya se leyo el archivo completo
                reader.close();
                return null;
            }

            return new String(buffer, 0, charsRead);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedReader crearLectorUTF8(String filePath){
        try {
            FileInputStream fis = new FileInputStream(filePath);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);

            return new BufferedReader(isr);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }  
    }

    public static String getHeader(){
        return headerLeido;
    }

    public static String getBytes(){
        return bytesLeidos;
    }
}
