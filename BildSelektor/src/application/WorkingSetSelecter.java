package application;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public abstract class WorkingSetSelecter {

	private static final Color COLOR_LINE = new Color(0.5, 0.5, 0.5, 1);
	private static final int THUMBNAIL_WIDTH = 100;

	public static Pane getSurface(boolean initial, BufferedWorkingSetSelectAction action) {

		Pane res = new Pane();

		Label labelHeadline = new Label(
				initial ? "Sitzung zum Laden auswählen" : "Sitzung zum Überschreiben auswählen");
		labelHeadline.setStyle("-fx-text-fill: rgb(248,248,248);" + "	-fx-font-size: 17px; -fx-font-weight: bold;");
		labelHeadline.setPadding(new Insets(5, 10, 5, 20));

		Line l0 = new Line(0, 0, 0, 0);
		l0.endXProperty().bind(res.widthProperty());
		l0.setStroke(COLOR_LINE);

		VBox layoutLowest = new VBox();
		layoutLowest.getChildren().addAll(labelHeadline, l0);
		VBox.setMargin(l0, new Insets(0, 0, 1, 0));
	

		VBox layoutSets = new VBox();
		layoutSets.setSpacing(10);
		layoutSets.setPadding(new Insets(20, 10, 30, 10));
		
		ScrollPane scrollPane = new ScrollPane(layoutSets);
		scrollPane.maxHeightProperty().bind(res.heightProperty().subtract(35));
		scrollPane.setFitToWidth(true);
		scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		
		scrollPane.heightProperty().addListener(new InvalidationListener() {
			
			@Override
			public void invalidated(Observable observable) {
				
				if (layoutSets.getHeight() + 35 < res.getHeight()) {
					scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
				} else {
					scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
				}
				
			}
		});
		
		WorkingSetInfo[] workingSets = FileManager.getWorkingSetInfos();
		WorkingSetInfo[] sortedSets = sortForAge(workingSets);
		for (WorkingSetInfo info : sortedSets)
			layoutSets.getChildren().add(getVisualSetInfo(info, layoutSets, action));

		layoutLowest.getChildren().add(scrollPane);
		res.getChildren().add(layoutLowest);

		return res;

	}

	private static Button getVisualSetInfo(WorkingSetInfo info, VBox sublayout, BufferedWorkingSetSelectAction action) {

		Button res = new Button();
		GridPane layout = new GridPane();
		res.getStyleClass().removeAll("button");
		res.getStyleClass().add("wsbutton");

		Image img = info.getThumbnail();
		double imgScale = THUMBNAIL_WIDTH / img.getWidth();
		ImageView imgview = MainWindow.getImageView(info.getThumbnail(), THUMBNAIL_WIDTH,
				(int) (img.getHeight() * imgScale));
		GridPane.setMargin(imgview, new Insets(10, 10, 10, 10));

		Line lineHorizontal = new Line(0, 0, 0, 0);
		lineHorizontal.setStroke(COLOR_LINE);
		lineHorizontal.endXProperty().bind(res.widthProperty().subtract(THUMBNAIL_WIDTH + 20 + 20));

		Label labelTitle = new Label(info.getTitle());
		labelTitle.setStyle("-fx-text-fill: rgb(248,248,248);" + "	-fx-font-size: 30px; -fx-font-weight: normal");

		Label labelImages = new Label(info.getStartSize() + " Bilder");
		labelImages.setStyle("-fx-text-fill: rgb(248,248,248);" + "	-fx-font-size: 16px; -fx-font-weight: normal");

		Label labelPercentage = new Label(
				(int) (100 - (100.0 * ((double) info.getUnseen() / info.getStartSize()))) + " % durchgesehen");
		labelPercentage.setStyle("-fx-text-fill: rgb(248,248,248);" + "	-fx-font-size: 16px; -fx-font-weight: normal");

		int age = 0;
		String ageUnit = "Tag";
		if (info.getYearDifferenceToNow() > 0) {
			age = info.getYearDifferenceToNow();
			ageUnit = "Jahr" + (info.getYearDifferenceToNow() > 1 ? "e" : "");
		} else if (info.getMonthDifferenceToNow() > 0) {
			age = info.getMonthDifferenceToNow();
			ageUnit = "Monat" + (info.getMonthDifferenceToNow() > 1 ? "e" : "");
		} else if (info.getDayDifferenceToNow() > 0) {
			age = info.getDayDifferenceToNow();
			ageUnit = "Tag" + (info.getDayDifferenceToNow() > 1 ? "e" : "");
		} else if (info.getHourDifferenceToNow() > 0) {
			age = info.getHourDifferenceToNow();
			ageUnit = "Stunde" + (info.getHourDifferenceToNow() > 1 ? "n" : "");
		} else if (info.getMinuteDifferenceToNow() > 0) {
			age = info.getMinuteDifferenceToNow();
			ageUnit = "Minute" + (info.getMinuteDifferenceToNow() > 1 ? "n" : "");
		} else if (info.getSecondDifferenceToNow() > 0) {
			age = info.getSecondDifferenceToNow();
			ageUnit = "Sekunde" + (info.getSecondDifferenceToNow() > 1 ? "n" : "");
		} else {
			age = 1;
			ageUnit = "Ladefehler";
		}

		Label labelAge = new Label("etwa " + age + " " + ageUnit + " alt");
		labelAge.setStyle("-fx-text-fill: rgb(248,248,248);" + "	-fx-font-size: 16px; -fx-font-weight: normal");

		Pane horizontalFillPane = new Pane();
		
		GridPane.setMargin(labelImages, new Insets(15, 20, 5, 0));
		GridPane.setMargin(labelPercentage, new Insets(15, 0, 5, 0));
		GridPane.setMargin(labelAge, new Insets(15, 13, 5, 20));
		GridPane.setValignment(labelTitle, VPos.CENTER);
		GridPane.setVgrow(lineHorizontal, Priority.ALWAYS);
		GridPane.setVgrow(labelTitle, Priority.ALWAYS);
		GridPane.setHgrow(labelPercentage, Priority.ALWAYS);
		GridPane.setHgrow(labelImages, Priority.ALWAYS);

		layout.add(imgview, 0, 0, 1, 4);
		layout.add(labelTitle, 1, 0, 3, 1);
		layout.add(lineHorizontal, 1, 1, 3, 1);
		layout.add(horizontalFillPane, 1, 2, 3, 1);
		layout.add(labelImages, 1, 3);
		layout.add(labelPercentage, 2, 3);
		layout.add(labelAge, 3, 3);

		res.maxWidthProperty().bind(sublayout.widthProperty().subtract(20));
		res.setGraphic(layout);
		res.setOnAction(e -> action.select(info));
		return res;
	}

	private static WorkingSetInfo[] sortForAge(WorkingSetInfo[] base) {

		WorkingSetInfo[] res = new WorkingSetInfo[base.length];

		for (int j = 0; j != base.length; j++) {

			long lowestDate = Long.MAX_VALUE;
			WorkingSetInfo lowest = null;
			for (WorkingSetInfo i : base)
				if (i.getDate() < lowestDate) {
					boolean alreadyAdded = false;
					for (int k = 0; k != base.length; k++)
						if (res[k] != null)
							if (res[k] == i) {
								alreadyAdded = true;
								break;
							}
					if (!alreadyAdded)
						lowest = i;
				}
			res[j] = lowest;
		}

		return res;

	}

}
