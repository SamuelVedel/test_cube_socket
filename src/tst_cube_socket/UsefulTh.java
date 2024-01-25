package tst_cube_socket;

public abstract class UsefulTh {

    public static final int SIZE_OF_INT = 4;

    public static void writeInt(byte[] bytes, int offset, int value) {
        for (int i = 0; i < SIZE_OF_INT; ++i) {
            bytes[offset+i] = (byte)(value&0xFF);
            value >>= 8;
        }
    }

    public static int readInt(byte[] bytes) {
        return readInt(bytes, 0);
    }

    public static int readInt(byte[] bytes, int offset) {
        int value = 0;
        for (int i = SIZE_OF_INT-1; i >= 0; --i) {
            value <<= 8;
            byte b = bytes[offset+i];
            if (b >= 0) value += b;
            else value += (-(int)b)^0xFF-1;
        }
        return value;
    }

    public static void printMessage(byte[] message) {
        printMessage(message, "\n");
    }

    public static void printMessage(byte[] message, String prefix) {
        System.out.print("{");
        for (int i = 0; i < message.length; ++i) {
            if (i > 0) System.out.print(", ");
            System.out.print(message[i]);
        }
        System.out.print("}"+prefix);
    }

    public static void printMessage(String message) {
        printMessage(message, "\n");
    }

    public static void printMessage(String message, String prefix) {
        System.out.print("{");
        for (int i = 0; i < message.length(); ++i) {
            if (i > 0) System.out.print(", ");
            System.out.print((int)message.charAt(i));
        }
        System.out.print("}"+prefix);
    }
}