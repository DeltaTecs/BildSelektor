package application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Main extends Application {

	public static final String PATH = new File("").getAbsolutePath();
	private ArrayList<Image> baseImages = new ArrayList<Image>();

	private double loadProgress = 0;
	private MainWindow mainWindow;
	private ProgressWindow progressWindow;

	@Override
	public void start(Stage primaryStage) {
		
		try {

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Bilder auswählen");
			fileChooser.getExtensionFilters().add(new ExtensionFilter("Bilder", "*.png", "*.jpg", "*.jpeg"));

			// Progress :: // ---- Laden start ------
			List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
			if (files == null) {
				System.out.println("Abbruch");
				System.exit(0);
			}
			progressWindow = new ProgressWindow("Programm lädt", "Bilder werden geladen...", false);
			progressWindow.show();
			// Einlesen
			new Thread(new Runnable() {

				@Override
				public void run() {
					int i = 1;
					for (File f : files) {
						System.out.println();
						try {
							baseImages.add(SwingFXUtils.toFXImage(ImageIO.read(f), null));
						} catch (IOException e) {
							System.err.println("Ein Bild wurde nicht geladen");
						}
						loadProgress = ((double) i) / files.size();
						i++;
					}
					// files.clear();
					// System.gc();
				}
			}).start();
			// Darstellen
			new Thread(new Runnable() {

				@Override
				public void run() {
					while (loadProgress < 1.0) {
						progressWindow.setValue(loadProgress);
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							progressWindow.setMessage("Programm wird geladen...");
							progressWindow.setAlwaysOnTop(true);
							// HauptFenster initialisierens
							mainWindow = new MainWindow(primaryStage, progressWindow,
									new WorkingSet(baseImages, null, null, true));
							progressWindow.close();
						}
					});
				}
			}).start();
			// ---- Laden ende ------

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
