package application;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import javafx.scene.image.Image;

public class BufferedWorkingSet {

	private static final Random RANDOM = new Random(System.currentTimeMillis() + 4);
	public static final File FOLDER_TEMP = new File(Main.PATH + "\\temp");

	private static final int BUFFER_SIZE_DEFAULT = 5;
	private static final int MAX_IMAGES_IN_RAM = 15;
	private static final int PREVIEW_SIZE = 200;
	private static final boolean PREVIEW_SCALE_ON_WIDTH = false;

	private ArrayList<String> index_base = new ArrayList<String>();
	private ArrayList<String> index_trash = new ArrayList<String>();
	private ArrayList<String> index_copy = new ArrayList<String>();
	private ArrayList<String> index_base_keep = new ArrayList<String>();

	private ArrayList<SignedImage> previews = new ArrayList<SignedImage>();
	private HashMap<String, SignedImage> buffer_base = new HashMap<String, SignedImage>();
	private ArrayList<String> runningLoadTasks = new ArrayList<String>();

	private boolean first = false;
	private WorkingSetInfo info = null;
	private File sourceDir;
	private int prefBufferSize = BUFFER_SIZE_DEFAULT;
	private int runningLoadTasksAmount = 0;
	private int currentBufferSize = 0;
	private int runningCopyTasks = 0;
	private int imagesInRAM = 0;
	private SignedImage addImage = null;

	public BufferedWorkingSet(boolean first, File source) {
		super();
		this.first = first;
		sourceDir = source;
	}

	public static BufferedWorkingSet genNew(Collection<File> files) throws InterruptedException {

		BufferedWorkingSet ws = new BufferedWorkingSet(true, FOLDER_TEMP);

		// temp clearen (falls noch nich clear)
		clearTemp();

		Sandglass sg = new Sandglass(files.size());
		ArrayList<String> orderedNameList = new ArrayList<String>();
		// Alle in temp laden:
		for (File f : files) {
			try {
				String name = genName();
				orderedNameList.add(name);

				while (ws.getImagesInRAM() >= MAX_IMAGES_IN_RAM)
					Thread.sleep(20);
				ws.setImageInRAM(ws.getImagesInRAM() + 1);
				scheduleNewInitLoadingTask(f, ws, sg, name);
			} catch (Exception e) {
				System.err.println("Übertragen in temp gescheitert für " + f.getName() + ":/");
				e.printStackTrace();
			}
		}

		while (!sg.isEmpty())
			Thread.sleep(50);

		System.gc();

		// In die richtige Reihenfolge bringen
		for (String s : orderedNameList)
			ws.getIndex_base().add(0, s);

		ws.setInfo(WorkingSetInfo.gen(ws.getPreviews().get(0).getImage(), files.size(), files.size()));
		ws.kickOffUpdateLoop();
		files = null;
		return ws;
	}

	public static BufferedWorkingSet loadExisting(File dir) {

		BufferedWorkingSet ws = new BufferedWorkingSet(true, dir);

		// temp clearen (falls noch nich clear)
		clearTemp();

		// Dateien werden vor Ort belassen

		// Alle abgehen und eintragen
		if (new File(dir.getAbsolutePath() + FileManager.REL_PATH_TRASH).listFiles() != null) // Bilder vorhanden?
			for (File trashFile : new File(dir.getAbsolutePath() + FileManager.REL_PATH_TRASH).listFiles()) {

				if (!trashFile.exists() || trashFile.isDirectory()) {
					System.err.println("Error loading trash images. Found dir or non existing file");
					continue; //
				}
				if (trashFile.getName().equals(FileManager.NAME_INFO_ORDER))
					continue;

				ws.getPreviews().add(new SignedImage(trashFile.getName(), genPreview(trashFile)));
				Main.loadProgress += Main.progressPerImage;

			}
		if (new File(dir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_SEEN).listFiles() != null) // Bilder
																										// vorhanden?
			for (File oldFile : new File(dir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_SEEN).listFiles()) {

				if (!oldFile.exists() || oldFile.isDirectory()) {
					System.err.println("Error loading seen images. Found dir or non existing file");
					continue; //
				}
				if (oldFile.getName().equals(FileManager.NAME_INFO_ORDER))
					continue;

				ws.getPreviews().add(new SignedImage(oldFile.getName(), genPreview(oldFile)));
				Main.loadProgress += Main.progressPerImage;

			}
		if (new File(dir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_NEW).listFiles() != null) // Bilder
																										// vorhanden?
			for (File newFile : new File(dir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_NEW).listFiles()) {

				if (!newFile.exists() || newFile.isDirectory()) {
					System.err.println("Error loading unseen images. Found dir or non existing file");
					continue; //
				}
				if (newFile.getName().equals(FileManager.NAME_INFO_ORDER))
					continue;

				ws.getPreviews().add(new SignedImage(newFile.getName(), genPreview(newFile)));
				Main.loadProgress += Main.progressPerImage;

			}
		if (new File(dir.getAbsolutePath() + FileManager.REL_PATH_COPY).listFiles() != null) // Bilder vorhanden?
			for (File copyFile : new File(dir.getAbsolutePath() + FileManager.REL_PATH_COPY).listFiles()) {

				if (!copyFile.exists() || copyFile.isDirectory()) {
					System.err.println("Error loading copy images. Found dir or non existing file");
					continue; //
				}
				if (copyFile.getName().equals(FileManager.NAME_INFO_ORDER))
					continue;

				ws.getPreviews().add(new SignedImage(copyFile.getName(), genPreview(copyFile)));
				Main.loadProgress += Main.progressPerImage;
			}

		ws.getIndex_base().addAll(FileManager.readOrder(dir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_NEW));
		ws.getIndex_base_keep()
				.addAll(FileManager.readOrder(dir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_SEEN));
		ws.getIndex_trash().addAll(FileManager.readOrder(dir.getAbsolutePath() + FileManager.REL_PATH_TRASH));
		ws.getIndex_copy().addAll(FileManager.readOrder(dir.getAbsolutePath() + FileManager.REL_PATH_COPY));

		ws.setInfo(FileManager.parseWorkingSetInfo(dir));
		Main.startFiles = ws.getInfo().getInitial();
		ws.kickOffUpdateLoop();
		return ws;
	}

	private static void scheduleNewInitLoadingTask(File f, BufferedWorkingSet ws, Sandglass sg, String name) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Image img = FileManager.load(f);
					System.out.println("[info] loaded into RAM:\t " + name);
					Main.loadProgress += Main.progressPerImage * 0.2;
					FileManager.saveAndUpdateProgress(FOLDER_TEMP.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_NEW,
							img, name); // Speichern
					System.out.println("[info] saved in temp:\t " + name);
					ws.getPreviews().add(new SignedImage(name, genPreview(img))); // Vorschau laden
					System.out.println("[info] preview gen.:\t " + name);
					sg.substract();
					img = null;
					System.gc();
					ws.setImageInRAM(ws.getImagesInRAM() - 1);
					Main.loadProgress += Main.progressPerImage * 0.2;

				} catch (Exception e) {
					System.err.println("Error loading file " + f.getName());
					e.printStackTrace();
				}

			}
		}, "Initial-Loading-images").start();
	}

	public void addCopy(Image toCopy, Runnable finishTask) {
		runningCopyTasks++;
		new Thread(new Runnable() {

			@Override
			public void run() {
				String name = genName();
				FileManager.save(sourceDir.getAbsolutePath() + FileManager.REL_PATH_COPY, toCopy, name);
				// neues Preview anfertigen
				previews.add(new SignedImage(name, genPreview(toCopy)));
				index_copy.add(name);
				runningCopyTasks--;
				if (finishTask != null)
					finishTask.run();
			}

		}, "copy").start();
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
		index_copy.remove(s);
		File copyImage = new File(sourceDir.getAbsolutePath() + FileManager.REL_PATH_COPY + "\\" + copy.getName());
		if (copyImage.exists())
			copyImage.delete();
		else
			System.err.println("[WARN] failed to delete croped copy: File not existing");

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
			// Ist garantiert in "Unseen" oder ist geklont in "copy"
			// geklont?
			boolean copyed = false;
			for (String s : index_copy)
				if (s.equals(name)) {
					copyed = true;
					break;
				}
			if (copyed)
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
			index_base.remove(0); // aus index unseen entfernen
			SignedImage res = buffer_base.get(name); // aus buffer nehmen
			buffer_base.remove(name);
			currentBufferSize--;
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

			index_base.remove(0); // aus index unseen entfernen
			SignedImage res = buffer_base.get(name); // aus buffer nehmen
			buffer_base.remove(name);
			currentBufferSize--;
			return res;
		}

	}

	private Thread bufferUpdateLoop = new Thread(new Runnable() {

		@Override
		public void run() {

			while (true) {

				if ((currentBufferSize + runningLoadTasksAmount) < prefBufferSize) {
					// Es werden nicht genügend nachgeladen

					// Nächstes finden, dass geladen werden soll
					for (int i = 0; i != index_base.size(); i++) {
						if (runningLoadTasks.contains(index_base.get(i)))
							continue; // wird schon geladen

						// --> Bild gefunden das drin ist, gebraucht wird, aber noch nicht geladen wird
						scheduleLoadTask(index_base.get(i),
								sourceDir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_NEW);
						break; // nicht weiter suchen
					}

				}

				try {
					Thread.sleep(40);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}, "Buffer-Update-Loop");

	private void scheduleLoadTask(final String name, String dir) {
		System.out.println("[info] scheduled loading " + name);
		runningLoadTasksAmount++;
		runningLoadTasks.add(name);

		new Thread(new Runnable() {

			@Override
			public void run() {
				Image img = FileManager.load(new File(dir + "\\" + name));
				buffer_base.put(name, new SignedImage(name, img));
				currentBufferSize++;
				runningLoadTasksAmount--;
				runningLoadTasks.remove(name);
				System.out.println("[info] finished loading " + name);
			}

		}, "ImageLoader-" + name).start();

	}

	private static Image genPreview(File f) {
		Image source = FileManager.load(f);
		return FileManager.rescale(source, PREVIEW_SIZE, PREVIEW_SCALE_ON_WIDTH);
	}

	private static Image genPreview(Image i) {
		return FileManager.rescale(i, PREVIEW_SIZE, PREVIEW_SCALE_ON_WIDTH);
	}

	public ArrayList<String> getIndex_base() {
		return index_base;
	}

	public ArrayList<String> getIndex_trash() {
		return index_trash;
	}

	public ArrayList<String> getIndex_copy() {
		return index_copy;
	}

	public ArrayList<String> getIndex_base_keep() {
		return index_base_keep;
	}

	public boolean isFirst() {
		return first;
	}

	private void setInfo(WorkingSetInfo info) {
		this.info = info;
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

	public static String genName() {
		return RANDOM.nextInt(Integer.MAX_VALUE) + (int) (System.currentTimeMillis() % 100000) + ".png";
	}

	public void kickOffUpdateLoop() {

		// Buffer füllen
		for (int i = 0; i != prefBufferSize; i++) {

			if (i == index_base.size())
				break; // limit

			String name = index_base.get(i);
			Image img = FileManager
					.load(new File(sourceDir.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_NEW + "\\" + name));
			buffer_base.put(name, new SignedImage(name, img));
			currentBufferSize++;
			System.out.println("[info] pre-buffered " + name);
			Main.loadProgress += 0.06 / prefBufferSize;
		}

		bufferUpdateLoop.start();
	}

	private static void clearTemp() {
		FileManager.delContent(new File(FOLDER_TEMP.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_NEW));
		FileManager.delContent(new File(FOLDER_TEMP.getAbsolutePath() + FileManager.REL_PATH_ORIGINAL_SEEN));
		FileManager.delContent(new File(FOLDER_TEMP.getAbsolutePath() + FileManager.REL_PATH_COPY));
		FileManager.delContent(new File(FOLDER_TEMP.getAbsolutePath() + FileManager.REL_PATH_TRASH));
	}

	public int getAmountSeen() {
		return index_base_keep.size();
	}

	public int getAmountUnseen() {
		return index_base.size();
	}

	public int getAmountTrash() {
		return index_trash.size();
	}

	public int getAmountCopys() {
		return index_copy.size();
	}

	public File getSourceDir() {
		return sourceDir;
	}

	public String getOriginalKey(String mirror) {
		for (String s : index_base)
			if (s.equals(mirror))
				return s;
		for (String s : index_base_keep)
			if (s.equals(mirror))
				return s;
		for (String s : index_trash)
			if (s.equals(mirror))
				return s;
		for (String s : index_copy)
			if (s.equals(mirror))
				return s;
		return mirror;
	}

	public int getRunningCopyTasks() {
		return runningCopyTasks;
	}

	private int getImagesInRAM() {
		return imagesInRAM;
	}

	private void setImageInRAM(int i) {
		imagesInRAM = i;
	}

	public SignedImage getAddImage() {
		return addImage;
	}

	public void setAddImage(SignedImage addImage) {
		this.addImage = addImage;
	}

}
