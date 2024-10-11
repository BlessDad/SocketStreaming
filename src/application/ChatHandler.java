package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

public class ChatHandler implements Initializable {
	@FXML
	private TextArea chatArea;
	@FXML
	private TextField chatField;
	@FXML
	private Button sendBtn;

	private String UserName;
	private static final int BUF_LEN = 128;
	private Socket socket;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

	public VideoHandler chatVideoHandler;

	public ChatHandler(String username, Socket s, DataInputStream dis) {
		this.UserName = username;
		this.socket = s;
		this.dis = dis;
	}

	public void setVideoHandler(VideoHandler chatVideoHandler) {
		this.chatVideoHandler = chatVideoHandler;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		chatArea.setEditable(false);
		chatArea.setWrapText(true);

		// 전송 버튼을 누르거나 엔터키를 누르면 발생
		EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				String msg = null;
				msg = String.format("[%s] %s\n", UserName, chatField.getText());
				chatField.setText("");
				chatField.requestFocus();
				SendMessage(msg);
			}
		};

		// 엔터키 입력 처리하는 핸들러
		EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				if (keyEvent.getCode() == KeyCode.ENTER) {
					// 엔터키 입력 시 이벤트 핸들러 호출
					eventHandler.handle(new ActionEvent());
				}
			}
		};

		// 이벤트 핸들러 등록
		chatField.setOnKeyPressed(keyEventHandler);
		sendBtn.setOnAction(eventHandler);
		chatField.requestFocus();

		try {
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);

			SendMessage("/login " + UserName);

			ListenNetwork net = new ListenNetwork();
			net.start();

		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			chatArea.appendText("connect error");
		}
	}

	// 메세지 수신
	class ListenNetwork extends Thread {
		public void run() {
			try {
				while (true) {
					String msg = dis.readUTF();

					if (msg.trim().equalsIgnoreCase(VideoCommand.PLAY.getText())) {
						getVideoMessage(msg);
					} else if (msg.trim().equalsIgnoreCase(VideoCommand.PAUSE.getText())) {
						getVideoMessage(msg);
					} else if (msg.trim().equalsIgnoreCase(VideoCommand.STOP.getText())) {
						getVideoMessage(msg);
					} else if (msg.split(" ")[0].equalsIgnoreCase(VideoCommand.CURRENT_TIME.getText())) {
						// 비디오 시간 설정
						setVideoTime(msg);
					} else if (msg.split(" ")[0].equalsIgnoreCase(VideoCommand.CURRENT_STATUS.getText())) {
						// 비디오 상태 받기
						setVideoStatus(msg);
					} else {
						try {
							chatArea.appendText(msg);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	// 메세지 전송
	public void SendMessage(String msg) {
		try {
			dos.writeUTF(msg);

		} catch (IOException e) {
			chatArea.appendText("dos.write() error");
			try {
				dos.close();
				dis.close();
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}

	public void getVideoMessage(String msg) {
		System.out.println(msg);
		switch (msg.trim()) {
		case "/PLAY":
			this.chatVideoHandler.mediaPlayer.play();
			break;
		case "/PAUSE":
			this.chatVideoHandler.mediaPlayer.pause();
			break;

		case "/STOP":
			this.chatVideoHandler.mediaPlayer.stop();
			break;
		}

	}

	public void setVideoTime(String msg) {
		String[] msgs = msg.split(" ");
		String timeString = "";

		timeString = msgs[1] + msgs[2];

		System.out.println(timeString);

		// 받은 메세지에서 태그 제거한 timeString을 Duration으로 변환
		Duration curTime = Duration.valueOf(timeString);
		this.chatVideoHandler.setCurTime(curTime);
	}

	public void setVideoStatus(String msg) {
		String[] msgs = msg.split(" ");
		String statusString = "";

		statusString = msgs[1];

		this.chatVideoHandler.setClientStatus(statusString);
		

	}

}
