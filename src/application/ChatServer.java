package application;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ChatServer extends Application {

	private AnchorPane serverLayout;
	private static Stage primaryStage;
	
	@Override
	public void start(Stage primaryStage) throws Exception {		
		try {
			String fxmlFile = Paths.get("src", "server.fxml").toUri().toString();
			URL url = new URL(fxmlFile);
			
			ServerHandler serverHandler = new ServerHandler();

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(url);
			loader.setController(serverHandler);
			serverLayout = (AnchorPane) loader.load();

			Scene scene = new Scene(serverLayout);
			primaryStage.setTitle("스트리밍 서버 시작 - 호스트");
			primaryStage.setScene(scene);
			primaryStage.show();
			
			ChatServer.primaryStage = primaryStage;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeStage() {
		primaryStage.close();
	}
	
	public static void main(String args[]) {
		launch(args);
	}
	
}


