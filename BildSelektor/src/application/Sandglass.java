package application;

public class Sandglass {
	
	private int max;

	public Sandglass(int max) {
		super();
		this.max = max;
	}
	
	public void substract() {
		max--;
	}
	
	public void substract(int amount) {
		max -= amount;
	}
	
	public boolean isEmpty() {
		return max <= 0;
	}
	

}
