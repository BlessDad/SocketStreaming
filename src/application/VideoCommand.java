package application;

public enum VideoCommand {
	PLAY("/PLAY"), STOP("/STOP"), PAUSE("/PAUSE"), CURRENT_TIME("/CURRENT_TIME"), CURRENT_STATUS("/CURRENT_STATUS");

	private final String text;

	VideoCommand(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
}
