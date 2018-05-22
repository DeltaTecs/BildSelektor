package application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class BufferedWorkingSet {

	private static final Random RANDOM = new Random(System.currentTimeMillis() + 4);
	private static final File FOLDER_TEMP = new File(Main.PATH + "\\temp");

	private static final int BUFFER_SIZE_DEFAULT = 3;
	private static final int PREVIEW_SIZE = 120;
	private static final boolean PREVIEW_SCALE_ON_WIDTH = false;

	private ArrayList<String> index_base = new ArrayList<String>();
	private ArrayList<String> index_trash = new ArrayList<String>();
	private ArrayList<String> index_copy = new ArrayList<String>();
	private ArrayList<String> index_base_keep = new ArrayList<String>();
	private ArrayList<String> index_cutted = new ArrayList<String>();

	private ArrayList<SignedImage> previews = new ArrayList<SignedImage>();
	private HashMap<String, SignedImage> buffer_base = new HashMap<String, SignedImage>();
	private ArrayList<String> runningLoadTasks = new ArrayList<String>();

	private boolean first = false;
	private int startSize = 0;
	private WorkingSetInfo info = null;
	private File sourceDir;
	private int prefBufferSize = BUFFER_SIZE_DEFAULT;

	public BufferedWorkingSet(boolean first, File source) {
		super();
		this.first = first;
		sourceDir = source;
	}

	public static BufferedWorkingSet genNew(Collection<File> files) {

		BufferedWorkingSet ws = new BufferedWorkingSet(true, FOLDER_TEMP);

		// temp clearen (falls noch nich clear)
		clearTemp();

		// Alle in temp laden:
		for (File f : files) {
			try {

				Image img = SwingFXUtils.toFXImage(ImageIO.read(f), null);
				String name = genName();
				FileManager.save(FOLDER_TEMP.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_NEW, img, name); // Speichern
				ws.getIndex_base().add(name); // Eintragen
				ws.getPreviews().add(new SignedImage(name, genPreview(img))); // Vorschau laden

			} catch (IOException e) {
				System.err.println("Übertragen in temp gescheitert für " + f.getName() + ":/");
				e.printStackTrace();
			}
		}
		ws.kickOffUpdateLoop();
		return ws;
	}

	public static BufferedWorkingSet loadExisting(File dir) {

		BufferedWorkingSet ws = new BufferedWorkingSet(true, dir);

		// temp clearen (falls noch nich clear)
		clearTemp();

		// Dateien werden vor Ort belassen
		// -> Zugeschnittene werden dennoch in TEMP_COPY abgelegt

		// Alle abgehen und eintragen
		if (new File(dir.getAbsolutePath() + FileManager.REL_PATH_TRASH).listFiles() != null) // Bilder vorhanden?
			for (File trashFile : new File(dir.getAbsolutePath() + FileManager.REL_PATH_TRASH).listFiles()) {

				if (!trashFile.exists() || trashFile.isDirectory()) {
					System.err.println("Error loading trash images. Found dir or non existing file");
					continue; //
				}

				ws.getPreviews().add(new SignedImage(trashFile.getName(), genPreview(trashFile)));
				ws.getIndex_trash().add(trashFile.getName());

			}
		boolean first = true;
		if (new File(dir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_SEEN).listFiles() != null) // Bilder
																										// vorhanden?
			for (File oldFile : new File(dir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_SEEN).listFiles()) {

				if (!oldFile.exists() || oldFile.isDirectory()) {
					System.err.println("Error loading seen images. Found dir or non existing file");
					continue; //
				}

				if (first) {
					first = false;
					Image preview = genPreview(oldFile);
					ws.getPreviews().add(new SignedImage(oldFile.getName(), preview));
					ws.stamp(preview);
				} else
					ws.getPreviews().add(new SignedImage(oldFile.getName(), genPreview(oldFile)));

				ws.getIndex_base_keep().add(oldFile.getName());

			}
		if (new File(dir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_NEW).listFiles() != null) // Bilder
																										// vorhanden?
			for (File newFile : new File(dir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_NEW).listFiles()) {

				if (!newFile.exists() || newFile.isDirectory()) {
					System.err.println("Error loading unseen images. Found dir or non existing file");
					continue; //
				}
				ws.getPreviews().add(new SignedImage(newFile.getName(), genPreview(newFile)));
				ws.getIndex_base().add(newFile.getName());

			}
		if (new File(dir.getAbsolutePath() + FileManager.REL_PATH_COPY).listFiles() != null) // Bilder vorhanden?
			for (File copyFile : new File(dir.getAbsolutePath() + FileManager.REL_PATH_COPY).listFiles()) {

				if (!copyFile.exists() || copyFile.isDirectory()) {
					System.err.println("Error loading copy images. Found dir or non existing file");
					continue; //
				}
				ws.getPreviews().add(new SignedImage(copyFile.getName(), genPreview(copyFile)));
				ws.getIndex_copy().add(copyFile.getName());

			}
		ws.kickOffUpdateLoop();
		return ws;
	}

	public void addCut(SignedImage cutted) {
		index_cutted.add(cutted.getName());
		FileManager.save(sourceDir.getAbsolutePath() + FileManager.REL_PATH_COPY, cutted.getImage(), cutted.getName());
	}

	public void addCopy(SignedImage copy) {
		index_copy.add(copy.getName());
	}

	public void addSeen(SignedImage seen) {
		index_base_keep.add(seen.getName());
	}

	public void addUnSeen(SignedImage unseen) {
		index_base.add(unseen.getName());
	}

	public void addTrash(SignedImage trash) {
		index_trash.add(trash.getName());
	}

	public void removeUnSeen(SignedImage unseen) {
		String s = "";
		ArrayList<String> indexBuffer = new ArrayList<String>();
		indexBuffer.addAll(index_base);
		for (String s0 : indexBuffer)
			if (s0.equals(unseen.getName())) {
				s = s0;
				break;
			}
		index_base.remove(s);
	}

	public void removeSeen(SignedImage seen) {
		String s = "";
		for (String s0 : index_base_keep)
			if (s0.equals(seen.getName())) {
				s = s0;
				break;
			}
		index_base_keep.remove(s);
	}

	public void removeCopy(SignedImage copy) {
		String s = null;
		for (String s0 : index_copy)
			if (s0.equals(copy.getName())) {
				s = s0;
				break;
			}
		if (s != null)
			index_copy.remove(s);
		else { // Bild wurde gecuttet... :(

			for (String s0 : index_cutted)
				if (s0.equals(copy.getName())) {
					s = s0;
					break;
				}
			index_cutted.remove(s);
			File cuttedImage = new File(
					sourceDir.getAbsolutePath() + FileManager.REL_PATH_COPY + "\\" + copy.getName());
			if (cuttedImage.exists())
				cuttedImage.delete();
			else
				System.err.println("[WARN] failed to delete croped copy: File not existing");
		}
	}

	public void removeTrash(SignedImage seen) {
		String s = "";
		for (String s0 : index_trash)
			if (s0.equals(seen.getName())) {
				s = s0;
				break;
			}
		index_trash.remove(s);
	}

	public SignedImage get(String name) {

		if (sourceDir == FOLDER_TEMP) {
			// Ist garantiert in "Unseen" oder ist gecutted in "copy"
			// gecutted?
			boolean croped = false;
			for (String s : index_cutted)
				if (s.equals(name)) {
					croped = true;
					break;
				}
			if (croped)
				return new SignedImage(name,
						FileManager.load(FOLDER_TEMP.getAbsolutePath() + FileManager.REL_PATH_COPY + "\\" + name));
			else
				return new SignedImage(name, FileManager
						.load(FOLDER_TEMP.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_NEW + "\\" + name));
		} else {

			// Checken wo das bild drin ist
			int loc = 0; // unseen, seen, copy, trash
			for (String s : index_base_keep)
				if (s.equals(name)) {
					loc = 1;
					break;
				}
			if (loc == 0)
				for (String s : index_copy)
					if (s.equals(name)) {
						loc = 2;
						break;
					}
			if (loc == 0)
				for (String s : index_trash)
					if (s.equals(name)) {
						loc = 3;
						break;
					}

			switch (loc) {
			case 0:
				return new SignedImage(name, FileManager
						.load(sourceDir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_NEW + "\\" + name));
			case 1:
				return new SignedImage(name, FileManager
						.load(sourceDir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_SEEN + "\\" + name));
			case 2:
				return new SignedImage(name,
						FileManager.load(sourceDir.getAbsolutePath() + FileManager.REL_PATH_COPY + "\\" + name));
			case 3:
				return new SignedImage(name,
						FileManager.load(sourceDir.getAbsolutePath() + FileManager.REL_PATH_TRASH + "\\" + name));
			default:
				break; // invalid
			}

		}

		return null;
	}

	public SignedImage getNextUnseen() {

		if (index_base.size() == 0)
			return null;

		String name = index_base.get(0); // ### DEBUG: Umkehrbar mit index=size-1 statt index=0
		// --->>> !!! Dann aber auch in bufferUpdateLoop vermerken da dort auf den
		// indizi zugegriffen wird

		if (buffer_base.containsKey(name)) {

			// Bild bereits reingeladen
			index_base.remove(name); // aus index unseen entfernen
			SignedImage res = buffer_base.get(name); // aus buffer nehmen
			buffer_base.remove(name);
			return res;

		} else {

			// Noch nicht reingeladen
			prefBufferSize++;
			System.out.println("[info] Image not loaded yet. Expanding buffer to " + prefBufferSize + " images.");
			while (!buffer_base.containsKey(name))
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			
			index_base.remove(name); // aus index unseen entfernen
			SignedImage res = buffer_base.get(name); // aus buffer nehmen
			buffer_base.remove(name);
			return res;
		}

	}

	private Thread bufferUpdateLoop = new Thread(new Runnable() {

		@Override
		public void run() {

			while (true) {

				if (buffer_base.size() < prefBufferSize) {

					int i = 0;
					ArrayList<String> indexBuffer = new ArrayList<String>();
					indexBuffer.addAll(index_base);
					for (String s0 : indexBuffer) {
						for (String s1 : runningLoadTasks)
							if (!s1.equals(s0))
								break;
						i++;
					}

					if (i > prefBufferSize)
						continue; // Noch nicht alle geschueldeten fertig. Theoretisch ist der Buffer voll.
									// Praktisch brauchen die Tasks nich ein bisschen

					// neuen Task beauftragen
					scheduleLoadTask(index_base.get(i), sourceDir + FileManager.REL_PATH_ORIGINAL_NEW);
				}

				try {
					Thread.sleep(40);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	});

	private void scheduleLoadTask(final String name, String dir) {

		runningLoadTasks.add(name);

		new Thread(new Runnable() {

			@Override
			public void run() {
				Image img = FileManager.load(new File(dir + "\\" + name));
				buffer_base.put(name, new SignedImage(name, img));
				runningLoadTasks.remove(name);
			}

		}).start();

	}

	private static Image genPreview(File f) {
		Image source = FileManager.load(f);
		return FileManager.rescale(source, PREVIEW_SIZE, PREVIEW_SCALE_ON_WIDTH);
	}

	private static Image genPreview(Image i) {
		return FileManager.rescale(i, PREVIEW_SIZE, PREVIEW_SCALE_ON_WIDTH);
	}

	private ArrayList<String> getIndex_base() {
		return index_base;
	}

	private ArrayList<String> getIndex_trash() {
		return index_trash;
	}

	private ArrayList<String> getIndex_copy() {
		return index_copy;
	}

	private ArrayList<String> getIndex_base_keep() {
		return index_base_keep;
	}

	public void stamp(Image thumb) {
		info = WorkingSetInfo.gen(thumb);
	}

	public boolean isFirst() {
		return first;
	}

	public int getStartSize() {
		return startSize;
	}

	public WorkingSetInfo getInfo() {
		return info;
	}

	public ArrayList<SignedImage> getPreviews() {
		return previews;
	}

	public SignedImage getPreview(String name) {
		for (SignedImage i : previews)
			if (i.getName().equals(name))
				return i;
		return null;
	}

	private static String genName() {
		return RANDOM.nextInt(Integer.MAX_VALUE) + (int) (System.currentTimeMillis() % 100000) + ".png";
	}

	public void kickOffUpdateLoop() {
		bufferUpdateLoop.start();
	}

	private static void clearTemp() {
		FileManager.delContent(new File(FOLDER_TEMP.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_NEW));
		FileManager.delContent(new File(FOLDER_TEMP.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_SEEN));
		FileManager.delContent(new File(FOLDER_TEMP.getAbsolutePath() + FileManager.REL_PATH_COPY));
		FileManager.delContent(new File(FOLDER_TEMP.getAbsolutePath() + FileManager.REL_PATH_TRASH));
	}

}
