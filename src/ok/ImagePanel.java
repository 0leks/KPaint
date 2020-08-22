package ok;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private BufferedImage original;
	private BufferedImage current;
	private BufferedImage selectionOverlay;
	private int xOffset;
	private int yOffset;
	private int xStart;
	private int yStart;
	private Point mousePosition = new Point(0, 0);
	private boolean movingImage;
	private int mouseButtonDown;

	private boolean[][] selected;

	private double pixelSize = 1;
	private int brushSize;
	
	private boolean mouseDown;
	private boolean setToWhileMouseDown;
	
	private Color color1;
	private Color color2;

	public class Pixel {
		private int x;
		private int y;

		public Pixel(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof Pixel) {
				Pixel pixel = (Pixel)other;
				return this.x == pixel.x && this.y == pixel.y;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (x + "," + y).hashCode();
		}
		
	}

	public enum Mode {
		COLOR_SELECT("Color Select"), FILL_SELECT("Fill Select"), SINGLE_SELECT("Single Select"), NONE("None");
		private String name;
		Mode(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}

	private Mode currentMode;

	public ImagePanel() {
		color1 = Color.black;
		color2 = Color.white;
		BufferedImage defaultImage = new BufferedImage(300, 300, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = defaultImage.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, defaultImage.getWidth(), defaultImage.getHeight());
		g.dispose();
		this.setImage(defaultImage);
		this.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				Point pixelPosition = getPixelPosition();
				pixelPosition.x = Math.max(0, Math.min(pixelPosition.x, current.getWidth()));
				pixelPosition.y = Math.max(0, Math.min(pixelPosition.y, current.getHeight()));
				double oldPixelSize = pixelSize;
				if (e.getWheelRotation() > 0) {
					pixelSize = pixelSize * 0.9;
					if(pixelSize < 0.01) { 
						pixelSize = 0.01;
					}
				} else {
					pixelSize = pixelSize*1.1 + 0.1;
				}
				double deltaPixelSize = pixelSize - oldPixelSize;
				xOffset = (int)(xOffset - deltaPixelSize * pixelPosition.x);
				yOffset = (int)(yOffset - deltaPixelSize * pixelPosition.y);
				repaint();
			}
		});
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					int mouseX = (int) ((mousePosition.x - xOffset) / pixelSize);
					int mouseY = (int) ((mousePosition.y - yOffset) / pixelSize);
					if (mouseX >= 0 && mouseX < current.getWidth() && mouseY >= 0 && mouseY < current.getHeight()) {
						Color color = new Color(current.getRGB(mouseX, mouseY));
						String s = color.getRed() + ", " + color.getGreen() + ", " + color.getBlue();
						File file = new File("pixelColors.txt");
						try {
							PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
							pw.println(s);
							pw.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mouseButtonDown = e.getButton();
				if(mouseButtonDown == MouseEvent.BUTTON2) {
					movingImage = true;
					xStart = e.getX();
					yStart = e.getY();
				}
				else {
					selectAction(getPixelPosition(), e.isShiftDown());
				}
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getButton() == mouseButtonDown) {
					mouseButtonDown = 0;
				}
				mouseDown = false;
				if (e.getButton() == MouseEvent.BUTTON2) {
					movingImage = false;
				}
			}
		});
		this.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				mousePosition = e.getPoint();
				if (movingImage) {
					int deltaX = e.getX() - xStart;
					int deltaY = e.getY() - yStart;
					xOffset += deltaX;
					yOffset += deltaY;
					xStart = e.getX();
					yStart = e.getY();
					repaint();
				}
				else {
					selectAction(getPixelPosition(), e.isShiftDown());
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				mousePosition = e.getPoint();
				repaint();
			}
		});
	}
	
	public void selectAction(Point pixel, boolean shiftDown) {
		if (pixel.x >= 0 && pixel.x < current.getWidth() && pixel.y >= 0 && pixel.y < current.getHeight()) {
			Color setTo = color1;
			if(mouseButtonDown == MouseEvent.BUTTON3) {
				setTo = color2;
			}
			
			if (!shiftDown) {
				deselectAll();
			}
			Point lowerBound = new Point(pixel.x - brushSize, pixel.y - brushSize);
			lowerBound.x = Math.max(lowerBound.x, 0);
			lowerBound.y = Math.max(lowerBound.y, 0);
			
			Point upperBound = new Point(pixel.x + brushSize, pixel.y + brushSize);
			upperBound.x = Math.min(upperBound.x, current.getWidth()-1);
			upperBound.y = Math.min(upperBound.y, current.getHeight()-1);

//			if (currentMode == Mode.COLOR_SELECT) {
//				colorSelect(lowerBound, upperBound, setTo);
//			} 
//			else 
			if (currentMode == Mode.FILL_SELECT) {
				fill(lowerBound, upperBound, setTo);
			}
			else if (currentMode == Mode.SINGLE_SELECT) {
				for(int i = lowerBound.x; i <= upperBound.x; i++) {
					for(int j = lowerBound.y; j <= upperBound.y; j++) {
						current.setRGB(i, j, setTo.getRGB());
					}
				}
				repaint();
			}
		}
	}
	
	public Point getPixelPosition() {
		Point pixel = new Point();
		pixel.x = (int) ((mousePosition.x - xOffset)/pixelSize);
		pixel.y = (int) ((mousePosition.y - yOffset)/pixelSize);
		return pixel;
	}

	public BufferedImage getCurrentImage() {
		return current;
	}

	public void setImage(BufferedImage image) {
		original = copyImage(image);
		current = copyImage(image);
		selected = new boolean[original.getWidth()][original.getHeight()];
		selectionOverlay = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		pixelSize = 1;
		xOffset = getWidth()/2 - current.getWidth()/2;
		yOffset = getHeight()/2 - current.getHeight()/2;
		repaint();
	}

	public void setTransparent() {
		for (int x = 0; x < selected.length; x++) {
			for (int y = 0; y < selected[x].length; y++) {
				if (selected[x][y]) {
					current.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());
				}
			}
		}
		deselectAll();
		repaint();
	}

	public void deselectAll() {
		selected = new boolean[selected.length][selected[0].length];
		selectionOverlay = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
	}
	
	private void setSelected(int x, int y, boolean active) {
		selected[x][y] = active;
		if(selected[x][y]) {
			selectionOverlay.setRGB(x, y, Color.red.getRGB());
		}
		else {
			selectionOverlay.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());
		}
	}
	
	private LinkedList<Pixel> getNeighbors(Pixel pixel) {
		LinkedList<Pixel> neighbors = new LinkedList<>();
		neighbors.add(new Pixel(pixel.x - 1, pixel.y));
		neighbors.add(new Pixel(pixel.x + 1, pixel.y));
		neighbors.add(new Pixel(pixel.x, pixel.y - 1));
		neighbors.add(new Pixel(pixel.x, pixel.y + 1));
		return neighbors;
	}
	
	private void fill(Point lowerBound, Point upperBound, Color setTo) {
		HashSet<Integer> colors = new HashSet<>();
		HashSet<Pixel> visited = new HashSet<>();
		LinkedList<Pixel> search = new LinkedList<Pixel>();
		for(int i = lowerBound.x; i <= upperBound.x; i++) {
			for(int j = lowerBound.y; j <= upperBound.y; j++) {
				Pixel start = new Pixel(i, j);
				search.add(start);
				colors.add(current.getRGB(i, j));
				visited.add(start);
			}
		}
		while (!search.isEmpty()) {
			Pixel pixel = search.removeFirst();
			current.setRGB(pixel.x, pixel.y, setTo.getRGB());
//			setSelected(pixel.x, pixel.y, setTo);
			for(Pixel neighbor : getNeighbors(pixel)) {
				if(!visited.contains(neighbor) && neighbor.x >= 0 && neighbor.y >= 0 && neighbor.x < selected.length && neighbor.y < selected[0].length) {
					visited.add(neighbor);
					if (colors.contains(current.getRGB(neighbor.x, neighbor.y))) {
						search.add(neighbor);
					}
				}
			}
		}
		repaint();
	}

	private void colorSelect(Point lowerBound, Point upperBound, boolean setTo) {
		HashSet<Integer> colors = new HashSet<>();
		for(int i = lowerBound.x; i <= upperBound.x; i++) {
			for(int j = lowerBound.y; j <= upperBound.y; j++) {
				colors.add(current.getRGB(i, j));
			}
		}
		for (int i = 0; i < current.getWidth(); i++) {
			for (int j = 0; j < current.getHeight(); j++) {
				int rgb = current.getRGB(i, j);
				if(colors.contains(rgb)) {
					setSelected(i, j, setTo);
				}
			}
		}
		repaint();
	}

	public void setMode(int modeIndex) {
		currentMode = Mode.values()[modeIndex];
	}
	public void setBrushSize(int brushSize) {
		this.brushSize = brushSize;
		repaint();
	}
	
	public void setColor1(Color color1) {
		this.color1 = color1;
	}
	public void setColor2(Color color2) {
		this.color2 = color2;
	}
	public Color getColor1() {
		return color1;
	}
	public Color getColor2() {
		return color2;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Point pixelPosition = getPixelPosition();
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());
		int stripeWidth = 10;
		for (int i = 0; i < getWidth(); i += stripeWidth) {
			g.setColor(new Color((int) (i * 255 / getWidth()),
					(int) (i * 255 / getWidth() ),
					(int) (i * 255 / getWidth())));
			g.fillRect(i, 0, stripeWidth, getHeight());
		}
		g.drawImage(current, xOffset, yOffset, (int)(current.getWidth()*pixelSize), (int)(current.getHeight()*pixelSize), null);
		g.drawImage(selectionOverlay, xOffset, yOffset, (int)(current.getWidth()*pixelSize), (int)(current.getHeight()*pixelSize), null);
		
		g.setColor(Color.black);
		int boxsize = (int)(pixelSize * (brushSize * 2 + 1));
		g.drawRect((int)(xOffset + (pixelPosition.x - brushSize) * pixelSize), (int)(yOffset + (pixelPosition.y - brushSize) * pixelSize), boxsize, boxsize);
		
		if(PaintDriver.DEBUG) {
			g.setColor(Color.green);
			g.drawString(brushSize + "", 10, getHeight() - 90);
			g.drawString(pixelSize + "", 10, getHeight() - 70);
			g.drawString(xOffset + "," + yOffset, 10, getHeight() - 50);
			g.drawString(pixelPosition.x + "," + pixelPosition.y, 10, getHeight() - 30);
			g.drawString(mousePosition.x + "," + mousePosition.y, 10, getHeight() - 10);
		}
		
		g.setColor(color1);
		g.fillRect(5, 5, 20, 20);
		g.setColor(color2);
		g.fillRect(30, 5, 20, 20);
	}

	public static BufferedImage copyImage(BufferedImage image) {
		BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = copy.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return copy;
	}
}
