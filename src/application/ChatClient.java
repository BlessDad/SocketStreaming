package application;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ChatClient extends Application {
	private AnchorPane loginLayout;
	private static Stage primaryStage;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		try { 			
			String fxmlFile = Paths.get("src", "Login.fxml").toUri().toString();
			URL url = new URL(fxmlFile);
			
			LoginHandler loginHandler = new LoginHandler();

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(url);
			loader.setController(loginHandler);
			loginLayout = (AnchorPane) loader.load();

			Scene scene = new Scene(loginLayout);
			primaryStage.setTitle("스트리밍 서버 접속 - 클라이언트");
			primaryStage.setScene(scene);
			primaryStage.show();
			
			ChatClient.primaryStage = primaryStage;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void closeStage() {
		primaryStage.close();
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
