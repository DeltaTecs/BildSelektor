package application;

public class FinalNotificationTask {
	
	private String title;
	private String message;
	private boolean exitOnQuit;
	
	public FinalNotificationTask(String title, String message, boolean exitOnQuit) {
		super();
		this.title = title;
		this.message = message;
		this.exitOnQuit = exitOnQuit;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public boolean isExitOnQuit() {
		return exitOnQuit;
	}
	
	

}
