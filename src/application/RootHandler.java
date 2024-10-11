package application;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import application.LoginHandler.ClientInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class RootHandler implements Initializable {
	@FXML
	private BorderPane rootLayout;

	private String UserName;
	private Boolean server;
	private String video_url;

	private ChatHandler chatHandler;
	private ChatServerHandler chatServerHandler;
	private ClientInfo clientInfo;

	private VideoHandler videoHandler;

	public RootHandler(String username, Boolean server) {
		this.UserName = username;
		this.server = server;
	}
	// 클라이언트 생성자
	public RootHandler(String username, Boolean server, ClientInfo clientInfo) {
		this(username, server);
		this.clientInfo = clientInfo;
	}
	// 서버 생성자
	public RootHandler(String username, String portNum,Boolean server) {
		this(username, server);
		
		video_url = ServerHandler.GetVideoUrl();
		chatServerHandler = new ChatServerHandler(video_url, portNum);
	}

	@Override
	public void initialize(URL location, ResourceBundle resource) {
		FXMLLoader chatLoader = new FXMLLoader();
		FXMLLoader videoLoader = new FXMLLoader();

		videoHandler = new application.VideoHandler(chatServerHandler, ServerHandler.GetVideoUrl());

		// 서버 fxml 로드
		if (server) {
			try {
				// 비디오 fxml
				String fxmlVideoFile = Paths.get("src", "video.fxml").toUri().toString();
				URL urlVideo = new URL(fxmlVideoFile);
				
				videoHandler.setServer();
				chatServerHandler.setVideoHandler(videoHandler);
				videoLoader.setController(videoHandler);
				videoLoader.setLocation(urlVideo);
				
				AnchorPane videoLayout = (AnchorPane) videoLoader.load();


				// 채팅 fxml
				String fxmlChatFile = Paths.get("src", "chat.fxml").toUri().toString();
				URL urlChat = new URL(fxmlChatFile);

				chatLoader.setController(chatServerHandler);
				chatLoader.setLocation(urlChat);

				AnchorPane chatLayout = (AnchorPane) chatLoader.load();

				rootLayout.setCenter(videoLayout);
				rootLayout.setRight(chatLayout);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// 클라이언트 fxml 로드			
			try {
				chatHandler = new ChatHandler(UserName, clientInfo.GetSocket(), clientInfo.GetDis());
				
				videoHandler = new application.VideoHandler(chatServerHandler, LoginHandler.GetVideoURL(), chatHandler);
				String fxmlVideoFile = Paths.get("src", "video.fxml").toUri().toString();
				URL urlVideo = new URL(fxmlVideoFile);
				
				chatHandler.setVideoHandler(videoHandler);
				videoLoader.setController(videoHandler);
				videoLoader.setLocation(urlVideo);

				AnchorPane videoLayout = (AnchorPane) videoLoader.load();

				String fxmlChatFile = Paths.get("src", "chat.fxml").toUri().toString();
				URL urlChat = new URL(fxmlChatFile);

				chatLoader.setController(chatHandler);
				chatLoader.setLocation(urlChat);

				AnchorPane chatLayout = (AnchorPane) chatLoader.load();

				rootLayout.setCenter(videoLayout);
				rootLayout.setRight(chatLayout);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
