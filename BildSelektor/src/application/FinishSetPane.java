package application;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import javax.swing.filechooser.FileSystemView;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;

public class FinishSetPane {

	private static final int PREVIEW_WIDTH = 220;
	private static final int PREVIEW_HEIGHT = 350;
	private static final Color COLOR_BORDER = new Color(0.8, 0.8, 0.8, 1.0);

	private BorderPane layout_root = new BorderPane();
	private BufferedWorkingSet workingSet;

	public FinishSetPane(BufferedWorkingSet ws, MainWindow mainWindow) {
		workingSet = ws;

		Label labelHeadline = new Label("Diese Originale werden behalten: ");
		labelHeadline.setStyle("-fx-text-fill: rgb(248,248,248);" + "	-fx-font-size: 29px; ");
		labelHeadline.setPadding(new Insets(10, 10, 5, 20));

		Button buttonAbort = new Button("Abbruch", MainWindow.getImageView(MainWindow.img_red_x, 40, 40));
		buttonAbort.setFont(Font.font(15));
		HBox.setMargin(buttonAbort, new Insets(10));
		buttonAbort.setOnAction(e -> mainWindow.resetView());

		Button buttonNext = new Button("Weiter", MainWindow.getImageView(MainWindow.img_green_tick, 40, 40));
		buttonNext.setFont(Font.font(15));
		HBox.setMargin(buttonNext, new Insets(10));
		buttonAbort.widthProperty().addListener((e, d, c) -> buttonNext.setPrefWidth(buttonAbort.getWidth()));
		buttonAbort.heightProperty().addListener((e, d, c) -> buttonNext.setPrefHeight(buttonAbort.getHeight()));

		HBox layoutBottom = new HBox();
		Pane spaceholderBottom = new Pane();
		layoutBottom.getChildren().addAll(buttonAbort, spaceholderBottom, buttonNext);
		layoutBottom.prefWidthProperty().bind(layout_root.widthProperty());
		HBox.setHgrow(spaceholderBottom, Priority.ALWAYS);

		VBox centerLowest = new VBox();
		Pane p0 = new Pane();
		Pane p1 = new Pane();
		VBox.setVgrow(p0, Priority.ALWAYS);
		VBox.setVgrow(p1, Priority.ALWAYS);

		HBox layoutPreviews = new HBox();
		layoutPreviews.setSpacing(15);
		ScrollPane scrollPreviews = new ScrollPane(layoutPreviews);
		scrollPreviews.maxWidthProperty().bind(layout_root.widthProperty().subtract(10));
		scrollPreviews.setFitToHeight(false);
		scrollPreviews.setFitToWidth(true);
		scrollPreviews.setVbarPolicy(ScrollBarPolicy.NEVER);
		scrollPreviews.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		BorderPane.setMargin(scrollPreviews, new Insets(0, 10, 0, 10));
		VBox.setMargin(scrollPreviews, new Insets(0, 5, 0, 5));

		centerLowest.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (layoutPreviews.getWidth() + 10 < layout_root.getWidth())
					scrollPreviews.setHbarPolicy(ScrollBarPolicy.NEVER);
				else
					scrollPreviews.setHbarPolicy(ScrollBarPolicy.ALWAYS);

			}
		});

		int[] state = new int[] { 0 }; // 0 -> keep; 1 -> copy; 2 -> trash
		ArrayList<ArrayList<String>> toremove = new ArrayList<ArrayList<String>>();
		toremove.add(new ArrayList<String>()); // Originale zu löschen
		toremove.add(new ArrayList<String>()); // Kopien zu löschen
		toremove.add(new ArrayList<String>()); // Doch nicht zu Löschendes

		ArrayList<String> tokeep = new ArrayList<String>();
		tokeep.addAll(ws.getIndex_base());
		tokeep.addAll(ws.getIndex_base_keep());
		if (ws.getAddImage() != null)
			tokeep.add(ws.getAddImage().getName());
		if (tokeep.size() > 0) {
			state[0] = 1;
			toremove.set(0, initKeepList(tokeep, layoutPreviews, workingSet));
		} else if (ws.getIndex_copy().size() > 0) {
			state[0] = 2;
			toremove.set(1, initCopyList(ws.getIndex_copy(), layoutPreviews, workingSet));
			labelHeadline.setText("Diese Kopien werden angefertigt");
			scrollPreviews.setHvalue(0);
		} else if (ws.getIndex_trash().size() > 0) {
			state[0] = 3;
			toremove.set(2, initTrashList(ws.getIndex_trash(), layoutPreviews, workingSet));
			labelHeadline.setText("Diese Bilder werden gelöscht");
			scrollPreviews.setHvalue(0);
		} else {
			state[0] = 4;
			// Speicher Ort auswählen
			applySavingPane(toremove, mainWindow);
		}

		buttonNext.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (state[0] == 1) {
					state[0] = 2;
					layoutPreviews.getChildren().clear();
					if (ws.getIndex_copy().size() > 0) {
						toremove.set(1, initCopyList(ws.getIndex_copy(), layoutPreviews, workingSet));
						labelHeadline.setText("Diese Kopien werden angefertigt");
						scrollPreviews.setHvalue(0);
					} else if (ws.getIndex_trash().size() + toremove.get(0).size() + toremove.get(1).size() > 0) {
						state[0] = 3;
						ArrayList<String> alltoremove = new ArrayList<String>(ws.getIndex_trash());
						alltoremove.addAll(toremove.get(0));
						alltoremove.addAll(toremove.get(1));
						toremove.set(2, initTrashList(alltoremove, layoutPreviews, workingSet));
						labelHeadline.setText("Diese Bilder werden gelöscht");
						scrollPreviews.setHvalue(0);
					} else {
						state[0] = 4;
						// Speicher Ort auswählen
						applySavingPane(toremove, mainWindow);
					}
				} else if (state[0] == 2) {
					state[0] = 3;
					layoutPreviews.getChildren().clear();
					if (ws.getIndex_trash().size() + toremove.get(0).size() + toremove.get(1).size() > 0) {
						ArrayList<String> alltoremove = new ArrayList<String>(ws.getIndex_trash());
						alltoremove.addAll(toremove.get(0));
						alltoremove.addAll(toremove.get(1));
						toremove.set(2, initTrashList(alltoremove, layoutPreviews, workingSet));
						labelHeadline.setText("Diese Bilder werden gelöscht");
						scrollPreviews.setHvalue(0);
					} else {
						// Speicher Ort auswählen
						applySavingPane(toremove, mainWindow);
						state[0] = 4;
					}
				} else if (state[0] == 3) {
					state[0] = 4;

					// Speicher Ort auswählen
					applySavingPane(toremove, mainWindow);

				}
			}
		});

		centerLowest.getChildren().addAll(p0, scrollPreviews, p1);
		layout_root.setCenter(centerLowest);
		layout_root.setTop(labelHeadline);
		layout_root.setBottom(layoutBottom);

		if (layoutPreviews.getWidth() + 35 < layout_root.getWidth())
			scrollPreviews.setHbarPolicy(ScrollBarPolicy.NEVER);
		else
			scrollPreviews.setHbarPolicy(ScrollBarPolicy.ALWAYS);

	}

	private void applySavingPane(ArrayList<ArrayList<String>> selectionResults, MainWindow mw) {

		String[] saveto = new String[] { "%empty", "%empty" };
		Flag choosingOriginals = Flag.active();

		Label labelHeadline = new Label("Wo sollen die Originale hin?");
		labelHeadline.setStyle("-fx-text-fill: rgb(248,248,248);" + "	-fx-font-size: 29px; -fx-font-wdight: bold;");
		labelHeadline.setPadding(new Insets(10, 10, 5, 20));

		HBox centerLowest = new HBox();
		Pane p0 = new Pane();
		HBox.setHgrow(p0, Priority.ALWAYS);

		Button buttonAbort = new Button("Abbruch", MainWindow.getImageView(MainWindow.img_red_x, 40, 40));
		buttonAbort.setFont(Font.font(15));
		HBox.setMargin(buttonAbort, new Insets(10));
		buttonAbort.setOnAction(e -> mw.resetView());

		HBox layoutBottom = new HBox();
		Pane spaceholderBottom = new Pane();
		layoutBottom.getChildren().addAll(buttonAbort, spaceholderBottom);
		layoutBottom.prefWidthProperty().bind(layout_root.widthProperty());
		HBox.setHgrow(spaceholderBottom, Priority.ALWAYS);

		Button buttonFolderSelect = new Button(" In einen ausgewähltem Ordner",
				MainWindow.getImageView(MainWindow.img_folder_search, 70, 70));
		Button buttonFolderDesktop = new Button(" In einen Ordner auf dem Desktop",
				MainWindow.getImageView(MainWindow.img_folder_desktop, 70, 70));
		Button buttonFolderImages = new Button(" In einen Ordner bei allen Bildern",
				MainWindow.getImageView(MainWindow.img_folder_images, 70, 70));
		Button buttonStick = new Button(" Auf ein externes Medium",
				MainWindow.getImageView(MainWindow.img_usbstick, 70, 70));
		buttonFolderSelect.setFont(Font.font(17));
		buttonFolderDesktop.setFont(Font.font(17));
		buttonFolderImages.setFont(Font.font(17));
		buttonStick.setFont(Font.font(17));
		buttonFolderSelect.setAlignment(Pos.CENTER_LEFT);
		buttonFolderDesktop.setAlignment(Pos.CENTER_LEFT);
		buttonFolderImages.setAlignment(Pos.CENTER_LEFT);
		buttonStick.setAlignment(Pos.CENTER_LEFT);
		buttonFolderSelect.prefWidthProperty().bind(labelHeadline.widthProperty());
		buttonFolderDesktop.prefWidthProperty().bind(labelHeadline.widthProperty());
		buttonFolderImages.prefWidthProperty().bind(labelHeadline.widthProperty());
		buttonStick.prefWidthProperty().bind(labelHeadline.widthProperty());

		VBox layoutButtons = new VBox();
		layoutButtons.setSpacing(10);
		HBox.setMargin(layoutButtons, new Insets(20));
		layoutButtons.getChildren().addAll(buttonFolderSelect, buttonFolderDesktop, buttonFolderImages, buttonStick);
		;
		centerLowest.getChildren().addAll(layoutButtons, p0);

		layout_root.setCenter(centerLowest);
		layout_root.setTop(labelHeadline);
		layout_root.setBottom(layoutBottom);

		buttonFolderSelect.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				DirectoryChooser dirchooser = new DirectoryChooser();
				dirchooser.setTitle("Ordner auswählen");
				File dir = dirchooser.showDialog(mw.getStage());

				if (dir == null)
					return; // Abbruch
				else {
					if (choosingOriginals.isActive()) {
						saveto[0] = dir.getAbsolutePath();
						choosingOriginals.setActive(false);
						labelHeadline.setText("Wo sollen die Kopien hin?");
					} else {
						// Finish
						saveto[1] = dir.getAbsolutePath();
						save(saveto, selectionResults, mw);
					}
				}

			}
		});

		buttonFolderDesktop.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (choosingOriginals.isActive()) {
					String supdir = System.getProperty("user.home") + "\\Desktop";
					saveto[0] = supdir + "\\" + genFolderName(supdir);
					choosingOriginals.setActive(false);
					labelHeadline.setText("Wo sollen die Kopien hin?");
				} else {
					// Finish
					String supdir = System.getProperty("user.home") + "\\Desktop";
					saveto[1] = supdir + "\\" + genFolderName(supdir, saveto[0]);
					save(saveto, selectionResults, mw);
				}
			}
		});

		buttonFolderImages.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				if (choosingOriginals.isActive()) {
					FileSystemView.getFileSystemView().getDefaultDirectory();
					String supdir = System.getProperty("user.home") + "\\Pictures";
					saveto[0] = supdir + "\\" + genFolderName(supdir);
					choosingOriginals.setActive(false);
					labelHeadline.setText("Wo sollen die Kopien hin?");
				} else {
					// Finish
					String supdir = System.getProperty("user.home") + "\\Pictures";
					saveto[1] = supdir + "\\" + genFolderName(supdir, saveto[0]);
					save(saveto, selectionResults, mw);
				}

			}
		});

		boolean[] usbWindowActive = new boolean[] { false };
		buttonStick.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				if (usbWindowActive[0])
					return;

				usbWindowActive[0] = true;

				new Thread(new Runnable() {

					@Override
					public void run() {

						File dir = USBSelector.openDialog(false);
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

						if (choosingOriginals.isActive()) {
							saveto[0] = dir.getAbsolutePath() + "\\" + genFolderName(dir.getAbsolutePath());
							choosingOriginals.setActive(false);
							Platform.runLater(() -> labelHeadline.setText("Wo sollen die Kopien hin?"));
						} else {
							// Finish
							saveto[1] = dir.getAbsolutePath() + "\\" + genFolderName(dir.getAbsolutePath(), saveto[0]);
							save(saveto, selectionResults, mw);
						}

					}
				}).start();
			}
		});

	}

	public static String genFolderName(String supdir) {

		String res = "Bilder ";
		Calendar c = Calendar.getInstance();
		res += c.get(Calendar.YEAR) + "-";
		res += c.get(Calendar.MONTH) + "-";
		res += c.get(Calendar.DAY_OF_MONTH);

		int highesnumber = 0;
		for (File f : new File(supdir).listFiles()) {

			String[] split = f.getName().split("_");

			if (split[0].equals(res) && split.length == 1) {
				highesnumber = 1;
				continue;
			}

			if (!split[0].equals(res) || split.length != 2)
				continue;

			int number = 0;
			try {
				number = Integer.parseInt(split[1]);
			} catch (NumberFormatException e) {
				continue;
			}

			if (number > highesnumber)
				highesnumber = number;
		}

		if (highesnumber != 0)
			res += "_" + (highesnumber + 1);

		return res;
	}

	public static String genFolderName(String supdir, String prevdir) {

		String res = "Bilder ";
		Calendar c = Calendar.getInstance();
		res += c.get(Calendar.YEAR) + "-";
		res += c.get(Calendar.MONTH) + "-";
		res += c.get(Calendar.DAY_OF_MONTH);

		int highesnumber = 0;
		for (File f : new File(supdir).listFiles()) {

			String[] split = f.getName().split("_");

			if (split[0].equals(res) && split.length == 1) {
				highesnumber = 1;
				continue;
			}

			if (!split[0].equals(res) || split.length != 2)
				continue;

			int number = 0;
			try {
				number = Integer.parseInt(split[1]);
			} catch (NumberFormatException e) {
				continue;
			}

			if (number > highesnumber)
				highesnumber = number;
		}

		if (highesnumber != 0)
			res += "__" + (highesnumber + 1);
		else if ((supdir + "\\" + res).equals(prevdir))
			res += "__2";

		return res;
	}

	private static void save(String[] dirs, ArrayList<ArrayList<String>> selectionResults, MainWindow mw) {

		BufferedWorkingSet ws = mw.getWorkingSet();
		long st = System.currentTimeMillis();

		ArrayList<String> finalOriginals = new ArrayList<String>();
		ArrayList<String> finalCopys = new ArrayList<String>();

		ArrayList<String> originals = new ArrayList<String>(ws.getIndex_base());
		originals.addAll(ws.getIndex_base_keep());
		if (ws.getAddImage() != null)
			originals.add(ws.getAddImage().getName());

		for (String n : originals)
			if (isContained(selectionResults.get(0), n)) {
				// Als "Doch löschen" makiert
				if (isContained(selectionResults.get(2), n))
					// Als "Trotzdem nicht löschen" makiert
					finalOriginals.add(n);

			} else
				finalOriginals.add(n);

		for (String n : ws.getIndex_copy())
			if (isContained(selectionResults.get(1), n)) {
				// Als "Doch löschen" makiert
				if (isContained(selectionResults.get(2), n))
					// Als "Trotzdem nicht löschen" makiert
					finalCopys.add(n);
			} else
				finalCopys.add(n);

		for (String n : selectionResults.get(2))
			if (isContained(ws.getIndex_trash(), n)) // Früher Aussortiert. Jetzt soll es aber doch behalten werden
				finalOriginals.add(n);

		int failed = 0;
		for (String n0 : finalOriginals) {
			boolean success = FileManager.switchDir(ws.getSourceDir().getAbsolutePath(), n0, dirs[0]);
			if (!success) {
				System.err.println("[WARN] Failed to save \t" + n0);
				failed++;
			}
		}

		for (String n1 : finalCopys) {
			boolean success = FileManager.switchDir(ws.getSourceDir().getAbsolutePath(), n1, dirs[1]);
			if (!success) {
				System.err.println("[WARN] Failed to save \t" + n1);
				failed++;
			}
		}

		System.out.println("[info] Saved all Images in " + ((System.currentTimeMillis() - st) / 1000) + " sec.  "
				+ failed + "/" + (finalCopys.size() + finalOriginals.size()) + " failed.");

		Platform.runLater(() -> mw.getStage().close());

		if (ws.getSourceDir() != BufferedWorkingSet.FOLDER_TEMP)
			FileManager.delDir(ws.getSourceDir()); // WorkingSet Entfernen da jetzt lückenhaft.

		openFinishWindow();
	}

	private static void openFinishWindow() {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				VBox layout_root = new VBox();
				Scene scene = new Scene(layout_root, 400, 200);
				Stage stage = new Stage();
				stage.setTitle("Fertig");
				stage.setResizable(false);
				stage.setScene(scene);
				scene.getStylesheets().add(MainWindow.global_style);
				Label headline = new Label("Speichern abgeschlossen");
				headline.setPadding(new Insets(10));
				Button buttonOk = new Button("OK");
				Button buttonNo = new Button("Nein");
				HBox layoutButtons = new HBox();
				layoutButtons.setAlignment(Pos.CENTER);
				layoutButtons.setSpacing(10);
				buttonOk.setPadding(new Insets(10, 20, 10, 20));
				buttonNo.setPadding(new Insets(10, 20, 10, 20));
				layoutButtons.getChildren().add(buttonOk);
				
				Runnable r_ok_pressed = new Runnable() {
					
					@Override
					public void run() {
						
						headline.setText("Ursprungsdateien löschen?");
						layoutButtons.getChildren().add(buttonNo);
						buttonOk.setText("Ja");
						buttonNo.setOnAction(e -> System.exit(1));
						buttonOk.setOnAction(new EventHandler<ActionEvent>() {
							
							@Override
							public void handle(ActionEvent event) {
								stage.close();
								Main.delStartFiles();
							}
						});
					}
				};

				layout_root.setSpacing(10);
				layout_root.getChildren().addAll(headline, layoutButtons);
				layout_root.setAlignment(Pos.CENTER);
				buttonOk.setOnAction(e -> Platform.runLater(r_ok_pressed));
				stage.setOnCloseRequest(e -> System.exit(1));

				stage.show();
			}
		});

	}

	private static ArrayList<String> initKeepList(Collection<String> index, HBox layout, BufferedWorkingSet ws) {

		ArrayList<String> toremove = new ArrayList<String>();

		for (String keep : index) {
			BorderPane preview = getPreview(ws.getPreview(keep).getImage(), "Doch löschen", "Doch nicht löschen",
					() -> toremove.add(keep), () -> toremove.remove(keep));
			HBox.setMargin(preview, new Insets(0, 0, 5, 0));
			layout.getChildren().add(preview);
		}

		return toremove;
	}

	private static ArrayList<String> initCopyList(Collection<String> index, HBox layout, BufferedWorkingSet ws) {

		ArrayList<String> toremove = new ArrayList<String>();

		for (String keep : index) {
			BorderPane preview = getPreview(ws.getPreview(keep).getImage(), "Verwerfen", "Doch nicht verwerfen",
					() -> toremove.add(keep), () -> toremove.remove(keep));
			HBox.setMargin(preview, new Insets(0, 0, 5, 0));
			layout.getChildren().add(preview);
		}

		return toremove;
	}

	private static ArrayList<String> initTrashList(Collection<String> index, HBox layout, BufferedWorkingSet ws) {

		ArrayList<String> toremove = new ArrayList<String>();

		for (String keep : index) {
			BorderPane preview = getPreview(ws.getPreview(keep).getImage(), "Doch nicht löschen", "Doch löschen",
					() -> toremove.add(keep), () -> toremove.remove(keep));
			HBox.setMargin(preview, new Insets(0, 0, 5, 0));
			layout.getChildren().add(preview);
		}

		return toremove;
	}

	private static BorderPane getPreview(Image img, String actionMessage, String reverseMessage, Runnable action,
			Runnable reverse) {

		Flag wasSelected = Flag.inactive();

		BorderPane res = new BorderPane();
		res.setStyle("-fx-background-color: rgb(65,65,65);	-fx-background-radius: 10;");
		res.setPrefSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
		res.setBorder(new Border(
				new BorderStroke(COLOR_BORDER, BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(2))));

		int imgHeight = (int) (img.getHeight() * (PREVIEW_WIDTH - 20) / img.getWidth());
		if (imgHeight > PREVIEW_HEIGHT - 60)
			imgHeight = PREVIEW_HEIGHT - 60;
		ImageView imgview = MainWindow.getImageView(img, (int) (PREVIEW_WIDTH - 20), imgHeight);
		BorderPane.setMargin(imgview, new Insets(10, 10, 0, 10));
		res.setTop(imgview);

		Button buttonAction = new Button(actionMessage);
		buttonAction.setPadding(new Insets(2));
		buttonAction.setPrefWidth(PREVIEW_WIDTH);
		buttonAction.setFont(Font.font(17));
		BorderPane.setAlignment(buttonAction, Pos.BOTTOM_CENTER);
		BorderPane.setMargin(buttonAction, new Insets(10));
		buttonAction.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				wasSelected.setActive(!wasSelected.isActive());

				if (wasSelected.isActive()) {

					res.setStyle("-fx-background-color: rgb(55,40,40);	-fx-background-radius: 10;");
					buttonAction.setText(reverseMessage);
					action.run();
				} else {

					res.setStyle("-fx-background-color: rgb(65,65,65);	-fx-background-radius: 10;");
					buttonAction.setText(actionMessage);
					reverse.run();
				}

			}
		});

		res.setBottom(buttonAction);

		return res;
	}

	public static boolean isContained(ArrayList<String> l, String s) {
		for (String s1 : l)
			if (s.equals(s1))
				return true;
		return false;
	}

	public BorderPane getLayout_root() {
		return layout_root;
	}

}
