package tst_cube_socket;

public class Main {

    public static void main(String[] args) {
        if (args.length >= 2) {
            new Play(args[0], Integer.parseInt(args[1]));
        } else {
            new Play(Integer.parseInt(args[0]));
        }
    }

}