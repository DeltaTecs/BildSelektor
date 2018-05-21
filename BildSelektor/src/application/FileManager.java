package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Random;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class FileManager {

	private static final Random RANDOM = new Random(System.currentTimeMillis());
	private static final String REL_PATH_WORKINGSETS = "\\zuruekgelegt";
	private static final String REL_PATH_TRASH = "\\muell";
	private static final String REL_PATH_COPY = "\\kopien";
	private static final String REL_PATH_ORIGINAL_NEW = "\\ungesehen";
	private static final String REL_PATH_ORIGINAL_SEEN = "\\behaltene-originale";
	private static final String NAME_THUMBNAIL = "thumbnail.png";
	private static final String NAME_INFO_FILE = "title.info";
	private static final String FLAG_SPLIT = "%";

	public static void saveWorkingSet(WorkingSet ws) {

		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_ORIGINAL_SEEN).mkdirs();
		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_ORIGINAL_NEW).mkdirs();
		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_COPY).mkdirs();
		new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_TRASH).mkdirs();

		saveAll(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_ORIGINAL_SEEN,
				ws.getBase_keep());
		saveAll(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_ORIGINAL_NEW,
				ws.getBase());
		saveAll(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_COPY, ws.getCopy());
		saveAll(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + REL_PATH_TRASH, ws.getTrash());
		
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
			writer = new PrintWriter(Main.PATH + REL_PATH_WORKINGSETS + "\\" + ws.getInfo().getHeader() + "\\" + NAME_INFO_FILE, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		writer.print(ws.getInfo().getTitle() + FLAG_SPLIT + ws.getInfo().getDate());
		writer.close();
	}

	public static void overrideWorkingSet(String baseHeader, WorkingSet newWs) {

		File baseDir = new File(Main.PATH + REL_PATH_WORKINGSETS + "\\" + baseHeader);
		delDir(baseDir);

		saveWorkingSet(newWs);
	}

	public static WorkingSetInfo[] getWorkingSets() {

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
			res[i] = parseWorkingSet(f, f.getName());
			i++;
		}
		return res;
	}

	public static WorkingSetInfo parseWorkingSet(File dir, String header) {
		Image thump = load(new File(dir.getAbsolutePath() + "\\" + NAME_THUMBNAIL));
		File infoFile = new File(dir.getAbsolutePath() + "\\" + NAME_INFO_FILE);
		String infoContent = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(infoFile));
			infoContent += reader.readLine();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] infoContentSplit = infoContent.split(FLAG_SPLIT);
		String title = infoContentSplit[0];
		long date = Long.parseLong(infoContentSplit[1]);
		return new WorkingSetInfo(title, thump, date, header);
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

	public static void delDir(File dir) {

		if (dir.listFiles() != null)
			for (File f : dir.listFiles())
				if (f.isDirectory())
					delDir(f);
				else
					f.delete();

		dir.delete();
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

	public static Image rescale(Image base, int width) {

		double s = width / base.getWidth();

		WritableImage res = new WritableImage(width, (int) (base.getHeight() * s));

		PixelReader reader = base.getPixelReader();
		PixelWriter writer = res.getPixelWriter();

		for (int x = 0; x != (int) base.getWidth(); x++)
			for (int y = 0; y != (int) base.getHeight(); y++) {
				int rgba = reader.getArgb(x, y);
				for (int dy = 0; dy < s; dy++)
					for (int dx = 0; dx < s; dx++)
						writer.setArgb((int) (x * s) + dx, (int) (y * s) + dy, rgba);
			}

		return res;

	}

}
