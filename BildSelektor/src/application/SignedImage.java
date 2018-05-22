package application;

import javafx.scene.image.Image;

public class SignedImage {

	private String name;
	private Image image;

	public SignedImage(String name, Image image) {
		super();
		this.name = name;
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public Image getImage() {
		return image;
	}
	
	

}
