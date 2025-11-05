package compression;
import java.util.ArrayList;
import java.util.List;

public class LZ77 {
    public List<Tupla> comprimir(String texto, int tamVentana, int tamBuffer) {
        List<Tupla> comprimido = new ArrayList<>();

        if (texto == null || texto.isEmpty()) {
            return comprimido;
        }

        int pos = 0;
        int n = texto.length();
        char[] chars = texto.toCharArray();

        while (pos < n) {
            int mejorOffset = 0;
            int mejorLongitud = 0;
            char siguienteChar = chars[pos];

            int inicioVentana = Math.max(0, pos - tamVentana);
            int maxLongitudBusqueda = Math.min(tamBuffer, n - pos);

            // Buscar la coincidencia más larga en la ventana
            for (int i = inicioVentana; i < pos; i++) {
                int longitudCoinc = 0;

                // Comparar carácter por carácter
                while (longitudCoinc < maxLongitudBusqueda &&
                    i + longitudCoinc < pos &&
                    chars[i + longitudCoinc] == chars[pos + longitudCoinc]) {
                    longitudCoinc++;
                }

                // Actualizar mejor coincidencia encontrada
                if (longitudCoinc > mejorLongitud) {
                    mejorLongitud = longitudCoinc;
                    mejorOffset = pos - i;
                }

                // Si ya alcanzamos la longitud máxima, salir
                if (mejorLongitud == maxLongitudBusqueda) {
                    break;
                }
            }

            // Determinar el siguiente carácter
            if (pos + mejorLongitud < n) {
                siguienteChar = chars[pos + mejorLongitud];
            } else {
                siguienteChar = '\0'; // Fin del texto
            }

            comprimido.add(new Tupla(mejorOffset, mejorLongitud, siguienteChar));

            // Avanzar la posición
            pos += mejorLongitud + 1;
        }

        return comprimido;
    }

    
    public String descomprimir(List<Tupla> comprimido) {
        StringBuilder resultado = new StringBuilder();
        
        if (comprimido == null) {
            return "";
        }
        
        for (Tupla tupla : comprimido) {
            if (tupla.longitud > 0) {
                int inicio = resultado.length() - tupla.offset;
                // Copiar los caracteres que coinciden
                for (int i = 0; i < tupla.longitud; i++) {
                    if (inicio + i < resultado.length()) {
                        resultado.append(resultado.charAt(inicio + i));
                    }
                }
            }
            
            if (tupla.siguiente != '\0') {
                resultado.append(tupla.siguiente);
            }
        }
        
        return resultado.toString();
    }
    
    // Convertir lista de tuplas a string para almacenarlas
    public String tuplasAString(List<Tupla> comprimido) {
        if (comprimido == null || comprimido.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (Tupla tupla : comprimido) {
            sb.append(tupla.offset).append(",")
              .append(tupla.longitud).append(",")
              .append((int)tupla.siguiente).append(";");
        }
        return sb.toString();
    }
    
    // Convertir string a lista de tuplas
    public List<Tupla> stringATuplas(String datos) {
        List<Tupla> comprimido = new ArrayList<>();
        
        if (datos == null || datos.isEmpty()) {
            return comprimido;
        }
        
        String[] tuplas = datos.split(";");
        
        for (String tuplaStr : tuplas) {
            if (!tuplaStr.trim().isEmpty()) {
                String[] partes = tuplaStr.split(",");
                if (partes.length == 3) {
                    try {
                        int offset = Integer.parseInt(partes[0]);
                        int longitud = Integer.parseInt(partes[1]);
                        char siguiente = (char) Integer.parseInt(partes[2]);
                        comprimido.add(new Tupla(offset, longitud, siguiente));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parseando tupla: " + tuplaStr);
                    }
                }
            }
        }
        
        return comprimido;
    }
}