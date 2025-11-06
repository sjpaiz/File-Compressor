package encryption;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

public class RSA {

    private static final String PUBLIC_KEY_FILE = "rsa_public.key";
    private static final String PRIVATE_KEY_FILE = "rsa_private.key";
    private static final int DEFAULT_KEY_SIZE = 1024; // segura y rápida

    // ==============================
    // 🔑 Estructuras internas
    // ==============================
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

    // ==============================
    // 🚀 Inicialización automática
    // ==============================
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

    // ======================================================
    // 🔒 ENCRIPTAR Y DESENCRIPTAR STRINGS
    // ======================================================

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

    // ======================================================
    // 📂 ENCRIPTAR ARCHIVOS Y DEVOLVER COMO STRING
    // ======================================================

    /**
     * Encripta un archivo completo y devuelve el resultado como un String Base64 listo
     * para escribir con FileManager.writeBinaryFile().
     */
    public static String encryptFileToString(String rutaArchivo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(rutaArchivo));
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(baos))) {

            int tamBloque = (publicKey.n.bitLength() - 1) / 8;
            byte[] buffer = new byte[tamBloque];
            int bytesLeidos;

            while ((bytesLeidos = in.read(buffer)) != -1) {
                byte[] bloque = new byte[bytesLeidos];
                System.arraycopy(buffer, 0, bloque, 0, bytesLeidos);

                BigInteger m = new BigInteger(1, bloque);
                BigInteger c = m.modPow(publicKey.valor, publicKey.n);
                byte[] bytesCifrados = c.toByteArray();

                out.writeInt(bytesCifrados.length);
                out.write(bytesCifrados);
            }
        }

        // Devuelve todo el archivo cifrado en Base64 (seguro para texto)
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * Desencripta el contenido previamente encriptado por encryptFileToString().
     */
    public static void decryptStringToFile(String contenidoEncriptado, String salida) throws IOException {
        byte[] datosCifrados = Base64.getDecoder().decode(contenidoEncriptado);

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(datosCifrados));
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(salida))) {

            while (true) {
                try {
                    int length = in.readInt();
                    byte[] bloqueCifrado = new byte[length];
                    in.readFully(bloqueCifrado);

                    BigInteger c = new BigInteger(1, bloqueCifrado);
                    BigInteger descifrado = c.modPow(privateKey.valor, privateKey.n);
                    byte[] bytesDescifrados = descifrado.toByteArray();

                    if (bytesDescifrados.length > 1 && bytesDescifrados[0] == 0) {
                        byte[] tmp = new byte[bytesDescifrados.length - 1];
                        System.arraycopy(bytesDescifrados, 1, tmp, 0, tmp.length);
                        bytesDescifrados = tmp;
                    }

                    out.write(bytesDescifrados);
                } catch (EOFException e) {
                    break;
                }
            }
        }
    }

}
