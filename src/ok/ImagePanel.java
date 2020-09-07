package ok;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.swing.*;

public class ImagePanel extends JPanel {

	public enum Mode {
		MOVE("Move", "resources/move.png"), SELECT("Select", "resources/select.png"), BRUSH("Brush", "resources/brush.png"), FILL("Fill", "resources/fill.png"), COLOR_SELECT("Matching Color", "resources/color.png");
		private String name;
		private ImageIcon image;
		Mode(String name, String imageLocation) {
			this.name = name;
			this.image = Utils.resizeImageIcon(Utils.loadImageIconResource(imageLocation), 32, 32);
		}
		public ImageIcon getImageIcon() {
			return image;
		}
		@Override
		public String toString() {
			return name;
		}
	}
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
	
	private History history = new History();
	private volatile BufferedImage selectedImage;
	private int xOffset;
	private int yOffset;
	private int xStart;
	private int yStart;
	private Point mousePosition = new Point(0, 0);
	private Point previousMousePosition = new Point(0, 0);
	private boolean movingImage;
	private boolean movingSelection;
	private int mouseButtonDown;

	private double pixelSize = 1;
	private int brushSize = 1;
	
	private Color color1;
	private Color color2;

	private Mode currentMode;
	
	private volatile Rectangle selectedRectangle;
	private GUIInterface guiInterface;
	private ImagePanelInterface ipInterface = new ImagePanelInterface() {
		@Override
		public void undo() {
			history.rewindVersion();
			repaint();
		}
		@Override
		public void redo() {
			history.upwindVersion();
			repaint();
		}
		@Override
		public void resetView() {
			ImagePanel.this.resetView();
		}
		@Override
		public void applySelection() {
			ImagePanel.this.applySelection();
		}
		@Override
		public void clearSelection() {
			ImagePanel.this.clearSelection();
		}
		@Override
		public void pasteFromClipboard() {
			ImagePanel.this.pasteFromClipboard();
		}
	};
	
	public ImagePanelInterface getInterface() {
		return ipInterface;
	}

	public void resetImage(int w, int h) {
		boolean firstTime = history.getCurrent() == null;
		if(!firstTime) {
			w = history.getCurrent().getWidth();
			h = history.getCurrent().getHeight();
		}
		BufferedImage defaultImage = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = defaultImage.getGraphics();
		g.setColor(new Color(0, 0, 0, 0));
		g.fillRect(0, 0, defaultImage.getWidth(), defaultImage.getHeight());
		g.dispose();
		setImage(defaultImage);
		if(firstTime) {
			resetView();
		}
	}
	public ImagePanel() {
		resetImage(64, 64);
		color1 = Color.black;
		color2 = new Color(0, 0, 0, 0);
		this.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				Point pixelPosition = getPixelPosition(mousePosition);
				pixelPosition.x = Math.max(0, Math.min(pixelPosition.x, history.getCurrent().getWidth()));
				pixelPosition.y = Math.max(0, Math.min(pixelPosition.y, history.getCurrent().getHeight()));
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
				if(e.isControlDown()) {
					if(e.getKeyCode() == KeyEvent.VK_N) {
						resetImage(history.getCurrent().getWidth(), history.getCurrent().getHeight());
					}
					if(e.getKeyCode() == KeyEvent.VK_V) {
						ipInterface.pasteFromClipboard();
					}
					else if(e.getKeyCode() == KeyEvent.VK_C) {
						if(selectedImage != null) {
							ClipboardImage.setClipboard(selectedImage);
						}
						else {
							ClipboardImage.setClipboard(getCurrentImage());
						}
					}
					if(e.getKeyCode() == KeyEvent.VK_Z) {
						if(e.isShiftDown()) {
							ipInterface.redo();
						}
						else {
							ipInterface.undo();
						}
					}
				}
				else {
					if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						ipInterface.applySelection();
					}
					else if(e.getKeyCode() == KeyEvent.VK_DELETE) {
						ipInterface.clearSelection();
					}
				}
			}
		});
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mousePosition = e.getPoint();
				mouseButtonDown = e.getButton();
				previousMousePosition = mousePosition;
				if(mouseButtonDown == MouseEvent.BUTTON2) {
					movingImage = true;
					xStart = e.getX();
					yStart = e.getY();
				}
				else if(currentMode == Mode.MOVE) {
					if(selectedRectangle != null) {
						movingSelection = true;
					}
				}
				else if(currentMode == Mode.SELECT) {
					resetSelection();
					updateSelectionRectangle();
				}
				else {
					draw(getPixelPosition(mousePosition), e.isShiftDown());
				}
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mousePosition = e.getPoint();
				if(e.getButton() == mouseButtonDown) {
					mouseButtonDown = 0;
				}
				if (e.getButton() == MouseEvent.BUTTON2) {
					movingImage = false;
				}
				else if(currentMode == Mode.MOVE) {
					movingSelection = false;
				}
				else if(currentMode == Mode.SELECT) {
					updateSelectionRectangle();
					updateSelection();
					guiInterface.finishedSelection();
				}
				else {
					history.pushVersion();
					repaint();
				}
				previousMousePosition = mousePosition;
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				mousePosition = e.getPoint();
				repaint();
			}
			@Override
			public void mouseExited(MouseEvent e) {
				mousePosition = null;
				repaint();
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
				}
				else if(movingSelection) {
					Point previous = getPixelPosition(previousMousePosition);
					Point pixelPosition = getPixelPosition(mousePosition);
					selectedRectangle.x += pixelPosition.x - previous.x;
					selectedRectangle.y += pixelPosition.y - previous.y;
					previousMousePosition = mousePosition;
				}
				else if(currentMode == Mode.SELECT) {
					updateSelectionRectangle();
				}
				else {
					draw(getPixelPosition(mousePosition), e.isShiftDown());
					previousMousePosition = mousePosition;
				}
				repaint();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				mousePosition = e.getPoint();
				repaint();
			}
		});
	}
	
	
	public void setGUIInterface(GUIInterface guiInterface) {
		this.guiInterface = guiInterface;
	}
	
	public void draw(Point pixel, boolean shiftDown) {
		history.modified();
		Color setTo = color1;
		if(mouseButtonDown == MouseEvent.BUTTON3) {
			setTo = color2;
		}
		Point lowerBound = new Point(pixel.x - brushSize/2, pixel.y - brushSize/2);
		Point upperBound = new Point(lowerBound.x + brushSize - 1, lowerBound.y + brushSize - 1);
		lowerBound.x = Math.max(lowerBound.x, 0);
		lowerBound.y = Math.max(lowerBound.y, 0);
		
		upperBound.x = Math.min(upperBound.x, history.getCurrent().getWidth()-1);
		upperBound.y = Math.min(upperBound.y, history.getCurrent().getHeight()-1);
		
		if (currentMode == Mode.COLOR_SELECT) {
			matchColorDraw(lowerBound, upperBound, setTo);
		} 
		else 
		if (currentMode == Mode.FILL) {
			fill(lowerBound, upperBound, setTo);
		}
		else if (currentMode == Mode.BRUSH) {
			brush(lowerBound, upperBound, setTo);
		}
		repaint();
	}
	
	public void updateSelectionRectangle() {
		Point one = getPixelPosition(mousePosition);
		Point two = getPixelPosition(previousMousePosition);
		int minx = Math.max(Math.min(one.x, two.x), 0);
		int miny = Math.max(Math.min(one.y, two.y), 0);
		int maxx = Math.min(Math.max(one.x, two.x), history.getCurrent().getWidth()-1);
		int maxy = Math.min(Math.max(one.y, two.y), history.getCurrent().getHeight()-1);
		Rectangle selected = new Rectangle(minx, miny, maxx-minx, maxy-miny);
		selectedRectangle = selected;
	}
	
	public void updateSelection() {
		history.modified();
		BufferedImage subimage = history.getCurrent().getSubimage(selectedRectangle.x, selectedRectangle.y, selectedRectangle.width + 1, selectedRectangle.height + 1);
		selectedImage = Utils.copyImage(subimage);
		brush(new Point(selectedRectangle.x, selectedRectangle.y), new Point(selectedRectangle.x+selectedRectangle.width, selectedRectangle.y + selectedRectangle.height), color2);
		repaint();
	}
	
	public void resizeCanvas(int xpos, int ypos, int width, int height) {
		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = newImage.getGraphics();
		g.drawImage(history.getCurrent(), xpos, ypos, null);
		g.dispose();
		history.setCurrentImage(newImage);
	}
	
	private void pasteFromClipboard() {
		Image image = Utils.getImageFromClipboard();
		if(image != null) {
			ipInterface.applySelection();
			selectedImage = Utils.toBufferedImage(image); 
			BufferedImage curImage = getCurrentImage();
			selectedRectangle = new Rectangle(curImage.getWidth()/2 - selectedImage.getWidth()/2, curImage.getHeight()/2 - selectedImage.getHeight()/2, selectedImage.getWidth()-1, selectedImage.getHeight()-1);
			repaint();
		}
	}
	
	private void clearSelection() {
		selectedImage = null;
		selectedRectangle = null;
		repaint();
	}
	
	private void applySelection() {
		if(selectedRectangle == null || selectedImage == null) {
			return;
		}
		history.modified();
		int minx = Math.min(selectedRectangle.x, 0);
		int miny = Math.min(selectedRectangle.y, 0);
		int maxx = Math.max(selectedRectangle.x + selectedImage.getWidth(), history.getCurrent().getWidth());
		int maxy = Math.max(selectedRectangle.y + selectedImage.getHeight(), history.getCurrent().getHeight());
		if(maxx - minx != history.getCurrent().getWidth() || maxy - miny != history.getCurrent().getHeight()) {
			System.out.println("resizing");
			int x = 0;
			int y = 0;
			if(selectedRectangle.x < 0) {
				x = -selectedRectangle.x;
			}
			if(selectedRectangle.y < 0) {
				y = -selectedRectangle.y;
			}
			resizeCanvas(x, y, maxx - minx, maxy - miny);
			selectedRectangle.x += x;
			selectedRectangle.y += y;
			xOffset -= x*pixelSize;
			yOffset -= y*pixelSize;
		}
		for(int i = 0; i < selectedImage.getWidth(); i++) {
			for(int j = 0; j < selectedImage.getHeight(); j++) {
				int x = i + selectedRectangle.x;
				int y = j + selectedRectangle.y;
				history.getCurrent().setRGB(x, y, selectedImage.getRGB(i, j));
			}
		}
		resetSelection();
		history.pushVersion();
		repaint();
	}
	
	public void resetSelection() {
		selectedImage = null;
		selectedRectangle = null;
	}
	
	public Point getPixelPosition(Point screenPos) {
		Point pixel = new Point();
		pixel.x = (int) ((screenPos.x - xOffset)/pixelSize);
		pixel.y = (int) ((screenPos.y - yOffset)/pixelSize);
		return pixel;
	}

	public BufferedImage getCurrentImage() {
		return history.getCurrent();
	}

	public void setImage(BufferedImage image) {
		history.setInitialImage(Utils.copyImage(image));
		repaint();
	}
	
	private void resetView() {
		double xfit = 1.0*getWidth()/history.getCurrent().getWidth();
		double yfit = 1.0*getHeight()/history.getCurrent().getHeight();
		pixelSize = Math.min(xfit, yfit) * 0.95;
		xOffset = (int) (getWidth()/2 - pixelSize * history.getCurrent().getWidth()/2);
		yOffset = (int) (getHeight()/2 - pixelSize * history.getCurrent().getHeight()/2);
		repaint();
	}
	
	private LinkedList<Pixel> getNeighbors(Pixel pixel) {
		LinkedList<Pixel> neighbors = new LinkedList<>();
		neighbors.add(new Pixel(pixel.x - 1, pixel.y));
		neighbors.add(new Pixel(pixel.x + 1, pixel.y));
		neighbors.add(new Pixel(pixel.x, pixel.y - 1));
		neighbors.add(new Pixel(pixel.x, pixel.y + 1));
		return neighbors;
	}
	
	private void brush(Point lowerBound, Point upperBound, Color setTo) {
		
		for(int i = lowerBound.x; i <= upperBound.x; i++) {
			for(int j = lowerBound.y; j <= upperBound.y; j++) {
				history.getCurrent().setRGB(i, j, setTo.getRGB());
			}
		}
	}
	private void fill(Point lowerBound, Point upperBound, Color setTo) {
		HashSet<Integer> colors = new HashSet<>();
		HashSet<Pixel> visited = new HashSet<>();
		LinkedList<Pixel> search = new LinkedList<Pixel>();
		for(int i = lowerBound.x; i <= upperBound.x; i++) {
			for(int j = lowerBound.y; j <= upperBound.y; j++) {
				Pixel start = new Pixel(i, j);
				search.add(start);
				colors.add(history.getCurrent().getRGB(i, j));
				visited.add(start);
			}
		}
		while (!search.isEmpty()) {
			Pixel pixel = search.removeFirst();
			history.getCurrent().setRGB(pixel.x, pixel.y, setTo.getRGB());
//			setSelected(pixel.x, pixel.y, setTo);
			for(Pixel neighbor : getNeighbors(pixel)) {
				if(!visited.contains(neighbor) && neighbor.x >= 0 && neighbor.y >= 0 && neighbor.x < history.getCurrent().getWidth() && neighbor.y < history.getCurrent().getHeight()) {
					visited.add(neighbor);
					if (colors.contains(history.getCurrent().getRGB(neighbor.x, neighbor.y))) {
						search.add(neighbor);
					}
				}
			}
		}
	}

	private void matchColorDraw(Point lowerBound, Point upperBound, Color setTo) {
		HashSet<Integer> colors = new HashSet<>();
		for(int i = lowerBound.x; i <= upperBound.x; i++) {
			for(int j = lowerBound.y; j <= upperBound.y; j++) {
				colors.add(history.getCurrent().getRGB(i, j));
			}
		}
		if(colors.isEmpty()) {
			return;
		}
		for (int i = 0; i < history.getCurrent().getWidth(); i++) {
			for (int j = 0; j < history.getCurrent().getHeight(); j++) {
				if(colors.contains(history.getCurrent().getRGB(i, j))) {
					history.getCurrent().setRGB(i, j, setTo.getRGB());
				}
			}
		}
	}

	public void setMode(int modeIndex) {
		currentMode = Mode.values()[modeIndex];
	}
	public void setMode(Mode mode) {
		currentMode = mode;
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
	
	public Point pixelPositionToDrawingPosition(Point pixel) {
		Point drawingPosition = new Point((int)(pixel.x * pixelSize), (int)(pixel.y * pixelSize));
		return drawingPosition;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());
		int stripeWidth = 10;
		for (int i = 0; i < getWidth(); i += stripeWidth) {
			g.setColor(new Color((int) (i * 255 / getWidth()),
					(int) (i * 255 / getWidth() ),
					(int) (i * 255 / getWidth())));
			g.fillRect(i, 0, stripeWidth, getHeight());
		}
		
		Graphics2D g2d = (Graphics2D)g;
		int strokeSize = 2;
		g2d.setStroke(new BasicStroke(strokeSize));
		
		g.translate(xOffset, yOffset);
		g.drawImage(history.getCurrent(), 0, 0, (int)(history.getCurrent().getWidth()*pixelSize), (int)(history.getCurrent().getHeight()*pixelSize), null);
		g.setColor(Color.white);
		g.drawRect(-strokeSize*2, -strokeSize*2, (int)(history.getCurrent().getWidth()*pixelSize) + strokeSize*4, (int)(history.getCurrent().getHeight()*pixelSize) + strokeSize*4);
		g.setColor(Color.black);
		g.drawRect(-strokeSize, -strokeSize, (int)(history.getCurrent().getWidth()*pixelSize) + strokeSize*2, (int)(history.getCurrent().getHeight()*pixelSize) + strokeSize*2);

		if(selectedImage != null) {
			g.drawImage(selectedImage, (int) (selectedRectangle.x*pixelSize)+1, (int) (selectedRectangle.y*pixelSize)+1, (int) ((selectedRectangle.width+1)*pixelSize)-1, (int) ((selectedRectangle.height+1)*pixelSize)-1, null);
		}
		if(selectedRectangle != null) {
			g.setColor(Color.gray);
			g.drawRect((int) (selectedRectangle.x*pixelSize), (int) (selectedRectangle.y*pixelSize), (int) ((selectedRectangle.width+1)*pixelSize)-1, (int) ((selectedRectangle.height+1)*pixelSize)-1);
		}
		if(mousePosition != null) {
			Point pixelPosition = getPixelPosition(mousePosition);
			int minx = (int) ((pixelPosition.x - brushSize/2) * pixelSize);
			int miny = (int) ((pixelPosition.y - brushSize/2) * pixelSize);
			int maxx = (int) ((pixelPosition.x - brushSize/2 + brushSize) * pixelSize) - 1;
			int maxy = (int) ((pixelPosition.y - brushSize/2 + brushSize) * pixelSize) - 1;
			g.setColor(Color.black);
			g.drawRect(minx, miny, maxx-minx, maxy-miny);
			g.setColor(Color.white);
			g.drawRect(minx + strokeSize, miny + strokeSize, maxx-minx - strokeSize*2, maxy-miny - strokeSize*2);
			if(PaintDriver.DEBUG) {
				g.setColor(Color.green);
				g.drawString(pixelSize + "", 10, getHeight() - 70);
				g.drawString(xOffset + "," + yOffset, 10, getHeight() - 50);
				g.drawString(pixelPosition.x + "," + pixelPosition.y, 10, getHeight() - 30);
				g.drawString(mousePosition.x + "," + mousePosition.y, 10, getHeight() - 10);
			}
		}

		g.translate(-xOffset, -yOffset);
		g.setColor(Color.green);
		g.setFont(PaintDriver.MAIN_FONT);
		g.drawString("Brush Size: " + brushSize, 10, getHeight() - 25);
		g.drawString("w,h: " + history.getCurrent().getWidth() + "," + history.getCurrent().getHeight(), 10, getHeight() - 10);

		int historyPreviewSize = 70;
		int historyPreviewOffset = 10;
		for(int i = 0; i < history.getHistory().size(); i++) {
			g.drawImage(history.getHistory().get(i), getWidth() - historyPreviewOffset - historyPreviewSize, historyPreviewOffset + i*(historyPreviewOffset + historyPreviewSize), historyPreviewSize, historyPreviewSize, null);
			g.setColor(Color.black);
			g2d.setStroke(new BasicStroke(1));
			g.drawRect(getWidth() - historyPreviewOffset - historyPreviewSize, historyPreviewOffset + i*(historyPreviewOffset + historyPreviewSize), historyPreviewSize, historyPreviewSize);

			if(i == history.getCursor()) {
				g.setColor(Color.green);
				g2d.setStroke(new BasicStroke(3));
				g.drawRect(getWidth() - historyPreviewOffset*3/2 - historyPreviewSize, historyPreviewOffset/2 + i*(historyPreviewOffset + historyPreviewSize), historyPreviewSize + historyPreviewOffset, historyPreviewSize + historyPreviewOffset);
			}
		}
	}

}
