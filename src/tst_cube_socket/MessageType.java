package tst_cube_socket;

public enum MessageType {
	HEY((char)0),
	WELCOME((char)1),
	BYE((char)2),
	STOP_SERVER((char)3),
	OTHER((char)4),

	PRESSED_MSG((char)5),
	ACTIONS_MSG((char)6);

	private char id;

	private MessageType(char id) {
		this.id = id;
	}

	public char getId() {
		return id;
	}
}
