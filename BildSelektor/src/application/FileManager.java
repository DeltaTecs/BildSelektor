package application;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
	public static final String NAME_INFO_ORDER = "order.info";
	private static final String FLAG_SPLIT = "%";

	public static void saveWorkingSetByCopying(BufferedWorkingSet ws) throws InterruptedException {

		long startTime = System.currentTimeMillis();

		WorkingSetInfo info = WorkingSetInfo.gen(ws.getInfo().getThumbnail(), ws.getInfo().getStartSize(),
				ws.getIndex_base().size() + (ws.getAddImage() != null ? 1 : 0));

		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_ORIGINAL_SEEN).mkdirs();
		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_ORIGINAL_NEW).mkdirs();
		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_COPY).mkdirs();
		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_TRASH).mkdirs();

		// Alle Dateien detektieren
		HashMap<String, File> files = new HashMap<String, File>();
		int fs = 0;
		// Ordner abgehen...
		System.out.println("[info] indexing existing files");
		for (File f : new File(ws.getSourceDir().getAbsolutePath() + REL_PATH_ORIGINAL_NEW).listFiles()) {
			if (!f.exists() || f.isDirectory())
				continue; // <<-- Fehler. Darf nicht passieren da nur bilder hier..
			files.put(ws.getOriginalKey(f.getName()), f);
			fs++;
		}
		for (File f : new File(ws.getSourceDir().getAbsolutePath() + REL_PATH_ORIGINAL_SEEN).listFiles()) {
			if (!f.exists() || f.isDirectory())
				continue; // <<-- Fehler. Darf nicht passieren da nur bilder hier..
			files.put(ws.getOriginalKey(f.getName()), f);
			fs++;
		}
		for (File f : new File(ws.getSourceDir().getAbsolutePath() + REL_PATH_TRASH).listFiles()) {
			if (!f.exists() || f.isDirectory())
				continue; // <<-- Fehler. Darf nicht passieren da nur bilder hier..
			files.put(ws.getOriginalKey(f.getName()), f);
			fs++;
		}
		while (ws.getRunningCopyTasks() > 0) {
			Thread.sleep(10);
		}
		for (File f : new File(ws.getSourceDir().getAbsolutePath() + REL_PATH_COPY).listFiles()) {
			if (!f.exists() || f.isDirectory())
				continue; // <<-- Fehler. Darf nicht passieren da nur bilder hier..
			files.put(ws.getOriginalKey(f.getName()), f);
			fs++;
		}
		Main.loadProgress = 0.1;
		double progressPerImage = 0.8 / fs;

		// Alle abspeichern..
		for (String nameOriginalNew : ws.getIndex_base()) {
			Image img = load(files.get(nameOriginalNew));
			Main.loadProgress += progressPerImage * 0.2;
			save(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_ORIGINAL_NEW, img,
					nameOriginalNew);
			Main.loadProgress += progressPerImage * 0.8;
			System.out.println("[info] saved " + nameOriginalNew + " (new)");
		}
		if (ws.getAddImage() != null) {
			String realName = ws.getOriginalKey(ws.getAddImage().getName());
			Image img = load(files.get(realName));
			Main.loadProgress += progressPerImage * 0.2;
			save(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_ORIGINAL_NEW, img, realName);
			Main.loadProgress += progressPerImage * 0.8;
			System.out.println("[info] saved " + realName + " (new)");
		}
		for (String nameOriginalSeen : ws.getIndex_base_keep()) {
			Image img = load(files.get(nameOriginalSeen));
			Main.loadProgress += progressPerImage * 0.2;
			save(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_ORIGINAL_SEEN, img,
					nameOriginalSeen);
			Main.loadProgress += progressPerImage * 0.8;
			System.out.println("[info] saved " + nameOriginalSeen + " (seen)");
		}
		for (String nameCopy : ws.getIndex_copy()) {
			Image img = load(files.get(nameCopy));
			Main.loadProgress += progressPerImage * 0.2;
			save(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_COPY, img, nameCopy);
			Main.loadProgress += progressPerImage * 0.8;
			System.out.println("[info] saved " + nameCopy + " (copy)");
		}
		for (String nameTrash : ws.getIndex_trash()) {
			Image img = load(files.get(nameTrash));
			Main.loadProgress += progressPerImage * 0.2;
			save(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_TRASH, img, nameTrash);
			Main.loadProgress += progressPerImage * 0.8;
			System.out.println("[info] saved " + nameTrash + " (trash)");
		}

		// Thumbnail
		try {
			ImageIO.write(SwingFXUtils.fromFXImage(info.getThumbnail(), null), "png",
					new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + "\\" + NAME_THUMBNAIL));
		} catch (IOException e) {
			System.err.println("Speichern des Thumbnails gescheitert");
			e.printStackTrace();
		}

		Main.loadProgress += 0.05;

		// Info
		File titleInfo = new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + "\\" + NAME_INFO_FILE);
		try {
			titleInfo.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + "\\" + NAME_INFO_FILE,
					"UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		writer.print(info.getTitle() + FLAG_SPLIT + info.getDate() + FLAG_SPLIT + info.getStartSize());
		writer.close();

		ArrayList<String> indexunseen = new ArrayList<String>();
		indexunseen.addAll(ws.getIndex_base());
		if (ws.getAddImage() != null)
			indexunseen.add(0, ws.getAddImage().getName());
		saveOrder(indexunseen, Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_ORIGINAL_NEW);
		saveOrder(ws.getIndex_base_keep(),
				Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_ORIGINAL_SEEN);
		saveOrder(ws.getIndex_trash(), Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_TRASH);
		saveOrder(ws.getIndex_copy(), Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_COPY);

		Main.loadProgress = 1.0;

		System.out.println("[info] finished saving: "
				+ ((int) (100 * (System.currentTimeMillis() - startTime) / (1000 * 60.0)) / 100.0) + " minutes used.");
	}

	public static void saveWorkingSetByMoving(BufferedWorkingSet ws) throws InterruptedException {

		WorkingSetInfo info = WorkingSetInfo.gen(ws.getInfo().getThumbnail(), ws.getInfo().getStartSize(),
				ws.getIndex_base().size() + (ws.getAddImage() != null ? 1 : 0));

		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_ORIGINAL_SEEN).mkdirs();
		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_ORIGINAL_NEW).mkdirs();
		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_COPY).mkdirs();
		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_TRASH).mkdirs();

		// Alle Dateien detektieren
		HashMap<String, File> files = new HashMap<String, File>();
		int fs = 0;
		// Ordner abgehen...
		System.out.println("[info] indexing existing files");
		for (File f : new File(ws.getSourceDir().getAbsolutePath() + REL_PATH_ORIGINAL_NEW).listFiles()) {
			if (!f.exists() || f.isDirectory())
				continue; // <<-- Fehler. Darf nicht passieren da nur bilder hier..
			files.put(ws.getOriginalKey(f.getName()), f);
			fs++;
		}
		for (File f : new File(ws.getSourceDir().getAbsolutePath() + REL_PATH_ORIGINAL_SEEN).listFiles()) {
			if (!f.exists() || f.isDirectory())
				continue; // <<-- Fehler. Darf nicht passieren da nur bilder hier..
			files.put(ws.getOriginalKey(f.getName()), f);
			fs++;
		}
		for (File f : new File(ws.getSourceDir().getAbsolutePath() + REL_PATH_TRASH).listFiles()) {
			if (!f.exists() || f.isDirectory())
				continue; // <<-- Fehler. Darf nicht passieren da nur bilder hier..
			files.put(ws.getOriginalKey(f.getName()), f);
			fs++;
		}
		while (ws.getRunningCopyTasks() > 0) {
			Thread.sleep(10);
		}
		for (File f : new File(ws.getSourceDir().getAbsolutePath() + REL_PATH_COPY).listFiles()) {
			if (!f.exists() || f.isDirectory())
				continue; // <<-- Fehler. Darf nicht passieren da nur bilder hier..
			files.put(ws.getOriginalKey(f.getName()), f);
			fs++;
		}

		Main.loadProgress = 0.2;
		double progressPerImage = 0.6 / fs;

		// Alle abspeichern..
		for (String nameOriginalNew : ws.getIndex_base()) {
			if (!files.get(nameOriginalNew).renameTo(new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader()
					+ REL_PATH_ORIGINAL_NEW + "\\" + nameOriginalNew)))
				System.err.println("[WARN] saving failed! File: " + nameOriginalNew);
			else
				System.out.println("[info] saved " + nameOriginalNew + " (new)");
			Main.loadProgress += progressPerImage;
		}
		if (ws.getAddImage() != null) {
			String realName = null;
			for (String name : files.keySet()) {
				if (name == null)
					continue; // WTF?

				if (name.equals(ws.getAddImage().getName())) {
					realName = name;
					break;
				}
			}
			if (realName == null)
				System.err.println("[WARN] was not able to save file correctly! AddImage of workingset not listed");
			if (!files.get(realName).renameTo(new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader()
					+ REL_PATH_ORIGINAL_NEW + "\\" + realName)))
				System.err.println("[WARN] saving failed! File: " + realName);
			else
				System.out.println("[info] saved " + ws.getAddImage().getName() + " (new)");
			Main.loadProgress += progressPerImage;
		}
		for (String nameOriginalSeen : ws.getIndex_base_keep()) {
			if (!files.get(nameOriginalSeen).renameTo(new File(Main.PATH + REL_PATH_WORKINGSETS + "\\"
					+ info.getHeader() + REL_PATH_ORIGINAL_SEEN + "\\" + nameOriginalSeen)))
				System.err.println("[WARN] saving failed! File: " + nameOriginalSeen);
			else
				System.out.println("[info] saved " + nameOriginalSeen + " (seen)");
			Main.loadProgress += progressPerImage;
		}
		for (String nameCopy : ws.getIndex_copy()) {
			if (!files.get(nameCopy).renameTo(new File(
					Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_COPY + "\\" + nameCopy)))
				System.err.println("[WARN] saving failed! File: " + nameCopy);
			else
				System.out.println("[info] saved " + nameCopy + " (copy)");
			Main.loadProgress += progressPerImage;
		}
		for (String nameTrash : ws.getIndex_trash()) {
			if (!files.get(nameTrash).renameTo(new File(
					Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_TRASH + "\\" + nameTrash)))
				System.err.println("[WARN] saving failed! File: " + nameTrash);
			else
				System.out.println("[info] saved " + nameTrash + " (trash)");
			Main.loadProgress += progressPerImage;
		}

		// Thumbnail
		try {
			ImageIO.write(SwingFXUtils.fromFXImage(info.getThumbnail(), null), "png",
					new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + "\\" + NAME_THUMBNAIL));
		} catch (IOException e) {
			System.err.println("Speichern des Thumbnails gescheitert");
			e.printStackTrace();
		}
		Main.loadProgress += 0.1;

		// Info
		File titleInfo = new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + "\\" + NAME_INFO_FILE);
		try {
			titleInfo.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + "\\" + NAME_INFO_FILE,
					"UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		writer.print(info.getTitle() + FLAG_SPLIT + info.getDate() + FLAG_SPLIT + info.getStartSize());
		writer.close();

		ArrayList<String> indexunseen = new ArrayList<String>();
		indexunseen.addAll(ws.getIndex_base());
		if (ws.getAddImage() != null)
			indexunseen.add(0, ws.getAddImage().getName());
		saveOrder(indexunseen, Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_ORIGINAL_NEW);
		saveOrder(ws.getIndex_base_keep(),
				Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_ORIGINAL_SEEN);
		saveOrder(ws.getIndex_trash(), Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_TRASH);
		saveOrder(ws.getIndex_copy(), Main.PATH + REL_PATH_WORKINGSETS + "\\" + info.getHeader() + REL_PATH_COPY);

		Main.loadProgress = 1.0;
		System.out.println("[info] saving finished.");
	}

	public static void overrideWorkingSet(String baseHeader, BufferedWorkingSet newWs, boolean copying)
			throws InterruptedException {

		if (!copying)
			saveWorkingSetByMoving(newWs);
		else
			saveWorkingSetByCopying(newWs);

		if (!baseHeader.equals(newWs.getInfo().getHeader())) {
			File baseDir = new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + baseHeader);
			delDir(baseDir);
		}
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
		int errors = 0;
		int i = 0;
		for (File f : dir.listFiles()) {

			if (!f.exists() || !f.isDirectory())
				continue;
			try {
				res[i] = parseWorkingSetInfo(f);
				i++;
			} catch (Exception e) {
				errors++;
			}
		}

		if (errors > 0) {
			System.err.println("[WARN] " + errors + " Working-Sets unreadable");
			WorkingSetInfo[] res1 = new WorkingSetInfo[amount - errors];
			for (int j = 0; j != amount - errors; j++)
				res1[j] = res[j];

			return res1;
		} else
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

		int unseen = 0;
		if (new File(dir.getAbsolutePath() + REL_PATH_ORIGINAL_NEW).listFiles() != null)
			for (File f0 : new File(dir.getAbsolutePath() + REL_PATH_ORIGINAL_NEW).listFiles())
				if (f0.exists() && !f0.isDirectory() && !f0.getName().equals(NAME_INFO_ORDER))
					unseen++;

		return new WorkingSetInfo(title, thumbnail, date, header, startSize, unseen);
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

	public static boolean switchDir(String source, String name, String dest) {
		boolean success = false;
		File f = searchForFile(new File(source), name);
		if (f == null)
			return false;
		new File(dest).mkdirs();
		success = f.renameTo(new File(dest + "\\" + name));
		return success;
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
			for (int x = 0; x < (int) base.getWidth(); x++)
				for (int y = 0; y < (int) base.getHeight(); y++) {
					int rgba = reader.getArgb(x, y);
					for (int dy = 0; dy < s && dy + (int) (y * s) < res.getHeight(); dy++)
						for (int dx = 0; dx < s && dx + (int) (x * s) < res.getWidth(); dx++) {
							writer.setArgb((int) (x * s) + dx, (int) (y * s) + dy, rgba);
						}
				}

		} catch (IndexOutOfBoundsException e) {
			System.err.println("[WARN] rescaling failed: FileManager.rescale(Image, int, boolean");
		}

		return res;

	}

	public static ArrayList<String> readOrder(String dir) {

		ArrayList<String> res = new ArrayList<String>();

		String content = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(dir + "\\" + NAME_INFO_ORDER)));
			content += reader.readLine();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] split = content.split(FLAG_SPLIT);
		for (String s0 : split)
			if (!s0.equals("null"))
				res.add(s0);
		return res;
	}

	private static void saveOrder(ArrayList<String> order, String dir) {
		// Info
		File orderInfo = new File(dir + "\\" + NAME_INFO_ORDER);
		try {
			orderInfo.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(orderInfo.getAbsolutePath(), "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String res = "";
		int index = 0;
		for (String s : order) {
			res += s;
			if (index != order.size() - 1)
				res += FLAG_SPLIT;
			index++;
		}

		writer.print(res);
		writer.close();
	}

	public static File searchForFile(File folder, String name) {

		if (!folder.exists() || !folder.isDirectory())
			return null;

		for (File f : folder.listFiles()) {

			if (!f.exists())
				continue;

			if (f.isDirectory()) {
				File f1 = searchForFile(f, name);
				if (f1 != null)
					return f1;
			}

			if (f.getName().equals(name))
				return f;
		}

		return null;
	}

}
