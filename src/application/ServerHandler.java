package application;

import java.io.IOException;
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
import javafx.stage.Stage;

public class ServerHandler implements Initializable {

	@FXML
	private TextField portField;
	@FXML
	private TextField urlField;
	@FXML
	private Button startBtn;

	private String portNum;
	private static String video_url;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		startBtn.setOnAction(e -> {
			if (portField.getText().isEmpty())
				portNum = "30000";
			else
				portNum = portField.getText();
			if (urlField.getText().isEmpty())
				video_url = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
			else
				video_url = urlField.getText();

			try {
				FXMLLoader loader = new FXMLLoader();
				RootHandler rootHandler = new RootHandler("Host", portNum, true);

				String fxmlFile = Paths.get("src", "root.fxml").toUri().toString();
				URL url = new URL(fxmlFile);

				loader.setController(rootHandler);
				loader.setLocation(url);

				Parent rootScreen = loader.load();

				Stage stage = new Stage();
				stage.setScene(new Scene(rootScreen));
				stage.setTitle("스트리밍 서버 - 호스트");

				ChatServer.closeStage();
				stage.show();

			} catch (IOException ee) {
				ee.printStackTrace();
			}
		});

	}

	public static String GetVideoUrl() {
		return video_url;
	}

}
