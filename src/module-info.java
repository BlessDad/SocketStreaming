module streamingTest5 {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.media;
	requires javafx.graphics;
	requires java.desktop;
	requires javafx.base;
	
	opens application to javafx.graphics, javafx.fxml;
}
