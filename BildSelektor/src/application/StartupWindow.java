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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class StartupWindow extends Stage {

	private static final Color COLOR_LINE = new Color(0.5, 0.5, 0.5, 1);

	private BorderPane layout_root = null;
	private VBox layoutLowest = null;
	private Label labelHeadline = null;

	public StartupWindow() {

		layout_root = new BorderPane();
		this.getIcons().add(MainWindow.icon);
		Scene scene = new Scene(layout_root, 600, 600);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		labelHeadline = new Label("Was soll geladen werden?");
		labelHeadline.setStyle("-fx-text-fill: rgb(248,248,248);" + "	-fx-font-size: 17px; -fx-font-weight: bold;");
		labelHeadline.setPadding(new Insets(5, 10, 5, 20));

		Line l0 = new Line(0, 0, 0, 0);
		l0.endXProperty().bind(layout_root.widthProperty());
		l0.setStroke(COLOR_LINE);

		layoutLowest = new VBox();
		layoutLowest.getChildren().addAll(labelHeadline, l0);
		VBox.setMargin(l0, new Insets(0, 0, 1, 0));

		GridPane buttonGrid = new GridPane();
		Pane leftSpacePane = new Pane();
		Pane rightSpacePane = new Pane();
		buttonGrid.add(leftSpacePane, 0, 0, 1, 2);
		buttonGrid.add(rightSpacePane, 2, 0, 1, 2);
		GridPane.setHgrow(leftSpacePane, Priority.ALWAYS);
		GridPane.setHgrow(rightSpacePane, Priority.ALWAYS);

		Button buttonNew = new Button("Neue Bilder laden", MainWindow.getImageView(MainWindow.img_import, 50, 50));
		Button buttonLoadExisting = new Button("Alte Sitzung fortsetzen",
				MainWindow.getImageView(MainWindow.img_loadFromFloppydisk, 50, 50));
		buttonNew.setFont(Font.font(25));
		buttonLoadExisting.setFont(Font.font(25));
		GridPane.setMargin(buttonNew, new Insets(40, 0, 20, 0));
		buttonNew.setOnAction(e -> applySourceChoosingScreen());
		buttonLoadExisting.setOnAction(e -> openOverrideScreen(this));

		buttonGrid.add(buttonNew, 1, 0);
		buttonGrid.add(buttonLoadExisting, 1, 1);

		layoutLowest.getChildren().add(buttonGrid);
		layout_root.setCenter(layoutLowest);

		boolean worksetsAvailable = (new File(Main.PATH + FileManager.REL_PATH_WORKINGSETS).listFiles().length > 0);
		buttonLoadExisting.setDisable(!worksetsAvailable);

		this.setScene(scene);
		this.show();
	}

	private void applySourceChoosingScreen() {

		Label labelHeadline = new Label("Woher soll geladen werden?");
		labelHeadline.setStyle("-fx-text-fill: rgb(248,248,248);" + "	-fx-font-size: 17px; -fx-font-weight: bold;");
		labelHeadline.setPadding(new Insets(5, 10, 5, 20));
		labelHeadline.setText("Woher soll geladen werden?");

		Line l0 = new Line(0, 0, 0, 0);
		l0.endXProperty().bind(layout_root.widthProperty());
		l0.setStroke(COLOR_LINE);

		VBox layoutMain = new VBox();
		layoutMain.getChildren().addAll(labelHeadline, l0);
		VBox.setMargin(l0, new Insets(0, 0, 1, 0));

		GridPane buttonGrid = new GridPane();
		buttonGrid.setVgap(10);

		Button buttonFolder = new Button(" Ausgewählter Ordner",
				MainWindow.getImageView(MainWindow.img_folder_search, 70, 70));
		Button buttonUsb = new Button(" Externes Speichermedium",
				MainWindow.getImageView(MainWindow.img_usbstick, 70, 70));
		Button buttonHandy = new Button(" Kamera oder Handy", MainWindow.getImageView(MainWindow.img_handy, 70, 70));
		GridPane.setMargin(buttonFolder, new Insets(10, 0, 0, 10));
		GridPane.setMargin(buttonUsb, new Insets(0, 0, 0, 10));
		GridPane.setMargin(buttonHandy, new Insets(0, 0, 0, 10));
		buttonFolder.setFont(Font.font(17));
		buttonUsb.setFont(Font.font(17));
		buttonHandy.setFont(Font.font(17));
		buttonFolder.setAlignment(Pos.CENTER_LEFT);
		buttonUsb.setAlignment(Pos.CENTER_LEFT);
		buttonHandy.setAlignment(Pos.CENTER_LEFT);
		buttonFolder.setPrefWidth(300);
		buttonUsb.setPrefWidth(300);
		buttonHandy.setPrefWidth(300);

		buttonFolder.setOnAction(e -> openNewSet());
		buttonUsb.setOnAction(e -> openFromStick());
		buttonHandy.setOnAction(e -> openFromHandy());

		Button buttonReturn = new Button(" Zurück", MainWindow.getImageView(MainWindow.img_returnarrow, 40, 40));
		buttonReturn.setAlignment(Pos.CENTER_LEFT);
		buttonReturn.setPrefWidth(150);
		GridPane.setMargin(buttonReturn, new Insets(10, 0, 0, 10));
		buttonReturn.setOnAction(e -> layout_root.setCenter(layoutLowest));

		buttonGrid.add(buttonFolder, 0, 0);
		buttonGrid.add(buttonUsb, 0, 1);
		buttonGrid.add(buttonHandy, 0, 2);
		buttonGrid.add(buttonReturn, 0, 3);

		layoutMain.getChildren().add(buttonGrid);
		layout_root.setCenter(layoutMain);
	}

	private void openOverrideScreen(Stage currentStage) {
		layout_root.setCenter(WorkingSetSelecter.getSurface(true, new BufferedWorkingSetSelectAction() {

			@Override
			public void select(WorkingSetInfo info) {
				Platform.runLater(() -> Main.currentInstance.openOldSet(info));
				Platform.runLater(() -> currentStage.close());
			}
		}));
	}

	private void openNewSet() {
		this.close();
		Platform.runLater(() -> Main.currentInstance.openNewSet());
	}

	private void openFromHandy() {

		Stage s0 = this;
		boolean[] usbWindowActive = new boolean[] { false };

		if (usbWindowActive[0])
			return;

		usbWindowActive[0] = true;

		new Thread(new Runnable() {

			@Override
			public void run() {

				File dir = USBSelector.openDialog(true);
				while (dir == null) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				usbWindowActive[0] = false;

				if (dir == USBSelector.FILE_NONE)
					return;
				
				dir = new File(dir.getAbsolutePath() + "\\DCIM\\Camera");
				if (!dir.exists()) {
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							final Stage s = new Stage();
							s.getIcons().add(MainWindow.icon);
							BorderPane layoutRoot = new BorderPane();
							Scene scene = new Scene(layoutRoot, 400, 200);
							scene.getStylesheets().add(MainWindow.global_style);
							Label label = new Label("Das Medium verfügt nicht über einen\nDCIM\\Camera Ordner.");
							Button button = new Button("OK");
							label.setAlignment(Pos.CENTER);
							BorderPane.setAlignment(button, Pos.BOTTOM_CENTER);
							BorderPane.setMargin(button, new Insets(10));
							button.setAlignment(Pos.CENTER);
							button.setPrefSize(100, 40);
							label.setPadding(new Insets(20));
							layoutRoot.setCenter(label);
							layoutRoot.setBottom(button);
							s.setScene(scene);
							s.show();
							button.setOnAction(e -> s.close());
						}
					});
					return;
				}

				List<File> images = new ArrayList<File>();
				for (File f : dir.listFiles()) {
					if (f.isDirectory() || !f.exists())
						continue;
					String[] split = f.getName().split("\\.");
					if (split.length < 2)
						continue;
					if (split[split.length - 1].equalsIgnoreCase("jpg")
							|| split[split.length - 1].equalsIgnoreCase("png")
							|| split[split.length - 1].equalsIgnoreCase("jpeg")
							|| split[split.length - 1].equalsIgnoreCase("gif")) {
						images.add(f);

					}
				}

				if (images.size() == 0) {
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							final Stage s = new Stage();
							s.getIcons().add(MainWindow.icon);
							BorderPane layoutRoot = new BorderPane();
							Scene scene = new Scene(layoutRoot, 400, 200);
							scene.getStylesheets().add(MainWindow.global_style);
							Label label = new Label("Auf dem Medium wurden keine Bilder gefunden.");
							Button button = new Button("OK");
							label.setAlignment(Pos.CENTER);
							BorderPane.setAlignment(button, Pos.BOTTOM_CENTER);
							BorderPane.setMargin(button, new Insets(10));
							button.setAlignment(Pos.CENTER);
							button.setPrefSize(100, 40);
							label.setPadding(new Insets(20));
							layoutRoot.setCenter(label);
							layoutRoot.setBottom(button);
							s.setScene(scene);
							s.show();
							button.setOnAction(e -> s.close());
						}
					});
					return;
				}

				Platform.runLater(() -> s0.close());
				openFrom(images);

			}
		}).start();

	}
	
	private void openFromStick() {

		Stage s0 = this;
		boolean[] usbWindowActive = new boolean[] { false };

		if (usbWindowActive[0])
			return;

		usbWindowActive[0] = true;

		new Thread(new Runnable() {

			@Override
			public void run() {

				File dir = USBSelector.openDialog(true);
				while (dir == null) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				usbWindowActive[0] = false;

				if (dir == USBSelector.FILE_NONE)
					return;
				
				dir = getDirWithImages(dir);
				if (dir == null){
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							final Stage s = new Stage();
							s.getIcons().add(MainWindow.icon);
							BorderPane layoutRoot = new BorderPane();
							Scene scene = new Scene(layoutRoot, 400, 200);
							scene.getStylesheets().add(MainWindow.global_style);
							Label label = new Label("Auf dem Medium wurden\nkeine Bilder gefunden.");
							Button button = new Button("OK");
							label.setAlignment(Pos.CENTER);
							BorderPane.setAlignment(button, Pos.BOTTOM_CENTER);
							BorderPane.setMargin(button, new Insets(10));
							button.setAlignment(Pos.CENTER);
							button.setPrefSize(100, 40);
							label.setPadding(new Insets(20));
							layoutRoot.setCenter(label);
							layoutRoot.setBottom(button);
							s.setScene(scene);
							s.show();
							button.setOnAction(e -> s.close());
						}
					});
					return;
				}

				List<File> images = new ArrayList<File>();
				for (File f : dir.listFiles()) {
					if (f.isDirectory() || !f.exists())
						continue;
					String[] split = f.getName().split("\\.");
					if (split.length < 2)
						continue;
					if (split[split.length - 1].equalsIgnoreCase("jpg")
							|| split[split.length - 1].equalsIgnoreCase("png")
							|| split[split.length - 1].equalsIgnoreCase("jpeg")
							|| split[split.length - 1].equalsIgnoreCase("gif")) {
						images.add(f);

					}
				}

				Platform.runLater(() -> s0.close());
				openFrom(images);

			}
		}).start();

	}

	private void openFrom(List<File> images) {
		if (images == null) {
			System.out.println("Abbruch");
			System.exit(0);
		}
		Main.startFiles.addAll(images);
		Main.progressPerImage = 0.94 / images.size();
		Main.loadProgress = 0;
		
		ProgressWindow[] progressWindow = new ProgressWindow[] { null };
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				progressWindow[0] = new ProgressWindow("Programm lädt", "Bilder werden geladen...", false, null);
				progressWindow[0].show();
			}
		});

		BufferedWorkingSet[] workingSet = new BufferedWorkingSet[] { null };

		// Tatsächlich laden
		Thread actualLoadingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					workingSet[0] = BufferedWorkingSet.genNew(images);
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
				while (workingSet[0] == null) {
					if (progressWindow[0] != null)
						progressWindow[0].setValue(Main.loadProgress);
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
						new MainWindow(Main.currentInstance.getPrimaryStage(), workingSet[0]);
						progressWindow[0].close();
					}
				});
			}
		}, "prog-window-update").start();
		// ---- Laden ende ------
	}
	
	private static File getDirWithImages(File start) {
		
		
		List<File> subdirs = new ArrayList<File>();
		
		for (File f : start.listFiles()) {
			
			if (!f.exists())
				continue;
			
			if (f.isDirectory()) {
				subdirs.add(f);
				continue;
			}
			
			
			// Namen analysieren
			String[] split = f.getName().split("\\.");
			if (split.length < 2)
				continue;
			if (split[split.length - 1].equalsIgnoreCase("jpg")
					|| split[split.length - 1].equalsIgnoreCase("png")
					|| split[split.length - 1].equalsIgnoreCase("jpeg")
					|| split[split.length - 1].equalsIgnoreCase("gif")) {
				return start;

			}
		}
		
		for (File subdir : subdirs) {
			File res = getDirWithImages(subdir);
			if (res == null)
				continue;
			else
				return res;
		}
		
		return null;
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}

}
