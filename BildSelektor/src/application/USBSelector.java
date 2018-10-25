package application;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public abstract class USBSelector {

	public static final File FILE_NONE = new File("");

	public static File openDialog(boolean toload) {

		Flag flagAbort = Flag.inactive();

		Label headline = new Label("");
		Stage[] stage = new Stage[] { null };
		Flag instaCloseStage = Flag.inactive();

		Thread ui = new Thread(() -> Platform.runLater(new Runnable() {

			@Override
			public void run() {
				stage[0] = new Stage();
				VBox layout_center = new VBox();
				layout_center.setSpacing(10);
				Button buttonAbort = new Button("Abbrechen");
				BorderPane layout_root = new BorderPane();
				stage[0].setTitle("USB-Speicher wählen");
				Scene scene = new Scene(layout_root, 400, 200);
				stage[0].setResizable(false);
				scene.getStylesheets().add(MainWindow.global_style);
				stage[0].setScene(scene);
				stage[0].getIcons().add(MainWindow.icon);
				headline.setPadding(new Insets(10));
				layout_center.getChildren().addAll(headline, buttonAbort);
				BorderPane.setAlignment(layout_center, Pos.CENTER);
				layout_center.setAlignment(Pos.CENTER);
				layout_root.setCenter(layout_center);

				buttonAbort.setOnAction(e -> flagAbort.setActive(true));
				stage[0].setOnCloseRequest(e -> flagAbort.setActive(true));

				if (!instaCloseStage.isActive())
					stage[0].show();

			}
		}));
		ui.setPriority(Thread.MAX_PRIORITY);

		String[] letters = new String[] { "E", "F", "G", "H", "I", "J", "K", "L", "M", "N" };
		List<File> readables = new ArrayList<File>();

		for (int i = 0; i != letters.length; i++) {
			File device = new File(letters[i] + ":\\");
			if (device.exists())
				readables.add(device);
		}

		if (readables.size() == 1) { // Eh nur ein Gerät da
			if (stage[0] != null)
				Platform.runLater(() -> stage[0].close());
			else
				instaCloseStage.setActive(true);
			return readables.get(0);
		}

		if (readables.size() == 0)
			Platform.runLater(() -> headline.setText("Bitte Gerät anschließen" + (toload
					? "\n\n(Wenn das Gerät nicht erkannt wird, \nbitte Abbrechen und die Bilder manuell auswählen)"
					: "")));
		else
			Platform.runLater(() -> headline
					.setText("Mehrere Geräte gefunden.\nBitte gewähltes Gerät rausziehen \nund wieder anschließen."));

		ui.start();
		File res = getAdd(flagAbort);
		Platform.runLater(() -> stage[0].close());
		return res;

	}

	private static File getAdd(Flag abort) {
		String[] letters = new String[] { "E", "F", "G", "H", "I", "J", "K", "L", "M", "N" };
		List<File> lastReadables = new ArrayList<File>();
		for (int i = 0; i != letters.length; i++) {
			File device = new File(letters[i] + ":\\");
			if (device.exists())
				lastReadables.add(device);
		}

		while (!abort.isActive()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for (int i = 0; i != letters.length; i++) {
				File device = new File(letters[i] + ":\\");
				if (device.canRead() && !lastReadables.contains(device))
					return device;
			}

			lastReadables.clear();
			for (int i = 0; i != letters.length; i++) {
				File device = new File(letters[i] + ":\\");
				if (device.exists())
					lastReadables.add(device);
			}

		}

		return FILE_NONE;

	}

}
