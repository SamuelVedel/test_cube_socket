package tst_cube_socket;

public enum MessageType {
	HEY(0),
	WELCOME(1),
	BYE(2),
	STOP_SERVER(3),
	OTHER(4),

	PRESSED_MSG(5),
	ACTIONS_MSG(6);

	private byte by;

	private MessageType(int by) {
		this.by = (byte)by;
	}

	public byte getByte() {
		return by;
	}
}
