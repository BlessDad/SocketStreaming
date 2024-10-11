package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginHandler implements Initializable {
	@FXML
	private Text statusText;
	@FXML
	private TextField nameField;
	@FXML
	private TextField ipField;
	@FXML
	private TextField portField;
	@FXML
	private Button loginBtn;

	private String userName;
	private String ip_address;
	private String portNum;

	private Socket socket;
	private InputStream is;
	private DataInputStream dis;

	public static String video_url;
	public ClientInfo clientInfo;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		loginBtn.setOnAction(e -> {
			if (ipField.getText().isEmpty())
				ip_address = "127.0.0.1";
			else
				ip_address = ipField.getText();
			if (portField.getText().isEmpty())
				portNum = "30000";
			else
				portNum = portField.getText();
			if (nameField.getText().isEmpty())
				userName = "user";
			else
				userName = nameField.getText();

			try {
				// statusText.setText("접속 시도 중 .. ");
				socket = new Socket(ip_address, Integer.parseInt(portNum));

				is = socket.getInputStream();
				dis = new DataInputStream(is);

				video_url = dis.readUTF();

				clientInfo = new ClientInfo(socket, is, dis);

			} catch (IOException ee) {
				ee.printStackTrace();
			}

			try {
				FXMLLoader loader = new FXMLLoader();
				RootHandler rootHandler = new RootHandler(userName, false, clientInfo);

				String fxmlFile = Paths.get("src", "root.fxml").toUri().toString();
				URL url = new URL(fxmlFile);

				loader.setController(rootHandler);
				loader.setLocation(url);

				Parent rootScreen = loader.load();

				Stage stage = new Stage();
				stage.setScene(new Scene(rootScreen));
				stage.setTitle("스트리밍 서버 - 클라이언트");

				ChatClient.closeStage();
				stage.show();

			} catch (IOException ee) {
				ee.printStackTrace();
			}

		});

	}

	class ClientInfo {
		private Socket socket;
		private InputStream is;
		private DataInputStream dis;

		public ClientInfo(Socket s, InputStream is, DataInputStream dis) {
			this.socket = s;
			this.is = is;
			this.dis = dis;
		}

		public Socket GetSocket() {
			return socket;
		}

		public DataInputStream GetDis() {
			return dis;
		}

		public InputStream GetIs() {
			return is;
		}
	}

	public static String GetVideoURL() {
		return video_url;
	}

}
