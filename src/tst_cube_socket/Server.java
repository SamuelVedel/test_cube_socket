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

				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					//System.out.println(inputLine);
					/*for (int i = 0; i < inputLine.length(); ++i) {
						System.out.println((int)inputLine.charAt(i));
					}*/

					if (msgListener != null) msgListener.recieveMessage(id, inputLine);
					if (inputLine.charAt(0) == MessageType.HEY.getId()) {
						sendMessageToOthers(inputLine, this);
					} else if (inputLine.charAt(0) == MessageType.BYE.getId()) {
						sendMessageToOthers(inputLine, this);
						break;
					} else if (inputLine.charAt(0) == MessageType.STOP_SERVER.getId()) {
						sendMessageToOthers(inputLine, this);
						stopServer();
					} /*else {
						if (id == 0) sendMessageToOthers(inputLine, this);
						else sendToHost(inputLine);

					}*/
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
			UsefulTh.printMessage(message);
			out.println(message);
		}
	}
}
