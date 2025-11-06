package encryption;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class RSA {

    private static final String PUBLIC_KEY_FILE = "rsa_public.key";
    private static final String PRIVATE_KEY_FILE = "rsa_private.key";
    private static final int DEFAULT_KEY_SIZE = 1024; // segura y rápida


    public static class Clave implements Serializable {
        public BigInteger valor;
        public BigInteger n;

        public Clave(BigInteger valor, BigInteger n) {
            this.valor = valor;
            this.n = n;
        }
    }

    private static Clave publicKey;
    private static Clave privateKey;


    static {
        try {
            cargarOGenerarClaves();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al inicializar RSA");
        }
    }

    private static void cargarOGenerarClaves() throws Exception {
        File pubFile = new File(PUBLIC_KEY_FILE);
        File privFile = new File(PRIVATE_KEY_FILE);

        if (pubFile.exists() && privFile.exists()) {
            publicKey = (Clave) cargarClave(PUBLIC_KEY_FILE);
            privateKey = (Clave) cargarClave(PRIVATE_KEY_FILE);
        } else {
            generarYGuardarClaves(DEFAULT_KEY_SIZE);
        }
    }

    private static void generarYGuardarClaves(int bits) throws IOException {
        BigInteger p = BigInteger.probablePrime(bits / 2, new Random());
        BigInteger q = BigInteger.probablePrime(bits / 2, new Random());
        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger e = BigInteger.valueOf(65537);
        BigInteger d = e.modInverse(phi);

        publicKey = new Clave(e, n);
        privateKey = new Clave(d, n);

        guardarClave(publicKey, PUBLIC_KEY_FILE);
        guardarClave(privateKey, PRIVATE_KEY_FILE);
    }

    private static void guardarClave(Clave clave, String ruta) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ruta))) {
            out.writeObject(clave);
        }
    }

    private static Object cargarClave(String ruta) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(ruta))) {
            return in.readObject();
        }
    }

    public static String encrypt(String texto) {
        return encryptWithKey(texto, publicKey);
    }

    public static String decrypt(String textoEncriptado) {
        return decryptWithKey(textoEncriptado, privateKey);
    }

    private static String encryptWithKey(String texto, Clave key) {
        byte[] datos = texto.getBytes(StandardCharsets.UTF_8);
        BigInteger mensaje = new BigInteger(1, datos);
        BigInteger cifrado = mensaje.modPow(key.valor, key.n);
        return Base64.getEncoder().encodeToString(cifrado.toByteArray());
    }

    private static String decryptWithKey(String base64, Clave key) {
        byte[] cifradoBytes = Base64.getDecoder().decode(base64);
        BigInteger cifrado = new BigInteger(1, cifradoBytes);
        BigInteger descifrado = cifrado.modPow(key.valor, key.n);
        return new String(descifrado.toByteArray(), StandardCharsets.UTF_8);
    }

public static String encryptFileToString(String rutaArchivo) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(rutaArchivo));
         DataOutputStream out = new DataOutputStream(new BufferedOutputStream(baos))) {

        // Header: longitud original
        File f = new File(rutaArchivo);
        long originalLength = f.length();
        out.writeLong(originalLength);

        int tamBloqueClaro = (publicKey.n.bitLength() - 1) / 8;
        int cipherLen = (publicKey.n.bitLength() + 7) / 8;
        byte[] buffer = new byte[tamBloqueClaro];
        int bytesLeidos;
        int bloqueIdx = 0;

        while ((bytesLeidos = in.read(buffer)) != -1) {
            bloqueIdx++;
            byte[] plain = Arrays.copyOf(buffer, bytesLeidos);

            BigInteger m = new BigInteger(1, plain);
            BigInteger c = m.modPow(publicKey.valor, publicKey.n);

            byte[] bytesCifrados = toFixedLength(c.toByteArray(), cipherLen);

            out.writeInt(bytesLeidos);
            out.writeInt(bytesCifrados.length);
            out.write(bytesCifrados);

        }

        out.flush();
    }

    return Base64.getEncoder().encodeToString(baos.toByteArray());
}

public static void decryptStringToFile(String contenidoEncriptado, String salida) throws IOException {
    byte[] datosCifrados = Base64.getDecoder().decode(contenidoEncriptado);

    File outFile = new File(salida);
    try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(datosCifrados));
         BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile))) {

        // Leer header (long original)
        long originalLength;
        try {
            originalLength = in.readLong();
        } catch (EOFException e) {
            throw new IOException("Archivo cifrado corrupto: falta header de longitud", e);
        }

        int tamBloqueOriginal = (publicKey.n.bitLength() - 1) / 8;
        int bloqueIdx = 0;
        long totalWritten = 0;

        while (true) {
            try {
                int plainLen = in.readInt();
                int cipherLen = in.readInt();
                byte[] bloqueCifrado = new byte[cipherLen];
                in.readFully(bloqueCifrado);

                bloqueIdx++;

                BigInteger c = new BigInteger(1, bloqueCifrado);
                BigInteger m = c.modPow(privateKey.valor, privateKey.n);
                byte[] rawDesc = m.toByteArray();

                byte[] fixedDesc = rawDesc;
                if (rawDesc.length > tamBloqueOriginal) {
                    fixedDesc = Arrays.copyOfRange(rawDesc, rawDesc.length - tamBloqueOriginal, rawDesc.length);
                } else if (rawDesc.length < tamBloqueOriginal) {
                    // rellenar a la izquierda con ceros para tener tamBloqueOriginal bytes
                    byte[] padded = new byte[tamBloqueOriginal];
                    System.arraycopy(rawDesc, 0, padded, tamBloqueOriginal - rawDesc.length, rawDesc.length);
                    fixedDesc = padded;
                }

                int start = tamBloqueOriginal - plainLen;
                if (start < 0 || plainLen < 0 || plainLen > tamBloqueOriginal) {
                    throw new IOException("Inconsistencia de longitudes en bloque descifrado: plainLen=" + plainLen + " tamBloque=" + tamBloqueOriginal);
                }

                out.write(fixedDesc, start, plainLen);
                totalWritten += plainLen;
            } catch (EOFException e) {
                break;
            }
        }

        out.flush();
    }

    try (RandomAccessFile raf = new RandomAccessFile(outFile, "rw")) {
        byte[] decoded = Base64.getDecoder().decode(contenidoEncriptado);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(decoded));
        long originalLength = dis.readLong();
        long currentLength = raf.length();
        if (originalLength < currentLength) {
            raf.setLength(originalLength);
        }
    }
}

private static byte[] toFixedLength(byte[] input, int length) {
    if (input.length == length) return input;
    byte[] result = new byte[length];
    if (input.length > length) {
        System.arraycopy(input, input.length - length, result, 0, length);
    } else {
        System.arraycopy(input, 0, result, length - input.length, input.length);
    }
    return result;
}

}
