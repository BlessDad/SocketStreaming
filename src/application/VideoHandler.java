package application;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class VideoHandler implements Initializable {
	@FXML
	private MediaView mediaView;
	@FXML
	private ImageView imageView;
	@FXML
	private Button playBtn;
	@FXML
	private Button pauseBtn;
	@FXML
	private Button stopBtn;
	@FXML
	private Button muteBtn;
	@FXML
	private Slider volumeSlider;

	public MediaPlayer mediaPlayer;

	private boolean booEnd;
	private ChatHandler chatHandler;
	private ChatServerHandler chatServerHandler;
	private String video_url;

	private boolean isServer = false;

	private Duration curTime;
	private VideoStatus curStatus;

	// 생성하면서 확인
	public VideoHandler(application.ChatServerHandler chatServerHandler, String url) {
		this.chatServerHandler = chatServerHandler;
		video_url = url;
	}

	public VideoHandler(application.ChatServerHandler chatServerHandler, String url,
			application.ChatHandler chatHandler) {
		this(chatServerHandler, url);
		this.chatHandler = chatHandler;
	}

	public void setServer() {
		isServer = true;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Media media = new Media(video_url);
		if (isServer) {
			System.out.println("server");
		} else
			System.out.println("client");
		try {
			mediaPlayer = new MediaPlayer(media);
			mediaView.setMediaPlayer(mediaPlayer);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 볼륨 조절
		volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			mediaPlayer.setVolume(newValue.doubleValue() / 100.0);
		});

		muteBtn.setOnAction(e -> {
			mediaPlayer.setVolume(0);
		});

		mediaPlayer.setOnReady(new Runnable() {
			@Override
			public void run() {

				if (isServer) {

					setStatus(VideoStatus.STOPPED);
					tellServer(VideoCommand.CURRENT_STATUS);

					playBtn.setDisable(false);
					pauseBtn.setDisable(true);
					stopBtn.setDisable(true);

				}

				else {

					requestToServer(VideoCommand.CURRENT_STATUS);
					requestToServer(VideoCommand.CURRENT_TIME);
					mediaPlayer.seek(curTime);
					if (curStatus == VideoStatus.STOPPED) {
						playBtn.setDisable(true);
					} else
						playBtn.setDisable(false);
					pauseBtn.setDisable(true);
					stopBtn.setDisable(true);
				}

			}
		});

		mediaPlayer.setOnPlaying(() -> {
			if (isServer) {
				playBtn.setDisable(true);
				pauseBtn.setDisable(false);
				stopBtn.setDisable(false);
			} else {
				// 영상 재생할 때 현재 시간으로 맞춤
				requestToServer(VideoCommand.CURRENT_TIME);
				requestToServer(VideoCommand.CURRENT_STATUS);

				mediaPlayer.seek(curTime);
				playBtn.setDisable(true);
				pauseBtn.setDisable(false);
				stopBtn.setDisable(true);
			}
		});

		mediaPlayer.setOnPaused(() -> {
			if (isServer) {
				playBtn.setDisable(false);
				pauseBtn.setDisable(true);
				stopBtn.setDisable(false);
			} else {
				requestToServer(VideoCommand.CURRENT_TIME);
				requestToServer(VideoCommand.CURRENT_STATUS);

				playBtn.setDisable(false);
				pauseBtn.setDisable(true);
				stopBtn.setDisable(true);
			}
		});

		mediaPlayer.setOnEndOfMedia(() -> {
			if (isServer) {
				booEnd = true;
				playBtn.setDisable(false);
				pauseBtn.setDisable(true);
				stopBtn.setDisable(true);
			}
		});
		mediaPlayer.setOnStopped(() -> {
			if (isServer) {
				mediaPlayer.seek(mediaPlayer.getStartTime());
			}
			playBtn.setDisable(false);
			pauseBtn.setDisable(true);
			stopBtn.setDisable(true);

			// curTime 초기화
			curTime = mediaPlayer.getStartTime();
		});

		// 플레이 버튼
		playBtn.setOnAction(event -> {
			if (isServer) {
				// 서버
				if (booEnd) {
					// 만약 재생바가 끝까지 갔다면 처음으로 돌린다.
					mediaPlayer.stop();
					mediaPlayer.seek(mediaPlayer.getStartTime());
				}
				mediaPlayer.play();
				booEnd = false;

				// 상태 변경 및 전달
				setStatus(VideoStatus.PLAYING);
				tellServer(VideoCommand.CURRENT_STATUS);

				// 클라이언트 동영상 실행
				tellServer(VideoCommand.PLAY);
			} else {
				// 클라이언트
				// 서버에게 현재 시간과 상태 요청
				requestToServer(VideoCommand.CURRENT_TIME);
				// requestToServer(VideoCommand.CURRENT_STATUS);

				// 서버의 현재 상태가 재생중일 때만 재생 가능
				if (curStatus != VideoStatus.STOPPED) {
					mediaPlayer.seek(curTime);
					mediaPlayer.play();
				}
			}

		});
		// 각각의 버튼을 선택 했을 때의 기능을 설정 하는 부분
		pauseBtn.setOnAction(event -> {
			if (isServer) {
				// 상태 변경 및 전달
				setStatus(VideoStatus.STOPPED);
				tellServer(VideoCommand.CURRENT_STATUS);
				// 클라이언트 영상 pause
				tellServer(VideoCommand.PAUSE);
				mediaPlayer.pause();

			} else {
				mediaPlayer.pause();
				if (curStatus == application.VideoStatus.STOPPED)
					this.playBtn.setDisable(true);
			}

		});

		// 멈춤 버튼
		stopBtn.setOnAction(event -> {
			mediaPlayer.stop();

			// 클라이언트 영상 멈춤
			tellServer(VideoCommand.STOP);

			// 멈춤으로 서버 상태 변경 및 전달
			setStatus(VideoStatus.STOPPED);
			tellServer(VideoCommand.CURRENT_STATUS);
		});

	}

	public Duration getCurTime() {
		return mediaPlayer.getCurrentTime();
	}

	public void setCurTime(Duration curTime) {
		// for client
		this.curTime = curTime;
	}

	private void setStatus(VideoStatus status) {
		curStatus = status;
	}

	public void setClientStatus(String status) {
		VideoStatus parseStatus = VideoStatus.valueOf(status);
		curStatus = parseStatus;
	}

	public String getStatus() {
		return curStatus.name();
	}

	private void tellServer(VideoCommand msg) {
		if (msg.equals(VideoCommand.CURRENT_STATUS)) {
			String statusMsg = msg.getText();
			statusMsg += " " + getStatus();
			this.chatServerHandler.videoWrite(statusMsg);
		} else {
			this.chatServerHandler.videoWrite(msg.getText());
		}
	}

	// 서버에게 현재 시간 요청 보냄
	private void requestToServer(VideoCommand msg) {
		this.chatHandler.SendMessage(msg.getText());
	}

}
