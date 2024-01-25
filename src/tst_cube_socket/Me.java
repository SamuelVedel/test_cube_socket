package tst_cube_socket;

import java.awt.Color;
import java.awt.Graphics2D;

public class Me {
    private Play play;
    private int id;
    public boolean noMoreReasonToBe = false;

    private int x = 0, y = 0;
    private static final int WIDTH = 50, HEIGHT = WIDTH;
    private final int v = 3;

    private boolean rightPressed = false;
    private boolean leftPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

    public Me(int id, Play play) {
        this.id = id;
        this.play = play;
    }

    public void action() {
        if (rightPressed) x += v;
        if (downPressed) y += v;
        if (leftPressed) x -= v;
        if (upPressed) y -= v;
    }

    public void startMoving(int keyCode) {
        if (play.isHost()) {
            if (keyCode == 39) rightPressed = true;
            else if (keyCode == 40) downPressed = true;
            else if (keyCode == 37) leftPressed = true;
            else if (keyCode == 38) upPressed = true;
        } else {
            if (keyCode == 39) sendPressed((char)1, (char)0);
            if (keyCode == 40) sendPressed((char)1, (char)1);
            if (keyCode == 37) sendPressed((char)1, (char)2);
            if (keyCode == 38) sendPressed((char)1, (char)3);
        }
    }

    public void stopMoving(int keyCode) {
        if (play.isHost()) {
            if (keyCode == 39) rightPressed = false;
            else if (keyCode == 40) downPressed = false;
            else if (keyCode == 37) leftPressed = false;
            else if (keyCode == 38) upPressed = false;
        } else {
            if (keyCode == 39) sendPressed((char)0, (char)0);
            if (keyCode == 40) sendPressed((char)0, (char)1);
            if (keyCode == 37) sendPressed((char)0, (char)2);
            if (keyCode == 38) sendPressed((char)0, (char)3);
        }
    }

    public String writeActions() {
        String message = ""+(char)id;
        message += UsefulTh.writeIntInStr(x);
        message += UsefulTh.writeIntInStr(y);
        message += (noMoreReasonToBe? (char)1: (char)0);
        return message;
    }

    public int readActions(char[] message, int offset) {
        ++offset;
        x = UsefulTh.readIntInChars(message, offset);
        offset += UsefulTh.SIZE_OF_INT;
        y = UsefulTh.readIntInChars(message, offset);
        offset += UsefulTh.SIZE_OF_INT;
        if (message[offset] > 1) noMoreReasonToBe = true;
        ++offset;
        return offset;
    }

    private void sendPressed(char state, char i) {
        String message = ""+MessageType.OTHER.getId()+(char)id+MessageType.PRESSED_MSG.getId()+state+i;
        play.client.sendMessage(message);
    }

    public void recieveMessage(char[] message) {
        if (message[2] == MessageType.PRESSED_MSG.getId()) {
            recievePressed(message);
        }
    }

    private void recievePressed(char[] message) {
        if (message[4] == 0) rightPressed = (message[3] == 1);
        if (message[4] == 1) downPressed = (message[3] == 1);
        if (message[4] == 2) leftPressed = (message[3] == 1);
        if (message[4] == 3) upPressed = (message[3] == 1);
    }

    public void display(Graphics2D g2d) {
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x, y, WIDTH, HEIGHT);
    }
}