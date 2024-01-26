package tst_cube_socket;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JFrame;

public class Play {
	Toolkit toolkit = Toolkit.getDefaultToolkit();

	public Server server = null;
	public Client client;
	private boolean host;

	private VFrame vf = new VFrame();

	private ArrayList<Me> mes = new ArrayList<>();
	private int id = -1;
	private byte[] actionsMessage;

	private boolean ready = false;

	private MessageListener msgListener = new MessageListener() {

		@Override
		public void recieveMessage(int id, byte[] message) {
			if (message[0] == MessageType.HEY.getByte()) {
				addAMe();
			} else if (message[0] == MessageType.BYE.getByte()) {
				mes.get(id).noMoreReasonToBe = true;
			} else if (message[0] == MessageType.PRESSED_MSG.getByte()) {
				int i = UsefulTh.readInt(message, 1);
				mes.get(i).recievePressed(message);
			}

			if (isHost()) {
				if (message[0] == MessageType.HEY.getByte()) {
					server.sendMessageTo(mes.size()-2, ""+(char)MessageType.WELCOME.getByte()+(char)(mes.size()-2));
				}
			} else {
				if (message[0] == MessageType.ACTIONS_MSG.getByte() && ready) {
					actionsMessage = message;
				} else if (message[0] == MessageType.WELCOME.getByte()) {
					setId(message[1]+1);
					for (int i = 0; i <= getId(); ++i) {
						addAMe();
					}
					ready = true;
				} if (message[0] == MessageType.STOP_SERVER.getByte()) {
					stop();
				}
			}
		}
	};

	private KeyListener kl = new KeyListener() {
		@Override
		public void keyPressed(KeyEvent e) {
			if (id >= 0) mes.get(id).startMoving(e.getKeyCode());
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (id >= 0) mes.get(id).stopMoving(e.getKeyCode());
		}

		@Override
		public void keyTyped(KeyEvent e) {}
	};

	WindowAdapter winAd = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			stop();
		}
	};

	public Play(int port) {
		host = true;
		server = new Server();
		id = 0;
		server.start(port);
		server.setMessageListener(msgListener);

		/*client = new Client();
		client.setMessageListener(msgListener);
		client.startConnection("127.0.0.1", port);*/
		initJF();
		actions();
	}

	public Play(String ip, int port) {
		client = new Client();
		client.setMessageListener(msgListener);
		client.startConnection(ip, port);
		client.sendMessage(""+(char)MessageType.HEY.getByte());
		initJF();
		actions();
	}

	private void addAMe() {
		mes.add(new Me(mes.size(), this));
	}

	private int getId() {
		return id;
	}

	private void setId(int id) {
		this.id = id;
	}

	private void readActions(byte[] message) {
		int offset = 1;
		while (offset < message.length) {
			int i = UsefulTh.readInt(message, offset);
			offset = mes.get(i).readActions(message, offset);
		}
	}

	private void initJF() {
		//vf.setFullScreen(true);
		//vf.setFullScreen(false);
		vf.addKeyListener(kl);
		vf.addWindowListener(winAd);
		vf.setSize(800, 600);
		vf.setLocationRelativeTo(null);
		vf.setContentPane(new PlayPainter());
		vf.setVisible(true);
	}

	private int getSizeOfActionsMessage() {
		return 1+mes.size()*Me.getSizeOfActionsMessage();
	}

	private void actions() {
		if (isHost()) {
			mes.add(new Me(mes.size(), this));
			ready = true;
		}

		while (!ready);

		int time = 0;
		while (true) {
			boolean sendActions = false;
			if (isHost()) {
				//sendActions = time%5 == 0;
				sendActions = true;
			}

			byte[] message = new byte[getSizeOfActionsMessage()];;
			if (sendActions) message[0] = MessageType.ACTIONS_MSG.getByte();
			int offset = 1;

			for (int i = mes.size()-1; i >= 0; --i) {
				mes.get(i).action();
				if (sendActions) offset = mes.get(i).writeActions(message, offset);
				if (mes.get(i).noMoreReasonToBe) {
					mes.remove(i);
				}
			}
			if (sendActions) server.sendMessageToAll(message);

			if (actionsMessage != null) {
				readActions(actionsMessage);
				actionsMessage = null;
			}

			vf.repaint();
			toolkit.sync();
			++time;
			try {
				Thread.sleep(1000/60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void stop() {
		if (isHost()) {
			server.stopServer();
			byte[] message = {MessageType.STOP_SERVER.getByte()};
			server.sendMessageToAll(message);
		}
		else client.stopConnection();
		/*try {
			Thread.sleep(1000/30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		System.exit(0);
	}

	public boolean isHost() {
		return host;
	}

	private class PlayPainter extends JPanel {
		private static final long serialVersionUID = -7369176626068358885L;

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;

			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, getWidth(), getHeight());

			for (int i = mes.size()-1; i >= 0; --i) {
				mes.get(i).display(g2d);
			}

			g2d.dispose();
		}
	}
}