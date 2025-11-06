package core;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FileManager {

    private static String headerLeido = "";
    private static String bytesLeidos = "";

    public static void writeBinaryFile(List<String> content, String header, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath); 
             DataOutputStream dos = new DataOutputStream(fos)) {
            
            dos.writeUTF(header);
            for (String byteActual : content) {
                dos.writeByte(Integer.parseInt(byteActual, 2));
            }
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo .bin: " + e);
        }
    }

    public static void writeCriptedBinaryFile(byte[] data, String filePath) {
    try (FileOutputStream fos = new FileOutputStream(filePath)) {
        fos.write(data);
    } catch (IOException e) {
        System.err.println("Error al escribir el archivo binario: " + e.getMessage());
    }
}

    public static void appendBinaryFile(List<String> content, String filePath) {
    try (FileOutputStream fos = new FileOutputStream(filePath, true);
         DataOutputStream dos = new DataOutputStream(fos)) {

        // No escribimos header aquí porque ya se escribió en el primer bloque
        for (String byteActual : content) {
            dos.writeByte(Integer.parseInt(byteActual, 2));
        }
    } catch (IOException e) {
        System.err.println("Error al agregar bloque al archivo .bin: " + e);
    }
}

    public static void readBinaryFile(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             DataInputStream dis = new DataInputStream(fis)) {
            
            headerLeido = dis.readUTF();
            StringBuilder sb = new StringBuilder();

            // Leer bytes hasta el final del archivo
            while (true) {
                try {
                    byte b = dis.readByte();
                    String binario = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                    sb.append(binario);
                } catch (EOFException e) {
                    bytesLeidos = sb.toString();
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


public static String readBinaryFileToBase64Stream(String filePath) {
    try (InputStream fis = new BufferedInputStream(new FileInputStream(filePath));
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         OutputStream b64Out = Base64.getEncoder().wrap(baos)) {

        byte[] buffer = new byte[8192];
        int read;
        while ((read = fis.read(buffer)) != -1) {
            b64Out.write(buffer, 0, read);
        }
        b64Out.close();
        return baos.toString("US-ASCII"); // Base64 es ASCII puro
    } catch (IOException e) {
        throw new RuntimeException("Error leyendo archivo binario a Base64: " + e.getMessage(), e);
    }
}


    public static List<String[]> readBinaryBlocks(String filePath) {
        List<String[]> bloques = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
            DataInputStream dis = new DataInputStream(fis)) {

            while (dis.available() > 0) {
                try {
                    String header = dis.readUTF();
                    StringBuilder sb = new StringBuilder();

                    while (true) {
                        byte b = dis.readByte();
                        sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
                        if (dis.available() == 0) break;
                    }

                    bloques.add(new String[]{ header, sb.toString() });
                } catch (EOFException e) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bloques;
    }


    
    // Lee un archivo de texto plano por bloques de tamaño fijo.
     
    public static String readPlainTextFile(String filePath, int bufferSize) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);

            char[] buffer = new char[bufferSize];
            int charsRead = reader.read(buffer, 0, bufferSize);

            if (charsRead == -1) {
                reader.close();
                return null;
            }

            return new String(buffer, 0, charsRead);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Lector UTF-8 para archivos de texto plano
    public static BufferedReader crearLectorUTF8(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            return new BufferedReader(isr);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Escritor de texto plano con UTF-8 para acentos y ñ
    public static void writePlainTextFile(String content, String filePath) {
        try (FileWriter fw = new FileWriter(filePath, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(fw)) {

            writer.write(content);
            writer.flush();

        } catch (IOException e) {
            System.err.println("Error al escribir el archivo de texto: " + e.getMessage());
        }
    }
    
    public static String getHeader() {
        return headerLeido;
    }

    public static String getBytes() {
        return bytesLeidos;
    }
}
