package compression;

public class Tupla {
        int offset;
        int longitud;
        char siguiente;
        
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