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
        System.out.println(n);
        
        while (pos < n) {
            int mejorOffset = 0;
            int mejorLongitud = 0;
            char siguienteChar = texto.charAt(pos);
            
            int inicioVentana = Math.max(0, pos - tamVentana);
            
            // Buscar la coincidencia más larga en la ventana
            int maxLongitudBusqueda = Math.min(tamBuffer, n - pos);
            
            for (int longCoincidencia = 1; longCoincidencia <= maxLongitudBusqueda; longCoincidencia++) {
                String patron = texto.substring(pos, pos + longCoincidencia);
                
                // Buscar en la ventana (desde inicioVentana hasta pos-1)
                for (int i = inicioVentana; i < pos; i++) {
                    int finBusqueda = i + longCoincidencia;
                    if (finBusqueda <= pos && texto.substring(i, finBusqueda).equals(patron)) {
                        if (longCoincidencia > mejorLongitud) {
                            mejorLongitud = longCoincidencia;
                            mejorOffset = pos - i;
                        }
                    }
                }
            }
            
            // Determinar el siguiente carácter
            if (pos + mejorLongitud < n) {
                siguienteChar = texto.charAt(pos + mejorLongitud);
            } else {
                siguienteChar = '\0'; // Fin de texto
            }
            
            comprimido.add(new Tupla(mejorOffset, mejorLongitud, siguienteChar));
            pos += mejorLongitud + 1;
            System.out.println("Un cliclo mas: " + pos);
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