package application;

import java.io.File;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

public class SaveSetPane {

	private static final Color COLOR_LINE = new Color(0.5, 0.5, 0.5, 1);

	private BorderPane layout_root = new BorderPane();
	private MainWindow mainWindow;

	public SaveSetPane(MainWindow main) {
		this.mainWindow = main;

		Label labelHeadline = new Label("Sitzung für später abspeichern");
		labelHeadline.setStyle("-fx-text-fill: rgb(248,248,248);" + "	-fx-font-size: 17px; -fx-font-weight: bold;");
		labelHeadline.setPadding(new Insets(5, 10, 5, 20));

		Line l0 = new Line(0, 0, 0, 0);
		l0.endXProperty().bind(layout_root.widthProperty());
		l0.setStroke(COLOR_LINE);

		VBox layoutLowest = new VBox();
		layoutLowest.getChildren().addAll(labelHeadline, l0);
		VBox.setMargin(l0, new Insets(0, 0, 1, 0));

		GridPane buttonGrid = new GridPane();
		Pane leftSpacePane = new Pane();
		Pane rightSpacePane = new Pane();
		buttonGrid.add(leftSpacePane, 0, 0, 1, 3);
		buttonGrid.add(rightSpacePane, 3, 0, 1, 3);
		GridPane.setHgrow(leftSpacePane, Priority.ALWAYS);
		GridPane.setHgrow(rightSpacePane, Priority.ALWAYS);

		Button buttonNew = new Button("Diese Sitzung seperat speichern",
				MainWindow.getImageView(MainWindow.img_addFloppydisk, 50, 50));
		Button buttonOverride = new Button("Dafür eine alte Sitzung löschen",
				MainWindow.getImageView(MainWindow.img_overrideFloppydisk, 50, 50));
		buttonOverride.prefWidthProperty().bind(buttonNew.widthProperty());
		buttonNew.setFont(Font.font(25));
		buttonOverride.setFont(Font.font(25));
		GridPane.setMargin(buttonOverride, new Insets(40, 0, 20, 0));
		GridPane.setMargin(buttonNew, new Insets(0, 0, 40, 0));
		buttonNew.setOnAction(e -> saveNewSet());
		buttonOverride.setOnAction(e -> overrideSet());

		Button buttonReturn = new Button("Zurück", MainWindow.getImageView(MainWindow.img_returnarrow, 30, 30));
		buttonReturn.setOnAction(e -> main.resetView());

		buttonGrid.add(buttonNew, 1, 1, 2, 1);
		buttonGrid.add(buttonOverride, 1, 0, 2, 1);
		buttonGrid.add(buttonReturn, 2, 3);

		layoutLowest.getChildren().add(buttonGrid);
		layout_root.setCenter(layoutLowest);

		boolean worksetsAvailable = new File(Main.PATH + FileManager.REL_PATH_WORKINGSETS).listFiles() != null;
		buttonOverride.setDisable(!worksetsAvailable);
	}

	private void saveNewSet() {

		mainWindow.getStage().close();
		Main.loadProgress = 0;
		boolean copyNecessary = mainWindow.getWorkingSet().getSourceDir() != BufferedWorkingSet.FOLDER_TEMP;
		FinalNotificationTask notTask = new FinalNotificationTask("Fertig", "Alle Bilder wurden erfolgreich abgelegt.",
				true);
		ProgressWindow progressWindow = new ProgressWindow("Speichern",
				"Bilder " + (copyNecessary ? "kopiert " : "") + "ablegen...", false, notTask);
		progressWindow.show();

		Thread workingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if (copyNecessary)
						FileManager.saveWorkingSetByCopying(mainWindow.getWorkingSet());
					else
						FileManager.saveWorkingSetByMoving(mainWindow.getWorkingSet());

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "saving");
		workingThread.setPriority(Thread.NORM_PRIORITY);

		Thread updateThread = new Thread(new Runnable() {

			@Override
			public void run() {

				while (Main.loadProgress < 1.0) {
					progressWindow.setValue(Main.loadProgress);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				progressWindow.setValue(1.0);

			}
		}, "updateprogress");
		updateThread.setPriority(Thread.NORM_PRIORITY);

		workingThread.start();
		updateThread.start();

	}

	private void overrideSet() {

		BorderPane layout = new BorderPane();
		Pane setSelector = WorkingSetSelecter.getSurface(false, new BufferedWorkingSetSelectAction() {

			@Override
			public void select(WorkingSetInfo info) {

				mainWindow.getStage().close();
				boolean copyNecessary = !(mainWindow.getWorkingSet().getSourceDir() == BufferedWorkingSet.FOLDER_TEMP
						|| mainWindow.getWorkingSet().getSourceDir().getName().equals(info.getHeader()));
				FinalNotificationTask notTask = new FinalNotificationTask("Fertig",
						"Die ausgewählte Sitzung wurde\nerfolgreich mit der Neuen überschrieben", true);
				ProgressWindow progressWindow = new ProgressWindow("Speichern",
						"Sitzung " + (copyNecessary ? "kopiert " : "") + "überschreiben...", false, notTask);
				progressWindow.show();

				Main.loadProgress = 0;

				Thread workingThread = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							FileManager.overrideWorkingSet(info.getHeader(), mainWindow.getWorkingSet(), copyNecessary);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}, "saving");
				workingThread.setPriority(Thread.NORM_PRIORITY);

				Thread updateThread = new Thread(new Runnable() {

					@Override
					public void run() {

						while (Main.loadProgress < 1.0) {
							progressWindow.setValue(Main.loadProgress);
							try {
								Thread.sleep(20);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						progressWindow.setValue(1.0);

					}
				}, "updateprogress");
				updateThread.setPriority(Thread.NORM_PRIORITY);

				workingThread.start();
				updateThread.start();

			}
		});
		layout.setCenter(setSelector);

		HBox p = new HBox();
		Button buttonReturn = new Button("Zurück", MainWindow.getImageView(MainWindow.img_returnarrow, 40, 40));
		buttonReturn.setOnAction(e -> mainWindow.setView(layout_root));
		p.getChildren().add(buttonReturn);
		HBox.setMargin(buttonReturn, new Insets(10));
		layout.setBottom(p);
		mainWindow.setView(layout);
	}

	public BorderPane getLayout_root() {
		return layout_root;
	}

}
