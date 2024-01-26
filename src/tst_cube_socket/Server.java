package tst_cube_socket;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
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

	public void sendMessageTo(int id, byte[] message) {
		cvhs.get(id).sendMessage(message);
	}

	public void sendMessageToAll(byte[] message) {
		//UsefulTh.printMessage(message);
		sendMessageToOthers(message, null);
	}

	public void sendMessageToOthers(byte[] message, ConvClientHandler src) {
		for (int i = cvhs.size()-1; i >= 0; --i) {
			ConvClientHandler client = cvhs.get(i);
			if (!client.done) {
				if (client != src) client.sendMessage(message);
			} else {
				cvhs.remove(i);
			}
		}
	}

	public void sendMessageToOthers(byte[] message, int id) {
		sendMessageToOthers(message, cvhs.get(id));
	}

	public void sendMessageTo(int id, String message) {
		sendMessageTo(id, message.getBytes(StandardCharsets.UTF_8));
	}

	public void sendMessageToAll(String message) {
		sendMessageToAll(message.getBytes(StandardCharsets.UTF_8));
	}

	public void sendMessageToOthers(String message, ConvClientHandler src) {
		sendMessageToOthers(message.getBytes(StandardCharsets.UTF_8), src);
	}

	/*public void sendToHost(String message) {
		cvhs.get(0).sendMessage(message);
	}*/

	private class ConvClientHandler extends Thread {
		private Socket clientSo;
		private int id;
		private DataOutputStream out;
		private DataInputStream in;
		private boolean done = false;

		private ConvClientHandler(Socket clientSo, int id) {
			this.clientSo = clientSo;
			this.id = id;
		}

		@Override
		public void run() {
			try {
				out = new DataOutputStream(clientSo.getOutputStream());
				in = new DataInputStream(new BufferedInputStream(clientSo.getInputStream()));

				while (!stopped) {
					int length = in.readInt();
					byte[] message = new byte[length];
					in.readFully(message, 0, length);

					if (msgListener != null) msgListener.recieveMessage(-1, message);
					if (message[0] == MessageType.HEY.getByte()) {
						sendMessageToOthers(message, this);
					} else if (message[0] == MessageType.BYE.getByte()) {
						sendMessageToOthers(message, this);
					} else if (message[0] == MessageType.STOP_SERVER.getByte()) {
						sendMessageToOthers(message, this);
						stopServer();
					} else if (message[0] == MessageType.PRESSED_MSG.getByte()) {
						sendMessageToAll(message);
					}
				}

				stopClient();
			} catch (IOException e) {
				stopClient();
			}
		}

		public void stopClient() {
			if (!done) {
				try {
					out.close();
					in.close();
					clientSo.close();
					done = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void sendMessage(String message) {
			sendMessage(message.getBytes(StandardCharsets.UTF_8));
		}

		public void sendMessage(byte[] message) {
			try {
				out.writeInt(message.length);
				out.write(message);
			} catch (IOException e) {
				stopClient();
			}
		}
	}
}
