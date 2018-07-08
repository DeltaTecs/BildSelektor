package application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ExitPrompt extends Stage {

	private static ExitPrompt currentInstance = null;

	public ExitPrompt() {
		getIcons().add(MainWindow.icon);

		GridPane layout_root = new GridPane();
		Scene scene = new Scene(layout_root, 400, 200);
		scene.getStylesheets().add(MainWindow.global_style);
		setScene(scene);
		setAlwaysOnTop(true);

		setOnCloseRequest(e -> currentInstance = null);

		ImageView redx = MainWindow.getImageView(MainWindow.img_red_x, 40, 40);
		Label message = new Label("Wirklich ohne zu Speichern schließen?\nAlle Änderungen gehen dann verloren.");
		Button buttonYes = new Button("Ja");
		Button buttonNo = new Button("Nein");

		layout_root.add(redx, 0, 0);
		layout_root.add(message, 1, 0, 3, 1);
		layout_root.add(buttonYes, 1, 1);
		layout_root.add(buttonNo, 2, 1);
		
		layout_root.setAlignment(Pos.CENTER);
		layout_root.setHgap(10);
		layout_root.setVgap(15);
		buttonYes.setPrefSize(100, 40);
		buttonNo.setPrefSize(100, 40);

		buttonYes.setOnAction(e -> System.exit(1));
		buttonNo.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				currentInstance.close();
				currentInstance = null;
			}
		});

		buttonNo.requestFocus();
		show();
		requestFocus();
	}

	public static void showPrompt() {

		if (currentInstance != null) {
			currentInstance.show();
			currentInstance.requestFocus();
		} else {
			ExitPrompt window = new ExitPrompt();
			currentInstance = window;
		}
	}

}
