package tst_cube_socket;

public abstract class UsefulTh {

    public static final int SIZE_OF_INT = 4;

    public static String writeInt(int value) {
        String str = "";
        for (int i = 0; i < SIZE_OF_INT; ++i) {
            str += (char)(value&0xFF);
            value >>= 8;
        }
        return str;
    }

    public static int readInt(String str, int offset) {
        int value = 0;
        for (int i = 0; i < SIZE_OF_INT; ++i) {
            value += (int)str.charAt(offset+i);
            value <<= 8;
        }
        return value;
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