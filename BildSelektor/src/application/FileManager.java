package application;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class FileManager {

	private static final Random RANDOM = new Random(System.currentTimeMillis());
	public static final String REL_PATH_WORKINGSETS = "\\zuruekgelegt";
	public static final String REL_PATH_TRASH = "\\muell";
	public static final String REL_PATH_COPY = "\\kopien";
	public static final String REL_PATH_ORIGINAL_NEW = "\\ungesehen";
	public static final String REL_PATH_ORIGINAL_SEEN = "\\behaltene-originale";
	public static final String NAME_THUMBNAIL = "thumbnail.png";
	public static final String NAME_INFO_FILE = "title.info";
	private static final String FLAG_SPLIT = "%";

	public static void saveWorkingSet(BufferedWorkingSet ws) throws InterruptedException {
		
		long startTime = System.currentTimeMillis();

		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_ORIGINAL_SEEN).mkdirs();
		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_ORIGINAL_NEW).mkdirs();
		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_COPY).mkdirs();
		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_TRASH).mkdirs();

		// Alle Dateien detektieren
		HashMap<String, File> files = new HashMap<String, File>();
		// Ordner abgehen...
		System.out.println("[info] indexing existing files");
		for (File f : new File(ws.getSourceDir().getAbsolutePath() + REL_PATH_ORIGINAL_NEW).listFiles()) {
			if (!f.exists() || f.isDirectory())
				continue; // <<-- Fehler. Darf nicht passieren da nur bilder hier..
			files.put(ws.getOriginalKey(f.getName()), f);
		}
		for (File f : new File(ws.getSourceDir().getAbsolutePath() + REL_PATH_ORIGINAL_SEEN).listFiles()) {
			if (!f.exists() || f.isDirectory())
				continue; // <<-- Fehler. Darf nicht passieren da nur bilder hier..
			files.put(ws.getOriginalKey(f.getName()), f);
		}
		for (File f : new File(ws.getSourceDir().getAbsolutePath() + REL_PATH_TRASH).listFiles()) {
			if (!f.exists() || f.isDirectory())
				continue; // <<-- Fehler. Darf nicht passieren da nur bilder hier..
			files.put(ws.getOriginalKey(f.getName()), f);
		}
		while (ws.getRunningCopyTasks() > 0) {
			Thread.sleep(10);
		}
		for (File f : new File(ws.getSourceDir().getAbsolutePath() + REL_PATH_COPY).listFiles()) {
			if (!f.exists() || f.isDirectory())
				continue; // <<-- Fehler. Darf nicht passieren da nur bilder hier..
			files.put(ws.getOriginalKey(f.getName()), f);
		}

		// Alle abspeichern..
		for (String nameOriginalNew : ws.getIndex_base()) {
			Image img = load(files.get(nameOriginalNew));
			save(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_ORIGINAL_NEW, img);
			System.out.println("[info] saved " + nameOriginalNew + " (new)");
		}
		for (String nameOriginalSeen : ws.getIndex_base_keep()) {
			Image img = load(files.get(nameOriginalSeen));
			save(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_ORIGINAL_SEEN, img);
			System.out.println("[info] saved " + nameOriginalSeen + " (seen)");
		}
		for (String nameCopy : ws.getIndex_copy()) {
			Image img = load(files.get(nameCopy));
			save(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_COPY, img);
			System.out.println("[info] saved " + nameCopy + " (copy)");
		}
		for (String nameTrash : ws.getIndex_trash()) {
			Image img = load(files.get(nameTrash));
			save(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_TRASH, img);
			System.out.println("[info] saved " + nameTrash + " (trash)");
		}

		// Thumbnail
		try {
			ImageIO.write(SwingFXUtils.fromFXImage(ws.getInfo().getThumbnail(), null), "png", new File(
					Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + "\\" + NAME_THUMBNAIL));
		} catch (IOException e) {
			System.err.println("Speichern des Thumbnails gescheitert");
			e.printStackTrace();
		}

		// Info
		File titleInfo = new File(
				Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + "\\" + NAME_INFO_FILE);
		try {
			titleInfo.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(
					Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + "\\" + NAME_INFO_FILE,
					"UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		writer.print(ws.getInfo().getTitle() + FLAG_SPLIT + ws.getInfo().getDate() + FLAG_SPLIT
				+ ws.getInfo().getStartSize());
		writer.close();
		
		System.out.println("[info] finished saving: " + ((int)(100 * (System.currentTimeMillis() - startTime) / (1000 * 60.0)) / 100.0) + " minutes used.");
	}

	public static void overrideWorkingSet(String baseHeader, BufferedWorkingSet newWs) throws InterruptedException {

		File baseDir = new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + baseHeader);
		delDir(baseDir);

		saveWorkingSet(newWs);
	}

	public static WorkingSetInfo[] getWorkingSetInfos() {

		int amount = 0;
		File dir = new File(Main.PATH + REL_PATH_WORKINGSETS);
		if (dir.listFiles() == null)
			return new WorkingSetInfo[0];

		for (File f : dir.listFiles())
			if (f.exists() && f.isDirectory())
				amount++;

		WorkingSetInfo[] res = new WorkingSetInfo[amount];
		int i = 0;
		for (File f : dir.listFiles()) {

			if (!f.exists() || !f.isDirectory())
				continue;
			res[i] = parseWorkingSetInfo(f);
			i++;
		}
		return res;
	}

	public static WorkingSetInfo parseWorkingSetInfo(File dir) {
		File infoFile = new File(dir.getAbsolutePath() + "\\" + NAME_INFO_FILE);
		Image thumbnail = load(dir.getAbsolutePath() + "\\" + NAME_THUMBNAIL);
		String infoContent = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(infoFile));
			infoContent += reader.readLine();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String title = "Laden fehlgeschlagen";
		long date = System.currentTimeMillis();
		int startSize = 0;
		String header = "--";
		try {
			String[] infoContentSplit = infoContent.split(FLAG_SPLIT);
			title = infoContentSplit[0];
			date = Long.parseLong(infoContentSplit[1]);
			startSize = Integer.parseInt(infoContentSplit[2]);
			header = dir.getName();
		} catch (Exception e) {
			System.err.println("Failed to load info File for working set");
			e.printStackTrace();
		}
		return new WorkingSetInfo(title, thumbnail, date, header, startSize);
	}

	public static Image[] loadAll(String dir) {

		File[] files = new File(dir).listFiles();
		Image[] res = new Image[files.length];

		for (int i = 0; i != files.length; i++)
			res[i] = load(files[i]);

		return res;
	}

	public static void saveAll(String dir, Collection<Image> images) {
		for (Image i : images)
			save(dir, i);
	}

	public static Image load(String dir) {

		File f = new File(dir);
		if (!f.exists()) {
			System.out.println("Laden gescheitert: " + dir);
			return null;
		}

		try {
			return SwingFXUtils.toFXImage(ImageIO.read(f), null);
		} catch (IOException e) {
			System.out.println("Laden gescheitert: " + dir);
			e.printStackTrace();
			return null;
		}
	}

	public static Image load(File f) {

		try {
			return SwingFXUtils.toFXImage(ImageIO.read(f), null);
		} catch (IOException e) {
			System.out.println("Laden gescheitert: " + f.getAbsolutePath());
			e.printStackTrace();
			return null;
		}
	}

	public static void save(String dir, Image i) {

		String name = RANDOM.nextInt(Integer.MAX_VALUE) + (int) (System.currentTimeMillis() % 100000) + ".png";
		File f = new File(dir + "\\" + name);

		try {
			ImageIO.write(SwingFXUtils.fromFXImage(i, null), "png", f);
		} catch (IOException e) {
			System.err.println("Speichern gescheitert: " + dir + name);
			e.printStackTrace();
		}
	}

	public static void save(String dir, Image i, String name) {

		File f = new File(dir + "\\" + name);

		try {
			BufferedImage img = SwingFXUtils.fromFXImage(i, null);
			ImageIO.write(img, "png", f);
		} catch (IOException e) {
			System.err.println("Speichern gescheitert: " + dir + name);
			e.printStackTrace();
		}
	}
	
	public static void saveAndUpdateProgress(String dir, Image i, String name) {

		File f = new File(dir + "\\" + name);

		try {
			BufferedImage img = SwingFXUtils.fromFXImage(i, null);
			Main.loadProgress += Main.progressPerImage * 0.3;
			ImageIO.write(img, "png", f);
			Main.loadProgress += Main.progressPerImage * 0.3;
		} catch (IOException e) {
			System.err.println("Speichern gescheitert: " + dir + name);
			e.printStackTrace();
		}
	}

	public static void delDir(File dir) {

		if (dir.listFiles() != null)
			for (File f : dir.listFiles())
				if (f.isDirectory())
					delDir(f);
				else
					f.delete();

		dir.delete();
	}

	public static void delContent(File dir) {

		if (dir.listFiles() != null)
			for (File f : dir.listFiles())
				if (f.isDirectory())
					delDir(f);
				else
					f.delete();
	}

	public static Image rescale(Image base, int width, int height) {

		double sx = width / base.getWidth();
		double sy = height / base.getHeight();

		WritableImage res = new WritableImage(width, height);

		PixelReader reader = base.getPixelReader();
		PixelWriter writer = res.getPixelWriter();

		for (int x = 0; x != (int) base.getWidth(); x++)
			for (int y = 0; y != (int) base.getHeight(); y++) {
				int rgba = reader.getArgb(x, y);
				for (int dy = 0; dy < sx; dy++)
					for (int dx = 0; dx < sy; dx++)
						writer.setArgb((int) (x * sx) + dx, (int) (y * sy) + dy, rgba);
			}

		return res;

	}

	public static Image rescale(Image base, int size, boolean onWidth) {

		double s = 0;
		if (onWidth)
			s = size / base.getWidth();
		else
			s = size / base.getHeight();

		WritableImage res = null;
		if (onWidth)
			res = new WritableImage(size, (int) (base.getHeight() * s));
		else
			res = new WritableImage((int) (base.getWidth() * s), size);

		PixelReader reader = base.getPixelReader();
		PixelWriter writer = res.getPixelWriter();

		try {
			for (int x = 0; (x * s) < res.getWidth() && x < (int) base.getWidth(); x++)
				for (int y = 0; (y * s) < res.getHeight() && y < (int) base.getHeight(); y++) {
					int rgba = reader.getArgb(x, y);
					for (int dy = 0; dy < s; dy++)
						for (int dx = 0; dx < s; dx++) {
							writer.setArgb((int) (x * s) + dx, (int) (y * s) + dy, rgba);

						}
				}
		} catch (IndexOutOfBoundsException e) {
			// Shut up and pass.
			System.err.println("[WARN] indexOutOfBounds: FileManager.rescale(Image, int, boolean)");
		}

		return res;

	}

}
