package compression;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Huffman {
    
    // Clase Nodo del árbol 
    static class Nodo implements Comparable<Nodo> {
        char caracter;
        int frecuencia;
        Nodo izquierda;
        Nodo derecha;
        
        // Constructor para hoja
        Nodo(char caracter, int frecuencia) {
            this.caracter = caracter;
            this.frecuencia = frecuencia;
        }
        
        // Constructor para nodo interno
        Nodo(int frecuencia, Nodo izquierda, Nodo derecha) {
            this.caracter = '\0';
            this.frecuencia = frecuencia;
            this.izquierda = izquierda;
            this.derecha = derecha;
        }
        
        // Para que PriorityQueue ordene por frecuencia
        public int compareTo(Nodo otro) {
            return this.frecuencia - otro.frecuencia;
        }
        
        public boolean esHoja() {
            return izquierda == null && derecha == null;
        }
    }
    
    private Map<Character, String> codigosHuffman;
    private Nodo raiz;
    
    public Huffman() {
        codigosHuffman = new HashMap<>();
    }
    
    public String comprimir(String texto) {
        if (texto == null || texto.isEmpty()) return "";
        
        // Calcular frecuencias
        Map<Character, Integer> frecuencias = calcularFrecuencias(texto);
        
        // Construir árbol de Huffman
        construirArbol(frecuencias);
        
        // Generar códigos
        generarCodigos(raiz, "");
        
        // Codificar texto
        return codificarTexto(texto);
    }

    public String descomprimir(String textoCodificado) {
        if (textoCodificado == null || textoCodificado.isEmpty() || raiz == null) return "";
        
        StringBuilder resultado = new StringBuilder();
        Nodo actual = raiz;
        
        for (char bit : textoCodificado.toCharArray()) {
            if (bit == '0') {
                actual = actual.izquierda;
            } else {
                actual = actual.derecha;
            }
            
            if (actual.esHoja()) {
                resultado.append(actual.caracter);
                actual = raiz;
            }
        }
        
        return resultado.toString();
    }
    
    private Map<Character, Integer> calcularFrecuencias(String texto) {
        Map<Character, Integer> frecuencias = new HashMap<>();
        for (char c : texto.toCharArray()) {
            frecuencias.put(c, frecuencias.getOrDefault(c, 0) + 1);
        }
        return frecuencias;
    }
    
    private void construirArbol(Map<Character, Integer> frecuencias) {
        PriorityQueue<Nodo> cola = new PriorityQueue<>();
        
        // Crear nodos hoja para cada caracter
        for (Map.Entry<Character, Integer> entry : frecuencias.entrySet()) {
            cola.offer(new Nodo(entry.getKey(), entry.getValue()));
        }
        
        // Construir árbol
        while (cola.size() > 1) {
            Nodo izquierda = cola.poll();
            Nodo derecha = cola.poll();
            Nodo padre = new Nodo(izquierda.frecuencia + derecha.frecuencia, izquierda, derecha);
            cola.offer(padre);
        }
        
        raiz = cola.poll();
    }
    
    private void generarCodigos(Nodo nodo, String codigo) {
        if (nodo.esHoja()) {
            codigosHuffman.put(nodo.caracter, codigo);
            return;
        }
        
        generarCodigos(nodo.izquierda, codigo + "0");
        generarCodigos(nodo.derecha, codigo + "1");
    }
    
    private String codificarTexto(String texto) {
        StringBuilder codificado = new StringBuilder();
        for (char c : texto.toCharArray()) {
            codificado.append(codigosHuffman.get(c));
        }
        return codificado.toString();
    }

    public Map<Character, String> getCodigosHuffman() {
        return new HashMap<>(codigosHuffman);
    }

    public void imprimirArbol() {
        if (raiz != null) {
            imprimirArbolRec(raiz, "", true);
        }
    }
    
    private void imprimirArbolRec(Nodo nodo, String prefijo, boolean esUltimo) {
        System.out.print(prefijo);
        System.out.print(esUltimo ? "└── " : "├── ");
        
        if (nodo.esHoja()) {
            System.out.println("'" + nodo.caracter + "' (" + nodo.frecuencia + ")");
        } else {
            System.out.println("* (" + nodo.frecuencia + ")");
        }
        
        String nuevoPrefijo = prefijo + (esUltimo ? "    " : "│   ");
        
        if (nodo.izquierda != null) {
            boolean ultimoDerecho = (nodo.derecha == null);
            imprimirArbolRec(nodo.izquierda, nuevoPrefijo, ultimoDerecho);
        }
        
        if (nodo.derecha != null) {
            imprimirArbolRec(nodo.derecha, nuevoPrefijo, true);
        }
    }
}