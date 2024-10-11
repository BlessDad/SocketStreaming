package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

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

import static application.ChatServerHandler.UserService.WriteAll;

public class ChatServerHandler implements Initializable {
	@FXML
	private TextArea chatArea;
	@FXML
	private TextField chatField;
	@FXML
	private Button sendBtn;

	private static final long serialVersionUID = 1L;

	private ServerSocket socket; // 서버소켓
	private Socket client_socket;
	private Vector<UserService> UserVec = new Vector<>();
	private static final int BUF_LEN = 128;

	private String UserName = "server";

	public VideoHandler chatVideoHandler;
	public Duration curTime;
	public String video_url;
	private String portNum;

	public ChatServerHandler(String video_url, String portNum) {
		this.video_url = video_url;
		this.portNum = portNum;
	}

	public void setVideoHandler(VideoHandler chatVideoHandler) {
		this.chatVideoHandler = chatVideoHandler;
	}

	// 사용자 접속 처리
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {
			while (true) {
				try {
					chatArea.appendText("Waiting clients ...\n");
					client_socket = socket.accept();
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user);
					chatArea.appendText("사용자 입장. 현재 참가자 수 " + UserVec.size() + "\n");
					new_user.start();
				} catch (IOException e) {
					chatArea.appendText("!!!! accept 에러 발생... !!!!");
				}
			}
		}
	}

	class UserService extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		private Socket client_socket;
		private static Vector<UserService> user_vc;
		private String UserName = "";

		public UserService(Socket client_socket) {
			this.client_socket = client_socket;
			UserService.user_vc = UserVec;

			try {
				is = client_socket.getInputStream();
				dis = new DataInputStream(is);
				os = client_socket.getOutputStream();
				dos = new DataOutputStream(os);

				// 클라이언트에게 비디오 링크 전달
				WriteOne(video_url);

				String line1 = dis.readUTF();
				String[] msg = line1.split(" ");
				UserName = msg[1].trim();
				chatArea.appendText("새로운 참가자 " + UserName + " 입장.\n");
				WriteOne("Welcome to Java Streaming server\n");
				WriteOne(UserName + "님 환영합니다.\n");

				// 새로 입장하는 클라이언트에게 재생시간 전달
				WriteOne(VideoCommand.CURRENT_TIME.getText() + " " + chatVideoHandler.getCurTime().toString());

			} catch (Exception e) {
				chatArea.appendText("userService error");
			}

		}

		// 지정 클라이언트에게 메세지 전달
		public void WriteOne(String msg) {
			try {
				dos.writeUTF(msg);
			} catch (IOException e) {
				chatArea.appendText("dos.write() error");
				try {
					dos.close();
					dis.close();
					client_socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				UserVec.removeElement(this);
				chatArea.appendText("사용자 퇴장. 현재 참가자 수 " + UserVec.size() + "\n");
			}
		}

		// 모든 클라이언트에게 순차적으로 메시지 전달
		public static void WriteAll(String str) {
			if (user_vc == null) {
				System.out.println("user_vc is null");
			} else {
				for (int i = 0; i < user_vc.size(); i++) {
					UserService user = user_vc.get(i);
					user.WriteOne(str);
				}
			}
		}

		public void run() {
			while (true) {
				try {
					String msg = dis.readUTF();
					if (msg.equals(VideoCommand.CURRENT_TIME.getText())) {
						// 클라이언트에게 현재 재생 시간 알리기
						// "/CURRENT_TIME 0.0 ms"
						msg += " " + chatVideoHandler.getCurTime();
						WriteAll(msg);
					} else if (msg.equals(VideoCommand.CURRENT_STATUS.getText())) {
						// 클라이언트에게 현재 재생 상태 알리기
						// "/CURRENT_STATUS STOPPED

						msg += " " + chatVideoHandler.getStatus();
						// if (msg.equals("/CURRENT_STATUSSTOPPED"))
						WriteAll(msg);
					} else {
						chatArea.appendText(msg);
						WriteAll(msg);
					}
				} catch (IOException e) {
					chatArea.appendText("dis.readUTF() error");
					System.out.println(e);
					try {
						dos.close();
						dis.close();
						client_socket.close();
						UserVec.removeElement(this); // 에러가 난 현재 객체를 벡터에서 지운다
						chatArea.appendText("사용자 퇴장. 남은 참가자 수 " + UserVec.size() + "\n");
						break;
					} catch (Exception ee) {
						break;
					}
				}
			}
		}
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		chatArea.setEditable(false);
		chatArea.setWrapText(true);

		// 전송 버튼을 누르거나 엔터키를 누르면 발생
		EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
			@Override
			public void handle(javafx.event.ActionEvent event) {
				String msg = null;
				msg = String.format("[%s] %s\n", UserName, chatField.getText());
				chatField.setText("");
				chatField.requestFocus();
				chatArea.appendText(msg);
				WriteAll(msg);
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

		chatField.setOnKeyPressed(keyEventHandler);
		sendBtn.setOnAction(eventHandler);
		chatField.requestFocus();

		try {
			socket = new ServerSocket(Integer.parseInt(portNum));
		} catch (NumberFormatException | IOException e1) {
			e1.printStackTrace();
		}
		chatArea.appendText("Chat Server Running..\n");
		AcceptServer accept_server = new AcceptServer();
		accept_server.start();
	}

	public void videoWrite(String msg) {
		WriteAll(msg);
	}

}
