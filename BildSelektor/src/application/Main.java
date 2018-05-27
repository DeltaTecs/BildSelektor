package application;

import java.io.File;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Main extends Application {

	public static final String PATH = new File("").getAbsolutePath();

	public static double loadProgress = 0;
	public static double progressPerImage = 0;
	public static Main currentInstance;
	private MainWindow mainWindow;
	private ProgressWindow progressWindow;
	private BufferedWorkingSet workingSet = null;

	@Override
	public void start(Stage primaryStage) {
		currentInstance = this;
		this.loadImages();
		new StartupWindow().show();

	}

	public static void main(String[] args) {
		launch(args);
	}

	public void openNewSet() {

		Stage primaryStage = new Stage();
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
			progressPerImage = 0.94 / files.size();

			progressWindow = new ProgressWindow("Programm lädt", "Bilder werden geladen...", false);
			progressWindow.show();

			// Tatsächlich laden
			Thread actualLoadingThread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						workingSet = BufferedWorkingSet.genNew(files);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}, "loading-main");
			actualLoadingThread.setPriority(Thread.NORM_PRIORITY);
			actualLoadingThread.start();

			// Lade-Fenster updaten
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (workingSet == null) {
						progressWindow.setValue(loadProgress);
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					// Laden beendet
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							mainWindow = new MainWindow(primaryStage, workingSet);
							progressWindow.close();
						}
					});
				}
			}, "prog-window-update").start();
			// ---- Laden ende ------

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openOldSet(WorkingSetInfo info) {
		Stage primaryStage = new Stage();
		try {

			progressWindow = new ProgressWindow("Programm lädt", "Bilder werden geladen...", false);
			progressWindow.show();

			// Tatsächlich laden
			Thread actualLoadingThread = new Thread(new Runnable() {

				@Override
				public void run() {
					String path = PATH + FileManager.REL_PATH_WORKINGSETS + "\\" + info.getHeader();
					int imagesNew = new File(PATH + FileManager.REL_PATH_WORKINGSETS + "\\" + info.getHeader()
							+ FileManager.REL_PATH_ORIGINAL_NEW).listFiles().length;
					int imagesOld = new File(PATH + FileManager.REL_PATH_WORKINGSETS + "\\" + info.getHeader()
							+ FileManager.REL_PATH_ORIGINAL_SEEN).listFiles().length;
					int imagesCopy = new File(PATH + FileManager.REL_PATH_WORKINGSETS + "\\" + info.getHeader()
							+ FileManager.REL_PATH_COPY).listFiles().length;
					int imagesTrash = new File(PATH + FileManager.REL_PATH_WORKINGSETS + "\\" + info.getHeader()
							+ FileManager.REL_PATH_TRASH).listFiles().length;
					progressPerImage = 0.94 / (imagesNew + imagesOld + imagesCopy + imagesTrash);
					workingSet = BufferedWorkingSet
							.loadExisting(new File(PATH + FileManager.REL_PATH_WORKINGSETS + "\\" + info.getHeader()));
				}
			}, "loading-main");
			actualLoadingThread.setPriority(Thread.NORM_PRIORITY);
			actualLoadingThread.start();

			// Lade-Fenster updaten
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (workingSet == null) {
						progressWindow.setValue(loadProgress);
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					// Laden beendet
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							mainWindow = new MainWindow(primaryStage, workingSet);
							progressWindow.close();
						}
					});
				}
			}, "prog-window-update").start();
			// ---- Laden ende ------

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadImages() {
		try {
			MainWindow.img_box_foreground = new Image(this.getClass().getResource("box_foreground.png").toString());
			MainWindow.img_box_background = new Image(this.getClass().getResource("box.png").toString());
			MainWindow.img_trash_foreground = new Image(this.getClass().getResource("trash_foreground.png").toString());
			MainWindow.img_trash_background = new Image(this.getClass().getResource("trash.png").toString());
			MainWindow.img_arrow_right = new Image(this.getClass().getResource("arrow_right.png").toString());
			MainWindow.img_arrow_left = new Image(this.getClass().getResource("arrow_left.png").toString());
			MainWindow.img_red_x = new Image(this.getClass().getResource("red_x.png").toString());
			MainWindow.img_green_tick = new Image(this.getClass().getResource("green_tick.png").toString());
			MainWindow.img_floppydisk = new Image(this.getClass().getResource("floppydisk.png").toString());
			MainWindow.img_scissors = new Image(this.getClass().getResource("scissors.png").toString());
			MainWindow.img_import = new Image(this.getClass().getResource("import.png").toString());
			MainWindow.img_export = new Image(this.getClass().getResource("export.png").toString());
			MainWindow.img_loadFromFloppydisk = new Image(this.getClass().getResource("loadFloppyDisk.png").toString());
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Bild laden für ui gescheitert. Exit.");
			System.exit(-1);
			return;
		}
	}
}
