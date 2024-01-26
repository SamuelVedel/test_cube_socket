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
        if (keyCode == 39 && !rightPressed) sendPressed(0, 1);
        else if (keyCode == 40 && !downPressed) sendPressed(1, 1);
        else if (keyCode == 37 && !leftPressed) sendPressed(2, 1);
        else if (keyCode == 38 && !upPressed) sendPressed(3, 1);
        if (play.isHost()) {
            if (keyCode == 39) rightPressed = true;
            else if (keyCode == 40) downPressed = true;
            else if (keyCode == 37) leftPressed = true;
            else if (keyCode == 38) upPressed = true;
        }
    }

    public void stopMoving(int keyCode) {
        if (keyCode == 39) sendPressed(0, 0);
        else if (keyCode == 40) sendPressed(1, 0);
        else if (keyCode == 37) sendPressed(2, 0);
        else if (keyCode == 38) sendPressed(3, 0);
        //if (play.isHost()) {
            if (keyCode == 39) rightPressed = false;
            else if (keyCode == 40) downPressed = false;
            else if (keyCode == 37) leftPressed = false;
            else if (keyCode == 38) upPressed = false;
        //}
    }

    public static int getSizeOfActionsMessage() {
        return UsefulTh.SIZE_OF_INT+2*UsefulTh.SIZE_OF_INT+1;
    }

    public int writeActions(byte[] message, int offset) {
        UsefulTh.writeInt(message, offset, id);
        offset += UsefulTh.SIZE_OF_INT;
        UsefulTh.writeInt(message, offset, x);
        offset += UsefulTh.SIZE_OF_INT;
        UsefulTh.writeInt(message, offset, y);
        offset += UsefulTh.SIZE_OF_INT;
        message[offset] = (noMoreReasonToBe? (byte)1: (byte)0);
        ++offset;
        return offset;
    }

    public int readActions(byte[] message, int offset) {
        offset += UsefulTh.SIZE_OF_INT;
        x = UsefulTh.readInt(message, offset);
        offset += UsefulTh.SIZE_OF_INT;
        y = UsefulTh.readInt(message, offset);
        offset += UsefulTh.SIZE_OF_INT;
        if (message[offset] > 1) noMoreReasonToBe = true;
        ++offset;
        return offset;
    }

    private void sendPressed(int i, int state) {
        int offset = 0;
        byte[] message = new byte[1+UsefulTh.SIZE_OF_INT+2];
        message[0] = MessageType.PRESSED_MSG.getByte();
        ++offset;
        UsefulTh.writeInt(message, offset, id);
        offset += UsefulTh.SIZE_OF_INT;
        message[offset] = (byte)i;
        ++offset;
        message[offset] = (byte)state;
        //++offset;
        if (play.isHost()) play.server.sendMessageToAll(message);
        else play.client.sendMessage(message);
    }

    public void recievePressed(byte[] message) {
        if (message[1+UsefulTh.SIZE_OF_INT] == 0) rightPressed = (message[2+UsefulTh.SIZE_OF_INT] == 1);
        if (message[1+UsefulTh.SIZE_OF_INT] == 1) downPressed = (message[2+UsefulTh.SIZE_OF_INT] == 1);
        if (message[1+UsefulTh.SIZE_OF_INT] == 2) leftPressed = (message[2+UsefulTh.SIZE_OF_INT] == 1);
        if (message[1+UsefulTh.SIZE_OF_INT] == 3) upPressed = (message[2+UsefulTh.SIZE_OF_INT] == 1);
    }

    public void display(Graphics2D g2d) {
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x, y, WIDTH, HEIGHT);
    }
}