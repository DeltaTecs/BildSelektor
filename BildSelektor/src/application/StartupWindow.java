package application;

import java.io.File;

import javafx.application.Platform;
import javafx.geometry.Insets;
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

	public StartupWindow() {

		layout_root = new BorderPane();

		Scene scene = new Scene(layout_root, 600, 600);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Label labelHeadline = new Label("Was soll geladen werden?");
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
		buttonGrid.add(leftSpacePane, 0, 0, 1, 2);
		buttonGrid.add(rightSpacePane, 2, 0, 1, 2);
		GridPane.setHgrow(leftSpacePane, Priority.ALWAYS);
		GridPane.setHgrow(rightSpacePane, Priority.ALWAYS);
		
		Button buttonNew = new Button("Neue Bilder laden", MainWindow.getImageView(MainWindow.img_import, 50, 50));
		Button buttonLoadExisting = new Button("Alte Sitzung fortsetzen", MainWindow.getImageView(MainWindow.img_loadFromFloppydisk, 50, 50));
		buttonNew.setFont(Font.font(25));
		buttonLoadExisting.setFont(Font.font(25));
		GridPane.setMargin(buttonNew, new Insets(40, 0, 20, 0));
		buttonNew.setOnAction(e -> openNewSet());
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
	

}
