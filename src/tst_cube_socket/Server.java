package tst_cube_socket;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

class Server {
	private ServerSocket serverSo;

	private ArrayList<ConvClientHandler> cvhs = new ArrayList<>();

	private MessageListener msgListener = null;

	private boolean stopped = false;

	public void start(int port) {
		new Thread() {
			@Override
			public void run() {
				try {
					serverSo = new ServerSocket(port);
					while (!stopped) {
						cvhs.add(new ConvClientHandler(serverSo.accept(), cvhs.size()));
						cvhs.get(cvhs.size()-1).start();
					}
				} catch (IOException e) {
					if (!stopped) stopServer();
				}
			}
		}.start();
	}

	public void setMessageListener(MessageListener msgListener) {
		this.msgListener = msgListener;
	}

	public void stopServer() {
		try {
			serverSo.close();
			stopped = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessageToAll(String message) {
		//UsefulTh.printMessage(message);
		sendMessageToOthers(message, null);
	}

	public void sendMessageToOthers(String message, ConvClientHandler src) {
		for (int i = cvhs.size()-1; i >= 0; --i) {
			ConvClientHandler client = cvhs.get(i);
			if (!client.done) {
				if (client != src) client.sendMessage(message);
			} else {
				cvhs.remove(i);
			}
		}
	}

	public void sendMessageTo(int id, String message) {
		cvhs.get(id).sendMessage(message);
	}

	/*public void sendToHost(String message) {
		cvhs.get(0).sendMessage(message);
	}*/

	private class ConvClientHandler extends Thread {
		private Socket clientSo;
		private int id;
		private PrintWriter out;
		private BufferedReader in;
		private boolean done = false;

		private ConvClientHandler(Socket clientSo, int id) {
			this.clientSo = clientSo;
			this.id = id;
		}

		@Override
		public void run() {
			try {
				out = new PrintWriter(clientSo.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSo.getInputStream()));

				while (!stopped) {
				char[] sizeStr = new char[UsefulTh.SIZE_OF_INT];
				in.read(sizeStr, 0, UsefulTh.SIZE_OF_INT);
				int size = UsefulTh.readIntInChars(sizeStr);
				char[] message = new char[size];
				in.read(message, 0, size);

				if (msgListener != null) msgListener.recieveMessage(-1, message);
				if (message[0] == MessageType.HEY.getId()) {
					sendMessageToOthers(UsefulTh.charsToStr(message), this);
				} else if (message[0] == MessageType.BYE.getId()) {
					sendMessageToOthers(UsefulTh.charsToStr(message), this);
				} else if (message[0] == MessageType.STOP_SERVER.getId()) {
					sendMessageToOthers(UsefulTh.charsToStr(message), this);
					stopServer();
				}
			}

				stopClient();
			} catch (IOException e) {
				if (!done) stopClient();
			}
		}

		public void stopClient() {
			try {
				out.close();
				in.close();
				clientSo.close();
				done = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void sendMessage(String message) {
			message = UsefulTh.writeIntInStr(message.length())+message;
			UsefulTh.printMessage(message);
			out.print(message);
		}
	}
}
