package application;

import java.util.Calendar;
import java.util.Date;

import javafx.scene.image.Image;

public class WorkingSetInfo {

	public static final String[] MONTHS = new String[] { "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli",
			"August", "September", "Oktober", "November", "Dezember" };

	private String title;
	private String header;
	private Image thumbnail;
	private long date;
	private int startSize;
	private int unseen;

	public WorkingSetInfo(String title, Image image, long date, String header, int startSize, int unseen) {
		super();
		this.title = title;
		this.thumbnail = image;
		this.date = date;
		this.header = header;
		this.startSize = startSize;
		this.unseen = unseen;
	}

	public static WorkingSetInfo gen(Image thump, int startSize, int unseen) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int year = cal.get(Calendar.YEAR);
		int month_index = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		String month = MONTHS[month_index];

		String t = day + ". " + month + " " + year;
		String h = year + "---" + month_index + "_" + month + "---" + day + "___"
				+ (System.currentTimeMillis() % 1000000);
		return new WorkingSetInfo(t, FileManager.rescale(thump, 120, true), System.currentTimeMillis(), h, startSize,
				unseen);
	}

	public String getTitle() {
		return title;
	}

	public String getHeader() {
		return header;
	}

	public Image getThumbnail() {
		return thumbnail;
	}

	public long getDate() {
		return date;
	}
	
	public int getSecondDifferenceToNow() {
		return (int) ((System.currentTimeMillis() - date) / (long) (1000));
	}
	
	public int getMinuteDifferenceToNow() {
		return (int) ((System.currentTimeMillis() - date) / (long) (1000 * 60));
	}
	
	public int getHourDifferenceToNow() {
		return (int) ((System.currentTimeMillis() - date) / (long) (1000 * 60 * 60));
	}

	public int getDayDifferenceToNow() {
		return (int) ((System.currentTimeMillis() - date) / (long) (1000 * 60 * 60 * 24));
	}

	public int getMonthDifferenceToNow() {
		return (int) ((System.currentTimeMillis() - date) / (long) (1000 * 60 * 60 * 24 * 30));
	}

	public int getYearDifferenceToNow() {
		return (int) ((System.currentTimeMillis() - date) / (long) (1000 * 60 * 60 * 24 * 30 * 370));
	}

	public int getStartSize() {
		return startSize;
	}

	public int getUnseen() {
		return unseen;
	}

}
