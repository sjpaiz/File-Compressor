package compression;

public class Tupla {
        public int offset;
        public int longitud;
        public char siguiente;
        
        Tupla(int offset, int longitud, char siguiente) {
            this.offset = offset;
            this.longitud = longitud;
            this.siguiente = siguiente;
        }
        
        @Override
        public String toString() {
            return "(" + offset + "," + longitud + "," + siguiente + ")";
        }
    }