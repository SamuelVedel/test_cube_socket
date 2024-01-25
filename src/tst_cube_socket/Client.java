package tst_cube_socket;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client {
	private Socket clientSo;
	private DataOutputStream out;
	private DataInputStream in;

	private MessageListener msgListener = null;

	private boolean stopped = false;

	public void startConnection(String ip, int port) {
		try {
			clientSo = new Socket(ip, port);
			out = new DataOutputStream(clientSo.getOutputStream());
			in = new DataInputStream(new BufferedInputStream(clientSo.getInputStream()));

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
		sendMessage(message.getBytes(StandardCharsets.UTF_8));
	}

	public void sendMessage(byte[] message) {
		try {
			out.writeInt(message.length);
			out.write(message);
		} catch (IOException e) {
			stopConnection();
		}
	}

	public void stopConnection() {
		if (!stopped) {
			try {
				out.close();
				in.close();
				clientSo.close();
				stopped = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void recieveMessages() {
		try {
			while (!stopped) {
				int length = in.readInt();
				byte[] message = new byte[length];
				in.readFully(message, 0, length);

				if (msgListener != null) msgListener.recieveMessage(-1, message);
				if (message[0] == MessageType.STOP_SERVER.getByte()) {
					stopConnection();
					break;
				}
			}
		} catch (IOException e) {
			stopConnection();
		}
	}
}
