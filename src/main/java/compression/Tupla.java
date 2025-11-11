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
        public int getOffset() {
        return offset;
    }

    public int getLength() {
        return longitud;
    }

    public char getNextChar() {
        return siguiente;
    }
        @Override
        public String toString() {
            return "(" + offset + "," + longitud + "," + siguiente + ")";
        }
    }