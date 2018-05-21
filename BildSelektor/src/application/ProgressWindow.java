package application;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ProgressWindow extends Stage {

	private ProgressBar bar;
	private Label labelTime;
	private Label labelMessage;
	private int secondsRemaining = 20;
	private double lastProgress = 0.0;
	private long lastUpdateTime = System.currentTimeMillis();
	private double averageSpeed = 0.0f;
	private boolean diagnostic;

	public ProgressWindow(String title, String message, boolean diagnostic) {
		this.setTitle(title);
		this.setResizable(false);
		this.diagnostic = diagnostic;

		GridPane grid = new GridPane();
		grid.setPadding(new Insets(60, 30, 0, 30));
		grid.setVgap(2);
		Scene scene = new Scene(grid, 400, 200);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		labelMessage = new Label(message);
		grid.add(labelMessage, 0, 0);
		if (diagnostic) {
			labelTime = new Label("... noch etwa " + secondsRemaining + " Sekunden.");
			grid.add(labelTime, 0, 2);
			GridPane.setHalignment(labelTime, HPos.RIGHT);
		}

		bar = new ProgressBar(0.0);
		bar.setPrefWidth(340);
		grid.add(bar, 0, 1);

		this.setScene(scene);
	}
	
	public void setMessage(String message) {
		Platform.runLater(() -> labelMessage.setText(message));
	}

	public void setValue(double v) {
		if (v == lastProgress)
			return;
		bar.setProgress(v);
		if (!diagnostic)
			return;
		long now = System.currentTimeMillis();
		double speed = (((now - lastUpdateTime) / 1000.0) / (v - lastProgress));
		averageSpeed = (0.9f * averageSpeed) + (0.1f * speed);
		System.out.println(averageSpeed);
		lastUpdateTime = now;
		lastProgress = v;
		secondsRemaining = (int) (averageSpeed * (1 - v)) + 1;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				labelTime.setText("... noch etwa " + secondsRemaining + " Sekunden.");
			}
		});
	}

}
