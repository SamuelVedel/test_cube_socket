package tst_cube_socket;

import java.io.*;
import java.net.*;

public class Client {
	private Socket clientSo;
	private PrintWriter out;
	private BufferedReader in;

	private MessageListener msgListener = null;

	private boolean stopped = false;

	public void startConnection(String ip, int port) {
		try {
			clientSo = new Socket(ip, port);
			out = new PrintWriter(clientSo.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSo.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		new Thread() {
			@Override
			public void run() {
				recieveMessages();
			}
		}.start();
	}

	public void setMessageListener(MessageListener msgListener) {
		this.msgListener = msgListener;
	}
	
	public void sendMessage(String message) {
		message = UsefulTh.writeIntInStr(message.length())+message;
		//System.out.print("hey:"+message);
		out.print(message);
	}

	public void stopConnection() {
		out.close();
		try {
			in.close();
			clientSo.close();
			stopped = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void recieveMessages() {
		try {
			while (!stopped) {
				System.out.println("hey");
				char[] sizeStr = new char[UsefulTh.SIZE_OF_INT];
				in.read(sizeStr, 0, UsefulTh.SIZE_OF_INT);
				int size = UsefulTh.readIntInChars(sizeStr);
				char[] message = new char[size];
				in.read(message, 0, size);

				UsefulTh.printMessage(message);
				if (msgListener != null) msgListener.recieveMessage(-1, message);
				if (message[0] == MessageType.STOP_SERVER.getId()) {
					stopConnection();
					break;
				}
			}
		} catch (IOException e) {
			if (!stopped) stopConnection();
		}
	}
}
