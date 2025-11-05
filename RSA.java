import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class RSA {

    public static class Clave implements Serializable {
        public BigInteger clave;
        public BigInteger n;

        public Clave(BigInteger clave, BigInteger n) {
            this.clave = clave;
            this.n = n;
        }
    }

    public static class ParClaves implements Serializable {
        public Clave publica;
        public Clave privada;

        public ParClaves(Clave publica, Clave privada) {
            this.publica = publica;
            this.privada = privada;
        }
    }

    public static ParClaves generarClaves(int bitLength) {
        BigInteger p = BigInteger.probablePrime(bitLength, new java.util.Random());
        BigInteger q = BigInteger.probablePrime(bitLength, new java.util.Random());
        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        BigInteger e = BigInteger.valueOf(65537);
        while (!phi.gcd(e).equals(BigInteger.ONE)) {
            e = e.add(BigInteger.TWO);
        }

        BigInteger d = e.modInverse(phi);
        return new ParClaves(new Clave(e, n), new Clave(d, n));
    }

    public static void guardarClave(Clave clave, String nombreArchivo) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(nombreArchivo))) {
            out.writeObject(clave);
        }
    }

    public static Clave cargarClave(String nombreArchivo) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(nombreArchivo))) {
            return (Clave) in.readObject();
        }
    }

    public static void encriptarArchivo(String entrada, String salida, Clave publica) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(entrada));
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(salida)))) {

            int tamBloque = (publica.n.bitLength() - 1) / 8; // tamaño máximo de bloque
            byte[] buffer = new byte[tamBloque];
            int bytesLeidos;

            while ((bytesLeidos = in.read(buffer)) != -1) {
                byte[] sub = new byte[bytesLeidos];
                System.arraycopy(buffer, 0, sub, 0, bytesLeidos);

                BigInteger bloque = new BigInteger(1, sub);
                BigInteger cifrado = bloque.modPow(publica.clave, publica.n);
                byte[] bytesCifrados = cifrado.toByteArray();

                // Guardar longitud y datos del bloque
                out.writeInt(bytesCifrados.length);
                out.write(bytesCifrados);
            }
        }
    }

    public static void desencriptarArchivo(String entrada, String salida, Clave privada) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(entrada)));
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(salida))) {

            while (true) {
                try {
                    int length = in.readInt();
                    byte[] bloqueCifrado = new byte[length];
                    in.readFully(bloqueCifrado);

                    BigInteger c = new BigInteger(1, bloqueCifrado);
                    BigInteger descifrado = c.modPow(privada.clave, privada.n);
                    byte[] bytesDescifrados = descifrado.toByteArray();

                    // Eliminar byte 0 inicial si aparece
                    if (bytesDescifrados.length > 1 && bytesDescifrados[0] == 0) {
                        byte[] tmp = new byte[bytesDescifrados.length - 1];
                        System.arraycopy(bytesDescifrados, 1, tmp, 0, tmp.length);
                        bytesDescifrados = tmp;
                    }

                    out.write(bytesDescifrados);
                } catch (EOFException e) {
                    break; // fin del archivo
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);

            // Cargar o generar claves 
            Clave clavePublica, clavePrivada;
            File pubFile = new File("clave.pub");
            File privFile = new File("clave.priv");

            if (pubFile.exists() && privFile.exists()) {
                clavePublica = cargarClave("clave.pub");
                clavePrivada = cargarClave("clave.priv");
                System.out.println("Claves cargadas desde archivos existentes.");
            } else {
                ParClaves claves = generarClaves(512); // usa 512 bits (mínimo)
                clavePublica = claves.publica;
                clavePrivada = claves.privada;
                guardarClave(clavePublica, "clave.pub");
                guardarClave(clavePrivada, "clave.priv");
                System.out.println("Claves generadas y guardadas");
            }

            System.out.println(" 1 = Encriptar, 2 = Desencriptar):");
            int opcion = Integer.parseInt(sc.nextLine());

            System.out.println("Ingrese nombre del archivo:");
            String archivo = sc.nextLine();

            if (opcion == 1) {
                String salida = archivo + ".enc";
                encriptarArchivo(archivo, salida, clavePublica);
                System.out.println("Archivo encriptado: " + salida);

            } else if (opcion == 2) {
                String salida = archivo.replace(".enc", ".desc");
                desencriptarArchivo(archivo, salida, clavePrivada);
                System.out.println("Archivo desencriptado: " + salida);

            } else {
                System.out.println("Opción inválida.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

