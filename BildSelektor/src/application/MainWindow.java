package application;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class MainWindow {

	private static final int CANVAS_COLLECTION_WIDTH = 330;
	private static final int CANVAS_COLLECTION_HEIGHT = 300;
	private static final Color COLOR_LINE = new Color(0.5, 0.5, 0.5, 1);
	private static final Color COLOR_BLEND = new Color(0, 0, 0, 0.8);
	private static final Color COLOR_CUTTING_FRAME = new Color(226 / 255.0, 47 / 255.0, 29 / 255.0, 1);
	private static final int MIN_CUT_SIZE = 70;
	private static final double GRIP_HEIGHT = 30;
	private static final double GRIP_LENGTH = 60;

	private Image img_box_background;
	private Image img_trash_background;
	private Image img_box_foreground;
	private Image img_trash_foreground;
	private Image img_arrow_left;
	private Image img_arrow_right;
	private Image img_red_x;
	private Image img_green_tick;
	private Image img_floppydisk;
	private Image img_scissors;
	private Image img_import;
	private Image img_export;

	private Scene scene;
	private GraphicsContext g0;
	private BorderPane layout_root;
	private VBox layout_left_basis_content;
	private HBox layout_left_basis;
	private HBox layout_left_options;
	private GridPane layout_left_options_content;
	private HBox layout_left_exit;
	private GridPane layout_left_exit_content;
	private Canvas canvasCollection;
	private ResizeableCanvas canvasShowcase;
	private HBox layout_canvasdescription;
	private Label label_trash;
	private Label label_original;
	private Label label_copy;
	private Button button_action_save;
	private Button button_action_delete;
	private CheckBox checkOriginalSave;
	private CheckBox checkOriginalDelete;
	private CheckBox checkCopySave;
	private Button buttonCut;

	private WorkingSet workingSet;
	private Image currentImage = null;
	private Image storeImage = null;
	private int turningAngle = 0;
	private boolean cuttingMode = false;
	private double cutting_x = 0.1, cutting_y = 0.1, cutting_width = 0.8, cutting_height = 0.8;
	private double show_pos_x, show_pos_y, show_width, show_height;
	private double show_press_x, show_press_y;
	private int show_press_sector; // 0: Oben; 1: Unten; 2: Links; 3: Rechts; 4: Mitte

	public MainWindow(Stage stage, ProgressWindow pw, WorkingSet workingSet) {
		// init, layout und canvases
		pw.setValue(0.1);
		this.workingSet = workingSet;
		workingSet.stamp();
		currentImage = workingSet.getBase().get(workingSet.getBase().size() - 1);
		layout_root = new BorderPane();
		scene = new Scene(layout_root, 1200, 700);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		canvasCollection = new Canvas(CANVAS_COLLECTION_WIDTH, CANVAS_COLLECTION_HEIGHT);
		canvasShowcase = new ResizeableCanvas();
		canvasShowcase.setDrawable(DRAWABLE_SHOWCASE_CANVAS);
		canvasShowcase.setOnMousePressed(e -> manageCanvasPressInput(e));
		canvasShowcase.setOnMouseDragged(e -> manageCanvasDragInput(e));

		Pane p0 = new Pane();
		BorderPane.setMargin(p0, new Insets(8, 8, 8, 8));
		canvasShowcase.widthProperty().bind(p0.widthProperty());
		canvasShowcase.heightProperty().bind(p0.heightProperty());
		p0.getChildren().add(canvasShowcase);

		layout_left_basis_content = new VBox();
		layout_left_basis = new HBox();
		layout_left_basis_content.setAlignment(Pos.BOTTOM_LEFT);
		g0 = canvasCollection.getGraphicsContext2D();
		layout_left_basis.getChildren().add(layout_left_basis_content);
		layout_root.setLeft(layout_left_basis);
		layout_root.setCenter(p0);
		pw.setValue(0.15);
		try {
			img_box_foreground = new Image(this.getClass().getResource("box_foreground.png").toString());
			img_box_background = new Image(this.getClass().getResource("box.png").toString());
			img_trash_foreground = new Image(this.getClass().getResource("trash_foreground.png").toString());
			img_trash_background = new Image(this.getClass().getResource("trash.png").toString());
			img_arrow_right = new Image(this.getClass().getResource("arrow_right.png").toString());
			img_arrow_left = new Image(this.getClass().getResource("arrow_left.png").toString());
			img_red_x = new Image(this.getClass().getResource("red_x.png").toString());
			img_green_tick = new Image(this.getClass().getResource("green_tick.png").toString());
			img_floppydisk = new Image(this.getClass().getResource("floppydisk.png").toString());
			img_scissors = new Image(this.getClass().getResource("scissors.png").toString());
			img_import = new Image(this.getClass().getResource("import.png").toString());
			img_export = new Image(this.getClass().getResource("export.png").toString());
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Bild laden f�r ui gescheitert. Exit.");
			System.exit(-1);
			return;
		}
		layout_canvasdescription = new HBox();
		label_trash = new Label("Gel�scht\n(" + workingSet.getTrash().size() + ")");
		label_original = new Label("�brige Originale\n(" + workingSet.getBase().size() + ")");
		label_copy = new Label("Kopien\n(" + workingSet.getCopy().size() + ")");
		label_trash.setTextAlignment(TextAlignment.CENTER);
		label_original.setTextAlignment(TextAlignment.CENTER);
		label_copy.setTextAlignment(TextAlignment.CENTER);
		layout_canvasdescription.getChildren().addAll(label_trash, label_original, label_copy);
		layout_canvasdescription.setSpacing(40);
		layout_canvasdescription.setPadding(new Insets(0, 0, 10, 27));

		// ---
		// Linien
		// Linie: Turn-Buttons -- Collection-Canvas
		Line l0 = new Line(0, 0, CANVAS_COLLECTION_WIDTH, 0);
		l0.setStroke(COLOR_LINE);
		// Linie: Left-Content -- Showcase-Canvas
		Line l1 = new Line(0, 0, 0, 0);
		l1.endYProperty().bind(layout_root.heightProperty().add(0));
		l1.setStroke(COLOR_LINE);
		layout_left_basis.getChildren().add(l1);
		// Linie: Action-Buttons -- Turn-Buttons
		Line l2 = new Line(0, 0, CANVAS_COLLECTION_WIDTH, 0);
		l2.setStroke(COLOR_LINE);
		// Linie: Exit-Button -- Action-Buttons
		Line l3 = new Line(0, 0, CANVAS_COLLECTION_WIDTH, 0);
		l3.setStroke(COLOR_LINE);

		// ---
		// Buttons

		// >> Drehen:
		HBox layout_turn = new HBox();
		HBox layout_turn_buttons = new HBox();
		layout_turn.setSpacing(100);
		layout_turn_buttons.setSpacing(5);
		layout_turn.setPadding(new Insets(20, 10, 20, 0));
		layout_turn.setAlignment(Pos.BASELINE_CENTER);
		layout_turn_buttons.setAlignment(Pos.BASELINE_CENTER);
		Label label_turn = new Label("Alle drehen:");
		Button button_turn_clockwise = new Button();
		Button button_turn_anticlockwise = new Button();
		ImageView imgview_turn_right = getImageView(img_arrow_right, 50, 50);
		ImageView imgview_turn_left = getImageView(img_arrow_left, 50, 50);
		imgview_turn_left.setFitHeight(50);
		imgview_turn_left.setFitWidth(50);
		imgview_turn_right.setFitHeight(50);
		imgview_turn_right.setFitWidth(50);
		button_turn_clockwise.setGraphic(imgview_turn_right);
		button_turn_anticlockwise.setGraphic(imgview_turn_left);
		button_turn_anticlockwise.setPadding(new Insets(4, 4, 4, 4));
		button_turn_clockwise.setPadding(new Insets(4, 4, 4, 4));
		button_turn_anticlockwise.setOnAction(e -> rotateLeft());
		button_turn_clockwise.setOnAction(e -> rotateRight());
		layout_turn_buttons.getChildren().addAll(button_turn_anticlockwise, button_turn_clockwise);
		layout_turn.getChildren().addAll(label_turn, layout_turn_buttons);

		// >> Aktion:
		HBox layout_action = new HBox();
		HBox layout_action_buttons = new HBox();
		layout_action.setSpacing(8);
		layout_action_buttons.setSpacing(5);
		layout_action.setPadding(new Insets(20, 10, 20, 0));
		layout_action.setAlignment(Pos.BASELINE_CENTER);
		layout_action_buttons.setAlignment(Pos.BASELINE_CENTER);
		Label label_action = new Label("Dieses Bild ");
		button_action_save = new Button("Behalten");
		button_action_delete = new Button("L�schen");
		ImageView imgview_action_delete = getImageView(img_red_x, 50, 50);
		ImageView imgview_action_save = getImageView(img_green_tick, 50, 50);
		button_action_save.setGraphic(imgview_action_save);
		button_action_delete.setGraphic(imgview_action_delete);
		button_action_delete.setPadding(new Insets(4, 4, 4, 4));
		button_action_save.setPadding(new Insets(4, 4, 4, 4));
		button_action_delete.setOnAction(e -> action_deleteImage());
		button_action_save.setOnAction(e -> action_keepImage());
		layout_action_buttons.getChildren().addAll(button_action_delete, button_action_save);
		layout_action.getChildren().addAll(label_action, layout_action_buttons);

		// >> Beenden:
		HBox layout_exit = new HBox();
		layout_exit.setPadding(new Insets(10, 10, 10, 10));
		layout_exit.setAlignment(Pos.TOP_LEFT);
		Button button_exit = new Button("Beenden", getImageView(img_floppydisk, 60, 60));
		button_exit.setTextAlignment(TextAlignment.RIGHT);
		button_exit.setPrefWidth(CANVAS_COLLECTION_WIDTH - 20);
		button_exit.setFont(new Font(16));
		button_exit.setAlignment(Pos.BASELINE_CENTER);
		button_exit.setOnAction(e -> openExitPanel());
		layout_exit.getChildren().add(button_exit);

		// add
		layout_left_basis_content.getChildren().addAll(layout_exit, l3, layout_action, l2, layout_turn, l0,
				canvasCollection, layout_canvasdescription);
		VBox.setVgrow(layout_exit, Priority.ALWAYS);

		initOptionsPanel();
		initExitPanel();

		pw.setValue(0.2);
		redrawCollectionCanvas();
		pw.setValue(0.7);
		stage.setMinHeight(690);
		stage.setMinWidth(700);
		stage.setScene(scene);
		stage.setMaximized(true);
		stage.show();
	}

	private static Image cut(Image in, double rx, double ry, double rwidth, double rheight, int angle) {

		PixelReader reader = in.getPixelReader();

		int x0, y0, width0, height0;
		int x, y, width, height;
		double iwidth, iheight;
		
		if (angle == 0 || angle == 180) {
			iwidth = in.getWidth();
			iheight = in.getHeight();
		} else {
			iwidth = in.getHeight();
			iheight = in.getWidth();
		} 
		
		x0 = (int) (iwidth * rx);
		y0 = (int) (iheight * ry);
		width0 = (int) (iwidth * rwidth);
		height0 = (int) (iheight * rheight);

		if (angle == 0) {
			x = x0;
			y = y0;
			width = width0;
			height = height0;
		} else if (angle == 90) {
			x = y0;
			y = (int) iwidth - x0 - width0;
			width = height0;
			height = width0;
		} else if (angle == 180) {
			x = (int) iwidth - x0 - width0;
			y = (int) iheight - y0 - height0;
			width = width0;
			height = height0;
		} else if (angle == 270) {
			x = (int) iheight - y0 - height0;
			y = x0;
			width = height0;
			height = width0;
		} else {
			new Exception("Invalid angle").printStackTrace();
			return null;
		}
		
		return new WritableImage(reader, x, y, width, height);
	}

	private void manageCanvasPressInput(MouseEvent e) {
		// Click -> Pos checken und aktion vormerken

		double x = e.getX() - show_pos_x;
		double y = e.getY() - show_pos_y;

		show_press_x = x;
		show_press_y = y;

		if (y <= show_height * cutting_y)
			// Oben angepackt
			show_press_sector = 0;
		else if (y >= show_height * (cutting_y + cutting_height))
			// Unten angepackt
			show_press_sector = 1;
		else
		// mittlere H�he angepackt
		if (x <= show_width * cutting_x)
			// Rechts
			show_press_sector = 2;
		else if (x >= show_width * (cutting_x + cutting_width))
			// Rechts
			show_press_sector = 3;
		else
			// Mitte
			show_press_sector = 4;
	}

	private void manageCanvasDragInput(MouseEvent e) {

		double x = e.getX() - show_pos_x;
		double y = e.getY() - show_pos_y;
		double dx = show_press_x - x;
		double dy = show_press_y - y;
		double rel_dx = dx / show_width;
		double rel_dy = dy / show_height;
		show_press_x = x;
		show_press_y = y;

		switch (show_press_sector) {
		case 0:
			// Obere Latte justieren:
			if (((cutting_height + rel_dy) * show_height) <= MIN_CUT_SIZE)
				break;
			else if (cutting_y - rel_dy <= 0) // wenn aus dem Bild raus
				break;

			cutting_y -= rel_dy;
			cutting_height += rel_dy;
			canvasShowcase.draw();
			break;
		case 1:
			if (((cutting_height - rel_dy) * show_height) <= MIN_CUT_SIZE)
				break;
			else if (cutting_y + cutting_height - rel_dy >= 1) // wenn aus dem Bild raus
				break;

			cutting_height -= rel_dy;
			canvasShowcase.draw();
			break;
		case 2:
			if ((cutting_width + rel_dx) * show_width <= MIN_CUT_SIZE)
				break;
			else if (cutting_x - rel_dx < 0)
				break;

			cutting_x -= rel_dx;
			cutting_width += rel_dx;
			canvasShowcase.draw();
			break;
		case 3:
			if ((cutting_width - rel_dx) * show_width <= MIN_CUT_SIZE)
				break;
			else if (cutting_width + cutting_x - rel_dx >= 1)
				break;
			cutting_width -= rel_dx;
			canvasShowcase.draw();
			break;
		default:
			break; // Keine Aktion da Leerlauf oder Sektor ung�ltig
		}

	}

	private void initExitPanel() {
		
		layout_left_exit = new HBox();
		layout_left_exit_content = new GridPane();
		layout_left_exit_content.setPrefWidth(CANVAS_COLLECTION_WIDTH);
		layout_left_exit_content.setVgap(5);
		layout_left_exit_content.setHgap(10);
		
		Line l1 = new Line(0, 0, 0, 0);
		l1.endYProperty().bind(layout_root.heightProperty());
		l1.setStroke(COLOR_LINE);
		
		Label headlineExit = new Label("Beenden:");
		headlineExit.setStyle("-fx-text-fill: rgb(248,248,248);" + "	-fx-font-size: 17px; -fx-font-weight: bold;");
		headlineExit.setPadding(new Insets(5, 5, 0, 5));
		
		Button buttonFinish = new Button("Jetzt komplett beenden", getImageView(img_export, 40, 40));
		Button buttonSave = new Button("Sp�ter weiter sortieren", getImageView(img_import, 40, 40));
		buttonFinish.setFont(Font.font(16));
		buttonSave.setFont(Font.font(16));
		buttonSave.setPadding(new Insets(10, 95, 10, 10));
		buttonFinish.setPadding(new Insets(10, 88, 10, 10));
		GridPane.setMargin(buttonSave, new Insets(5, 10, 5, 10));
		GridPane.setMargin(buttonFinish, new Insets(5, 10, 5, 10));
		buttonSave.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileManager.saveWorkingSet(workingSet);
			}
		});
		
		Button buttonAbort = new Button("Abbrechen", getImageView(img_red_x, 30, 30));
		buttonAbort.setPrefHeight(40);
		buttonAbort.setPadding(new Insets(10, 48, 10, 12));
		GridPane.setMargin(buttonAbort, new Insets(40, 0, 0, 10));
		buttonAbort.setOnAction(e -> closeExitPanel());
		
		layout_left_exit_content.add(headlineExit, 0, 0, 2, 1);
		layout_left_exit_content.add(genLine(), 0, 1, 2, 1);
		layout_left_exit_content.add(buttonSave, 0, 2, 2, 1);
		layout_left_exit_content.add(buttonFinish, 0, 3, 2, 1);
		layout_left_exit_content.add(buttonAbort, 0, 4);
		layout_left_exit.getChildren().addAll(layout_left_exit_content, l1);
		
		
	}
	
	private void openExitPanel() {
		layout_root.setLeft(layout_left_exit);
		storeImage = currentImage;
		currentImage = null;
		canvasShowcase.draw();
	}
	
	private void closeExitPanel() {
		layout_root.setLeft(layout_left_basis);
		currentImage = storeImage;
		canvasShowcase.draw();
	}
	
	private void initOptionsPanel() {

		layout_left_options = new HBox();
		layout_left_options_content = new GridPane();
		layout_left_options_content.setPrefWidth(CANVAS_COLLECTION_WIDTH);
		layout_left_options_content.setVgap(5);
		layout_left_options_content.setHgap(10);

		Line l1 = new Line(0, 0, 0, 0);
		l1.endYProperty().bind(layout_root.heightProperty());
		l1.setStroke(COLOR_LINE);
		layout_left_basis.getChildren().add(l1);

		Label headlineOptions = new Label("Bild behalten:");
		headlineOptions.setStyle("-fx-text-fill: rgb(248,248,248);" + "	-fx-font-size: 17px; -fx-font-weight: bold;");
		headlineOptions.setPadding(new Insets(5, 5, 0, 5));

		Label headlineOriginal = new Label("Das Original");
		headlineOriginal.setPadding(new Insets(10, 5, 0, 15));

		checkOriginalSave = new CheckBox("behalten");
		checkOriginalDelete = new CheckBox("l�schen");
		checkOriginalSave.setPadding(new Insets(0, 0, 0, 0));
		checkOriginalDelete.setPadding(new Insets(0, 0, 10, 0));
		checkOriginalSave.setSelected(true);
		checkOriginalDelete.setSelected(false);
		checkOriginalSave.setOnAction(e -> checkOriginalDelete.setSelected(!checkOriginalDelete.isSelected()));
		checkOriginalDelete.setOnAction(e -> checkOriginalSave.setSelected(!checkOriginalSave.isSelected()));

		Line l0 = new Line(0, 0, CANVAS_COLLECTION_WIDTH, 0);
		l0.setStroke(COLOR_LINE);
		Line l2 = new Line(0, 0, CANVAS_COLLECTION_WIDTH, 0);
		l2.setStroke(COLOR_LINE);
		Line l3 = new Line(0, 0, CANVAS_COLLECTION_WIDTH, 0);
		l3.setStroke(COLOR_LINE);

		Label headlineCopy = new Label("Eine Kopie");
		headlineCopy.setPadding(new Insets(10, 5, 0, 15));

		checkCopySave = new CheckBox("anfertigen");
		checkCopySave.setPadding(new Insets(0, 0, 0, 0));

		ImageView imgview_scissors = getImageView(img_scissors, 50, 50);
		buttonCut = new Button("Zuschneiden", imgview_scissors);
		buttonCut.setPadding(new Insets(0, 90, 0, 90));
		buttonCut.setDisable(true);
		GridPane.setMargin(buttonCut, new Insets(10, 0, 10, 0));
		GridPane.setHalignment(buttonCut, HPos.CENTER);
		checkCopySave.setOnAction(e -> buttonCut.setDisable(!checkCopySave.isSelected()));

		// Zuschneide-Pane: ----
		HBox layout_cut = new HBox();
		GridPane layout_cut_content = new GridPane();
		layout_cut_content.setPrefWidth(CANVAS_COLLECTION_WIDTH);

		Line l11 = new Line(0, 0, 0, 0);
		l11.endYProperty().bind(layout_root.heightProperty());
		l11.setStroke(COLOR_LINE);

		Label headlineCut = new Label("Bild zuschneiden:");
		headlineCut.setStyle("-fx-text-fill: rgb(248,248,248);" + "	-fx-font-size: 17px; -fx-font-weight: bold;");
		headlineCut.setPadding(new Insets(5, 5, 0, 5));
		Label labelCutWarning = new Label("Merke: Je kleiner das Bild, \ndesto schlechter die Aufl�sung");
		labelCutWarning.setPadding(new Insets(5, 5, 0, 15));

		layout_cut.getChildren().addAll(layout_cut_content, l11);

		ImageView imgview_green_tick = getImageView(img_green_tick, 40, 40);
		ImageView imgview_red_x = getImageView(img_red_x, 40, 40);
		Button buttonCutOk = new Button("Zuschneiden", imgview_green_tick);
		Button buttonCutCancel = new Button("Abbruch", imgview_red_x);
		buttonCutOk.setPadding(new Insets(10, 25, 10, 25));
		buttonCutCancel.setPadding(new Insets(10, 15, 10, 15));
		GridPane.setMargin(buttonCutOk, new Insets(40, 0, 0, 0));
		GridPane.setMargin(buttonCutCancel, new Insets(40, 0, 0, 15));
		buttonCutCancel.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				cuttingMode = false;
				layout_root.setLeft(layout_left_options);
			}
		});

		buttonCutOk.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				currentImage = cut(currentImage, cutting_x, cutting_y, cutting_width, cutting_height, turningAngle);
				layout_root.setLeft(layout_left_options);
				cuttingMode = false;
			}
		});

		layout_cut_content.add(headlineCut, 0, 0);
		layout_cut_content.add(labelCutWarning, 0, 1, 2, 1);
		layout_cut_content.add(buttonCutCancel, 0, 2);
		layout_cut_content.add(buttonCutOk, 1, 2);

		buttonCut.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				layout_root.setLeft(layout_cut);
				cuttingMode = true;
			}
		});

		// ---

		Button buttonFinish = new Button("OK");
		Button buttonAbort = new Button("Abbrechen");
		buttonFinish.setPadding(new Insets(15, 50, 15, 50));
		buttonAbort.setPadding(new Insets(15, 50, 15, 50));
		GridPane.setMargin(buttonFinish, new Insets(10, 0, 10, 12));
		GridPane.setMargin(buttonAbort, new Insets(10, 10, 10, 0));
		GridPane.setHalignment(buttonFinish, HPos.RIGHT);
		GridPane.setHalignment(buttonAbort, HPos.LEFT);
		buttonAbort.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				currentImage = storeImage;
				closeOptionsPanel();
			}
		});
		buttonFinish.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				
				if (checkOriginalSave.isSelected()) // Original behalten
					workingSet.getBase_keep().add(storeImage);
				
				if (checkCopySave.isSelected()) // Kopie anfertigen
					workingSet.getCopy().add(currentImage);
				
				workingSet.getBase().remove(storeImage);
				closeOptionsPanel();
				if (workingSet.getBase().size() > 0)
					currentImage = workingSet.getBase().get(workingSet.getBase().size() - 1);
				else {
					button_action_save.setDisable(true);
					button_action_delete.setDisable(true);
					currentImage = null;
				}
				updateImageAmounts();
				new Thread(() -> Platform.runLater(() -> redrawCollectionCanvas())).start();
				canvasShowcase.draw();
			}
		});

		layout_left_options_content.add(headlineOptions, 0, 0);
		layout_left_options_content.add(l0, 0, 1, 2, 1);
		layout_left_options_content.add(headlineOriginal, 0, 2);
		layout_left_options_content.add(checkOriginalSave, 1, 3);
		layout_left_options_content.add(checkOriginalDelete, 1, 4);
		layout_left_options_content.add(l2, 0, 5, 2, 1);
		layout_left_options_content.add(headlineCopy, 0, 6);
		layout_left_options_content.add(checkCopySave, 1, 7);
		layout_left_options_content.add(buttonCut, 0, 8, 2, 1);
		layout_left_options_content.add(l3, 0, 9, 2, 1);
		layout_left_options_content.add(buttonFinish, 0, 10);
		layout_left_options_content.add(buttonAbort, 1, 10);
		layout_left_options.getChildren().addAll(layout_left_options_content, l1);

	}

	private void openOptionsPanel() {
		storeImage = currentImage;
		layout_root.setLeft(layout_left_options);
		checkOriginalDelete.setSelected(false);
		checkOriginalSave.setSelected(true);
		checkCopySave.setSelected(false);
		buttonCut.setDisable(true);
	}

	private void closeOptionsPanel() {
		layout_root.setLeft(layout_left_basis);
		canvasShowcase.draw();
	}

	private void updateImageAmounts() {
		label_trash.setText("Gel�scht\n(" + workingSet.getTrash().size() + ")");
		label_original.setText("�brige Originale\n(" + workingSet.getBase().size() + ")");
		label_copy.setText("Kopien\n(" + workingSet.getCopy().size() + ")");
	}

	private void rotateLeft() {
		turningAngle -= 90;
		if (turningAngle < 0)
			turningAngle += 360;
		canvasShowcase.draw();
	}

	private void rotateRight() {
		turningAngle += 90;
		if (turningAngle >= 360)
			turningAngle -= 360;
		canvasShowcase.draw();
	}

	private void action_keepImage() {
		openOptionsPanel();
	}

	private void action_deleteImage() {

		workingSet.getBase().remove(currentImage);
		workingSet.getTrash().add(currentImage);
		if (workingSet.getBase().size() > 0)
			currentImage = workingSet.getBase().get(workingSet.getBase().size() - 1);
		else {
			button_action_save.setDisable(true);
			button_action_delete.setDisable(true);
			currentImage = null;
		}
		updateImageAmounts();
		new Thread(() -> Platform.runLater(() -> redrawCollectionCanvas())).start();
		canvasShowcase.draw();
	}

	private final Drawable DRAWABLE_SHOWCASE_CANVAS = new Drawable() {

		@Override
		public void draw(double width, double height, GraphicsContext g) {

			g.clearRect(0, 0, width, height);

			if (currentImage == null)
				return;

			Image snapshot = null;
			ImageView currentImage_view = new ImageView(currentImage);
			currentImage_view.setRotate(turningAngle);
			snapshot = currentImage_view.snapshot(null, null);

			boolean verticalFreeSpace = false;
			if (width / height < (snapshot.getWidth() / snapshot.getHeight()))
				verticalFreeSpace = true;

			if (cuttingMode) {
				// kleineres Bild zum Anpacken
				if (verticalFreeSpace) {
					double imgHeight = width * (snapshot.getHeight() / snapshot.getWidth());
					g.drawImage(snapshot, GRIP_HEIGHT, 0.5 * (height - imgHeight) + GRIP_HEIGHT,
							width - 2 * GRIP_HEIGHT, imgHeight - 2 * GRIP_HEIGHT);
					show_pos_x = GRIP_HEIGHT;
					show_pos_y = 0.5 * (height - imgHeight) + GRIP_HEIGHT;
					show_width = width - 2 * GRIP_HEIGHT;
					show_height = imgHeight - 2 * GRIP_HEIGHT;
				} else {
					double imgWidth = height * (snapshot.getWidth() / snapshot.getHeight());
					g.drawImage(snapshot, 0.5 * (width - imgWidth) + GRIP_HEIGHT, GRIP_HEIGHT,
							imgWidth - 2 * GRIP_HEIGHT, height - 2 * GRIP_HEIGHT);
					show_pos_x = 0.5 * (width - imgWidth) + GRIP_HEIGHT;
					show_pos_y = GRIP_HEIGHT;
					show_width = imgWidth - 2 * GRIP_HEIGHT;
					show_height = height - 2 * GRIP_HEIGHT;
				}

			} else {
				if (verticalFreeSpace) {
					double imgHeight = width * (snapshot.getHeight() / snapshot.getWidth());
					g.drawImage(snapshot, 0, 0.5 * (height - imgHeight), width, imgHeight);
					show_pos_x = 0;
					show_pos_y = 0.5 * (height - imgHeight);
					show_width = width;
					show_height = imgHeight;
				} else {
					double imgWidth = height * (snapshot.getWidth() / snapshot.getHeight());
					g.drawImage(snapshot, 0.5 * (width - imgWidth), 0, imgWidth, height);
					show_pos_x = 0.5 * (width - imgWidth);
					show_pos_y = 0;
					show_width = imgWidth;
					show_height = height;
				}
			}

			// Cutting-Mode:
			if (!cuttingMode)
				return;

			g.setFill(COLOR_BLEND);
			drawBorder(g, COLOR_BLEND, 1000, show_pos_x, show_pos_y, show_width, show_height, cutting_x, cutting_y,
					cutting_width, cutting_height);
			drawBorder(g, COLOR_CUTTING_FRAME, 4, show_pos_x, show_pos_y, show_width, show_height, cutting_x, cutting_y,
					cutting_width, cutting_height);
			drawGrips(g, show_pos_x, show_pos_y, show_width, show_height, cutting_x, cutting_y, cutting_width,
					cutting_height);
			drawBorder(g, Color.BLACK, 1.5, show_pos_x, show_pos_y, show_width, show_height, cutting_x, cutting_y,
					cutting_width, cutting_height);

		}
	};

	private static void drawBorder(GraphicsContext graphics, Color color, double thickness, double x, double y,
			double width, double height, double b_x, double b_y, double b_width, double b_height) {
		double t = thickness;
		graphics.setFill(color);
		graphics.fillRect(x + (width * b_x) - t, y + (height * b_y) - t, width * b_width + 2 * t, t);
		graphics.fillRect(x + width * b_x - t, y + (height * b_y) - 0.35, t, height * b_height + 0.7);
		graphics.fillRect(x + width * b_x - t, y + (height * (b_y + b_height)), width * b_width + 2 * t, t);
		graphics.fillRect(x + width * (b_x + b_width), y + height * b_y - 0.35, t, height * b_height + 0.7);
	}

	private static void drawGrips(GraphicsContext graphics, double x, double y, double width, double height, double b_x,
			double b_y, double b_width, double b_height) {
		double arc_deepth_out = 5;
		double arc_deepth_in = 3;
		double grip_height = 2;
		double grip_dx = 5;
		graphics.setFill(COLOR_CUTTING_FRAME);
		graphics.fillRoundRect(x + width * b_x + 0.5 * width * b_width - GRIP_LENGTH * 0.5,
				y + height * b_y - GRIP_HEIGHT, GRIP_LENGTH, GRIP_HEIGHT, arc_deepth_out, arc_deepth_out);
		graphics.fillRoundRect(x + width * b_x - GRIP_HEIGHT,
				y + height * b_y + height * b_height * 0.5 - 0.5 * GRIP_LENGTH, GRIP_HEIGHT, GRIP_LENGTH,
				arc_deepth_out, arc_deepth_out);
		graphics.fillRoundRect(x + width * b_x + 0.5 * width * b_width - GRIP_LENGTH * 0.5,
				y + height * (b_y + b_height), GRIP_LENGTH, GRIP_HEIGHT, arc_deepth_out, arc_deepth_out);
		graphics.fillRoundRect(x + width * (b_x + b_width),
				y + height * b_y + height * b_height * 0.5 - 0.5 * GRIP_LENGTH, GRIP_HEIGHT, GRIP_LENGTH,
				arc_deepth_out, arc_deepth_out);
		graphics.setFill(Color.BLACK);
		for (double a = 0.2; a < 0.8; a += 0.2) {
			graphics.fillRoundRect(x + width * b_x + 0.5 * width * b_width - GRIP_LENGTH * 0.5 + grip_dx,
					y + height * b_y - GRIP_HEIGHT + a * GRIP_HEIGHT - 0.5 * grip_height, GRIP_LENGTH - 2 * grip_dx,
					grip_height, arc_deepth_in, arc_deepth_in);
			graphics.fillRoundRect(x + width * b_x - GRIP_HEIGHT + GRIP_HEIGHT * a - 0.5 * grip_height,
					y + height * b_y + height * b_height * 0.5 - 0.5 * GRIP_LENGTH + grip_dx, grip_height,
					GRIP_LENGTH - 2 * grip_dx, arc_deepth_in, arc_deepth_in);
			graphics.fillRoundRect(x + width * b_x + 0.5 * width * b_width - GRIP_LENGTH * 0.5 + grip_dx,
					y + height * (b_y + b_height) + GRIP_HEIGHT * (1 - a), GRIP_LENGTH - 2 * grip_dx, grip_height,
					arc_deepth_in, arc_deepth_in);
			graphics.fillRoundRect(x + width * (b_x + b_width) + (1 - a) * GRIP_HEIGHT,
					y + height * b_y + height * b_height * 0.5 - 0.5 * GRIP_LENGTH + grip_dx, grip_height,
					GRIP_LENGTH - 2 * grip_dx, arc_deepth_in, arc_deepth_in);
		}
	}

	private void redrawCollectionCanvas() {
		g0.clearRect(0, 0, CANVAS_COLLECTION_WIDTH, CANVAS_COLLECTION_HEIGHT);
		double icon_basex = 20;
		double icon_basey = CANVAS_COLLECTION_HEIGHT - (img_trash_background.getHeight() * 0.15f) + 20;
		double icon_spacex = 15;

		// background
		g0.drawImage(img_trash_background, icon_basex, icon_basey, img_trash_background.getWidth() * 0.125f,
				img_trash_background.getHeight() * 0.125f);

		g0.drawImage(img_box_background, icon_basex + (img_trash_background.getWidth() * 0.125f) + icon_spacex,
				icon_basey + 34, img_box_background.getWidth() * 0.19f, img_box_background.getHeight() * 0.19f);

		// Original - Stapel
		double img_base_canvasheight = icon_basey + 1;
		double img_base_spacey = (double) img_base_canvasheight / workingSet.getStartSize();
		double img_base_y = img_base_canvasheight;
		for (Image i : workingSet.getBase()) {
			g0.drawImage(i, icon_basex + (img_trash_background.getWidth() * 0.125f) + 34, img_base_y,
					(img_box_background.getWidth() * 0.19f) - 40, (img_box_background.getWidth() * 0.19f) - 40);
			if (workingSet.getStartSize() < 38) {
				// Schatten
				LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
						new Stop(0, new Color(0, 0, 0, 0.8)), new Stop(1, Color.TRANSPARENT));
				g0.setFill(gradient);
				g0.fillRect(icon_basex + (img_trash_background.getWidth() * 0.125f) + 34,
						img_base_y + ((img_box_background.getWidth() * 0.19f) - 40),
						(img_box_background.getWidth() * 0.19f) - 40, 6);
			}
			img_base_y -= img_base_spacey;
		}

		// M�ll - Stapel
		double img_trash_canvasheight = icon_basey - 50;
		double img_trash_spacey = (double) img_trash_canvasheight / workingSet.getStartSize();
		double img_trash_y = img_trash_canvasheight;
		for (Image i : workingSet.getTrash()) {
			g0.drawImage(i, icon_basex + 3, img_trash_y, (img_box_background.getWidth() * 0.19f) - 40,
					(img_box_background.getWidth() * 0.19f) - 40);
			if (workingSet.getStartSize() < 38) {
				// Schatten
				LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
						new Stop(0, new Color(0, 0, 0, 0.8)), new Stop(1, Color.TRANSPARENT));
				g0.setFill(gradient);
				g0.fillRect(icon_basex + 3, img_trash_y + ((img_box_background.getWidth() * 0.19f) - 40),
						(img_box_background.getWidth() * 0.19f) - 40, 6);
			}
			img_trash_y -= img_trash_spacey;
		}

		// Kopie - Stapel
		double img_copy_canvasheight = icon_basey + 20;
		double img_copy_spacey = (double) img_copy_canvasheight / workingSet.getStartSize();
		double img_copy_y = img_copy_canvasheight;
		boolean firstImage = true;
		for (Image i : workingSet.getCopy()) {
			g0.drawImage(i, icon_basex + (2 * img_trash_background.getWidth() * 0.125f) + 67, img_copy_y,
					(img_box_background.getWidth() * 0.19f) - 40, (img_box_background.getWidth() * 0.19f) - 40);
			if (workingSet.getStartSize() < 38 && !firstImage) {
				// Schatten
				LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
						new Stop(0, new Color(0, 0, 0, 0.8)), new Stop(1, Color.TRANSPARENT));
				g0.setFill(gradient);
				g0.fillRect(icon_basex + (2 * img_trash_background.getWidth() * 0.125f) + 67,
						img_copy_y + ((img_box_background.getWidth() * 0.19f) - 40),
						(img_box_background.getWidth() * 0.19f) - 40, 6);
			}
			firstImage = false;
			img_copy_y -= img_copy_spacey;
		}

		// foreground
		g0.drawImage(img_trash_foreground, icon_basex, icon_basey, img_trash_foreground.getWidth() * 0.125f,
				img_trash_foreground.getHeight() * 0.125f);

		g0.drawImage(img_box_foreground, icon_basex + (img_box_foreground.getWidth() * 0.125f) + icon_spacex,
				icon_basey + 34, img_box_foreground.getWidth() * 0.19f, img_box_foreground.getHeight() * 0.19f);

	}

	public static ImageView getImageView(Image img, int width, int height) {
		ImageView r = new ImageView(img);
		r.setFitWidth(width);
		r.setFitHeight(height);
		return r;
	}

	private Line genLine() {
		Line l = new Line(0, 0, CANVAS_COLLECTION_WIDTH, 0);
		l.setStroke(COLOR_LINE);
		return l;
	}

}