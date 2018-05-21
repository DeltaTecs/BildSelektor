package application;

import javafx.scene.canvas.Canvas;

public class ResizeableCanvas extends Canvas {
	
	Drawable drawable;

	public ResizeableCanvas() {
		super();
		// Redraw canvas when size changes.
		widthProperty().addListener(evt -> draw());
		heightProperty().addListener(evt -> draw());
	}

	public void draw() {
		drawable.draw(getWidth(), getHeight(), getGraphicsContext2D());
	}
	
	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public double prefWidth(double height) {
		return getWidth();
	}

	@Override
	public double prefHeight(double width) {
		return getHeight();
	}

	public Drawable getDrawable() {
		return drawable;
	}

	public void setDrawable(Drawable drawable) {
		this.drawable = drawable;
	}
	

}
