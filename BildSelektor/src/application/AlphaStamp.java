package application;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JComponent;

/**
 * Zeigt den Stempel an v1.0
 * 
 * @author Martin
 *
 */
public class AlphaStamp extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final byte SIZE_X = 15;
	private static final byte SIZE_Y = 22;
	private static final Vector<Double> DIR0 = new Vector<Double>();
	private static final Vector<Double> DIR1 = new Vector<Double>();
	private static final Vector<Double> DIR2 = new Vector<Double>();
	private static final Vector<Double> DIR3 = new Vector<Double>();
	private int ticksPerLine = 200;
	private int ticksFade = 400;
	private static final int SPACE_TO_BORDER = 50;
	private static final int COLOR_BASE_R = 193;
	private static final int COLOR_BASE_G = 0;
	private static final int COLOR_BASE_B = 12;
	private static final int DELAY_BETWEEN_FRAMES = 2;
	private int duration = (ticksPerLine * 4 + ticksFade) * DELAY_BETWEEN_FRAMES;
	private static ArrayList<Point> corners = new ArrayList<Point>();
	private static BasicStroke stroke;
	private double color_r = COLOR_BASE_R;
	private double color_g = COLOR_BASE_G;
	private double color_b = COLOR_BASE_B;
	private double colorSpeed_r = (255 - COLOR_BASE_R) / (double) ticksPerLine;
	private double colorSpeed_g = (255 - COLOR_BASE_G) / (double) ticksPerLine;
	private double colorSpeed_b = (255 - COLOR_BASE_B) / (double) ticksPerLine;
	private double posX;
	private double posY;
	private Polygon polygon = new Polygon();
	private float scaling;
	private boolean done = false;
	private byte step = 0;
	private int time = ticksPerLine;

	/**
	 * Erledigt Layout und Visibility es müssen noch init() und execute() aufgerufen
	 * werden
	 */
	public AlphaStamp() {
		setLayout(null);
		setVisible(true);
	}

	/**
	 * Erledigt Layout und Visibility es müssen noch init() und execute() aufgerufen
	 * werden Zusätzlich wird die Geschwindigkeit multipliziert mit speedFac
	 */
	public AlphaStamp(float speedFac) {
		setLayout(null);
		setVisible(true);
		ticksPerLine = (int) ((1 / speedFac) * 200);
		ticksFade = (int) ((1 / speedFac) * 200);
		colorSpeed_r = (255 - COLOR_BASE_R) / (double) ticksPerLine;
		colorSpeed_g = (255 - COLOR_BASE_G) / (double) ticksPerLine;
		colorSpeed_b = (255 - COLOR_BASE_B) / (double) ticksPerLine;
		duration = (ticksPerLine * 4 + ticksFade) * DELAY_BETWEEN_FRAMES;
	}

	@Override
	public void paintComponent(Graphics g) {

		if (step == 5) {
			done = true;
			return;
		}

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		g2d.setStroke(stroke);
		g2d.setColor(new Color((int) color_r, (int) color_g, (int) color_b));
		g2d.drawPolygon(polygon);

	}

	private void update() {

		// init / update
		if (step == 5) {
			done = true;
			return;
		}

			switch (step) {
		case 0:
			polygon.reset();
			for (Point p : corners) {
				polygon.addPoint(p.x, p.y);
			}
			posX += (scaling * DIR0.get(0)) / ticksPerLine;
			posY += (scaling * DIR0.get(1)) / ticksPerLine;
			polygon.addPoint((int) posX, (int) posY);
			for (int i = corners.size() - 1; i != -1; i--) {
				polygon.addPoint(corners.get(i).x, corners.get(i).y);
			}
			break;
		case 1:
			polygon.reset();
			for (Point p : corners) {
				polygon.addPoint(p.x, p.y);
			}
			posX += (scaling * DIR1.get(0)) / ticksPerLine;
			posY += (scaling * DIR1.get(1)) / ticksPerLine;
			polygon.addPoint((int) posX, (int) posY);
			for (int i = corners.size() - 1; i != -1; i--) {
				polygon.addPoint(corners.get(i).x, corners.get(i).y);
			}
			break;
		case 2:
			polygon.reset();
			for (Point p : corners) {
				polygon.addPoint(p.x, p.y);
			}
			posX += (scaling * DIR2.get(0)) / ticksPerLine;
			posY += (scaling * DIR2.get(1)) / ticksPerLine;
			polygon.addPoint((int) posX, (int) posY);
			for (int i = corners.size() - 1; i != -1; i--) {
				polygon.addPoint(corners.get(i).x, corners.get(i).y);
			}
			break;
		case 3:
			polygon.reset();
			for (Point p : corners) {
				polygon.addPoint(p.x, p.y);
			}
			posX += (scaling * DIR3.get(0)) / ticksPerLine;
			posY += (scaling * DIR3.get(1)) / ticksPerLine;
			polygon.addPoint((int) posX, (int) posY);
			for (int i = corners.size() - 1; i != -1; i--) {
				polygon.addPoint(corners.get(i).x, corners.get(i).y);
			}
			break;
		case 4:
			color_r += colorSpeed_r;
			color_g += colorSpeed_g;
			color_b += colorSpeed_b;
			break;
		default:
			// won't happen ;)
			break;
		}

		time--;
		if (time == 0) {
			if (time < 4) {
				time = ticksPerLine;
			} else {
				time = ticksFade;
			}
			step++;
			if (step < 5) {
				corners.add(new Point((int) posX, (int) posY));
			}

		}
	}

	/**
	 * Startet die Animation. Übernimmt das Färben. Eigener Thread.
	 */
	public void execute() {

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!done) {
					update();
					repaint();
					try {
						Thread.sleep(DELAY_BETWEEN_FRAMES);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}, "Stamp Update");
		t.start();

	}

	/**
	 * berechnet aus dem gegebene Sitenverhältniss eine Skalierung für den
	 * grafischen Effetk. Ebenso die benötigten Vektoren
	 */
	public void init() {
		if (getHeight() <= 2 * SPACE_TO_BORDER || getHeight() <= 2 * SPACE_TO_BORDER) {
			System.out.println("[WARN] Stamp is too smal!");
			step = 5;
			return;
		}

		if ((getWidth() / (float) getHeight()) > (SIZE_X / (float) SIZE_Y)) {
			scaling = (getHeight() - (2 * SPACE_TO_BORDER)) / (float) SIZE_Y;
			posX = (getWidth() / 2.0) - ((SIZE_X * scaling) / 2.0) - (0.5 * scaling); // -
																						// (kp
																						// warum)
			posY = (getHeight() - SPACE_TO_BORDER);
		} else {
			scaling = (getWidth() - (2 * SPACE_TO_BORDER)) / (float) SIZE_X;
			posX = SPACE_TO_BORDER - (0.5 * scaling); // - (kp warum)
			posY = (getHeight() / 2.0) + ((SIZE_Y * scaling) / 2.0);
		}

		DIR0.setSize(2);
		DIR0.set(0, (12.0));
		DIR0.set(1, (-22.0));
		DIR1.setSize(2);
		DIR1.set(0, (3.0));
		DIR1.set(1, (15.0));
		DIR2.setSize(2);
		DIR2.set(0, (-13.0));
		DIR2.set(1, (-8.0));
		DIR3.setSize(2);
		DIR3.set(0, (14.0));
		DIR3.set(1, (15.0));

		stroke = new BasicStroke((float) (0.4 * scaling));

		corners.add(new Point((int) posX, (int) posY));

	}

	public int getDuration() {
		return duration;
	}

	public boolean isDone() {
		return done;
	}

}
