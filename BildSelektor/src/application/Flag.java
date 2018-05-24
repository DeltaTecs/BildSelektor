package application;

public class Flag {
	
	private boolean active;

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public static Flag active() {
		Flag f = new Flag();
		f.setActive(true);
		return f;
	}
	
	public static Flag inactive() {
		Flag f = new Flag();
		f.setActive(false);
		return f;
	}
	
	
	

}
