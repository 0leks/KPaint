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
import java.util.LinkedList;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private BufferedImage original;
  private BufferedImage current;
  private double scale;
  private double pixelSize;
  private int xOffset;
  private int yOffset;
  private int xStart;
  private int yStart;
  private Point mousePosition = new Point(0,0);
  private boolean movingImage;
  
  private boolean[][] selected;
  

  public class Pixel {
    private int x;
    private int y;
    public Pixel(int x, int y) {
      this.x = x;
      this.y = y;
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
  
  private void setScale(double scale) {
    this.scale = scale;
    pixelSize = 20 * this.scale;
  }
  public ImagePanel(BufferedImage image) {
    setScale(1);
    setImage(image);
    this.addMouseWheelListener(new MouseWheelListener() {
      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        if( e.getWheelRotation() > 0 ) {
          setScale(scale * 0.9);
        }
        else {
          setScale(scale * 1.1);
        }
        repaint();
      }
    });
    this.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if( e.getKeyCode() == KeyEvent.VK_SPACE ) {
          int mouseX = (int)((mousePosition.x - xOffset)/pixelSize);
          int mouseY = (int)((mousePosition.y - yOffset)/pixelSize);
          if( mouseX >= 0 && mouseX < current.getWidth() && mouseY >= 0 && mouseY < current.getHeight() ) {
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
        if (e.getButton() == MouseEvent.BUTTON1) {
        if (currentMode == Mode.COLOR_SELECT) {
            int pixelX = (int) ((e.getX() - xOffset) / pixelSize);
            int pixelY = (int) ((e.getY() - yOffset) / pixelSize);
            System.err.println(pixelX + "," + pixelY);
            if( pixelX >= 0 && pixelX < current.getWidth() && pixelY >= 0 && pixelY < current.getHeight() ) {
              boolean setTo = !selected[pixelX][pixelY];
              if( !e.isShiftDown() ) {
                deselectAll();
              }
              colorSelect(pixelX, pixelY, setTo);
            }
          }
        else if (currentMode == Mode.FILL_SELECT) {
            int pixelX = (int) ((e.getX() - xOffset) / pixelSize);
            int pixelY = (int) ((e.getY() - yOffset) / pixelSize);
            System.err.println(pixelX + "," + pixelY);
            if( pixelX >= 0 && pixelX < current.getWidth() && pixelY >= 0 && pixelY < current.getHeight() ) {
              boolean setTo = !selected[pixelX][pixelY];
              if( !e.isShiftDown() ) {
                deselectAll();
              }
              fillSelect(pixelX, pixelY, setTo);
            }
          }
          else if(currentMode == Mode.SINGLE_SELECT) {
            int pixelX = (int) ((e.getX() - xOffset) / pixelSize);
            int pixelY = (int) ((e.getY() - yOffset) / pixelSize);
            System.err.println(pixelX + "," + pixelY);
            if( pixelX >= 0 && pixelX < current.getWidth() && pixelY >= 0 && pixelY < current.getHeight() ) {
              boolean setTo = !selected[pixelX][pixelY];
              if( !e.isShiftDown() ) {
                deselectAll();
              }
              selected[pixelX][pixelY] = setTo;
            }
          }
        }
        if (e.getButton() == MouseEvent.BUTTON2) {
          movingImage = true;
          xStart = e.getX();
          yStart = e.getY();
        }
        repaint();
      }
      @Override
      public void mouseReleased(MouseEvent e) {
        if( e.getButton() == MouseEvent.BUTTON2 ) {
          movingImage = false;
        }
      }
    });
    this.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent e) {
        mousePosition = e.getPoint();
        if( movingImage ) {
          int deltaX = e.getX() - xStart;
          int deltaY = e.getY() - yStart;
          xOffset += deltaX;
          yOffset += deltaY;
          xStart = e.getX();
          yStart = e.getY();
          repaint();
        }
      }
      @Override
      public void mouseMoved(MouseEvent e) {
        mousePosition = e.getPoint();
        repaint();
      }
    });
  }
  
  public BufferedImage getCurrentImage() {
    return current;
  }
  
  public void setImage(BufferedImage image) {
    original = copyImage(image);
    current = copyImage(image);
    selected = new boolean[original.getWidth()][original.getHeight()];
    repaint();
  }
  
  public void setTransparent() {
    for( int x = 0; x < selected.length; x++ ) {
      for( int y = 0; y < selected[x].length; y++ ) {
        if( selected[x][y] ) {
          current.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());
        }
      }
    }
    deselectAll();
    repaint();
  }
  public void deselectAll() {
    selected = new boolean[selected.length][selected[0].length];
  }

  private void colorSelect(int x, int y, boolean setTo) {
    int originalRGB = current.getRGB(x, y);
    for(int i = 0; i < current.getWidth(); i++) {
    	for(int j = 0; j < current.getHeight(); j++) {
    	    int rgb = current.getRGB(i, j);
    	    if(rgb == originalRGB) {
    	        selected[i][j] = setTo;
    	    }
    	}
    }
    repaint();
  }
  private void fillSelect(int x, int y, boolean setTo) {
    Pixel start = new Pixel(x, y);
    LinkedList<Pixel> search = new LinkedList<Pixel>();
    search.add(start);
    while(!search.isEmpty()) {
      Pixel pixel = search.removeFirst();
      int rgb = current.getRGB(x, y);
      selected[pixel.x][pixel.y] = setTo;
      if( pixel.x > 0 && setTo != selected[pixel.x-1][pixel.y] ) {
        if( current.getRGB(pixel.x-1, pixel.y) == rgb ) {
          search.add(0, new Pixel(pixel.x-1, pixel.y) );
        }
      }
      if( pixel.x < selected.length - 1 && setTo != selected[pixel.x+1][pixel.y]) {
        if( current.getRGB(pixel.x+1, pixel.y) == rgb ) {
          search.add(0, new Pixel(pixel.x+1, pixel.y) );
        }
      }
      if( pixel.y > 0 && setTo != selected[pixel.x][pixel.y-1]) {
        if( current.getRGB(pixel.x, pixel.y-1) == rgb ) {
          search.add(0, new Pixel(pixel.x, pixel.y-1) );
        }
      }
      if( pixel.y < selected[0].length - 1 && setTo != selected[pixel.x][pixel.y+1]) {
        if( current.getRGB(pixel.x, pixel.y+1) == rgb ) {
          search.add(0, new Pixel(pixel.x, pixel.y+1) );
        }
      }
    }
    repaint();
  }
  public void setMode(int modeIndex ) {
    currentMode = Mode.values()[modeIndex];
  }
  
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(Color.black);
    g.fillRect(0, 0, getWidth(), getHeight());
    for( int i = 0; i < getWidth(); i += 20 ) {
      g.setColor(new Color((int) ((getWidth()-1-i)*245/getWidth() + Math.random() * 10), (int) (i*245/getWidth() + Math.random() * 10), (int) (i*245/getWidth() + Math.random() * 10)));
      g.fillRect(i, 0, 20, getHeight());
    }
    for( int x = 0; x < current.getWidth(); x++ ) {
      for( int y = 0; y < current.getHeight(); y++ ) {
        int drawX = (int)(xOffset + x * pixelSize);  
        int drawY = (int)(yOffset + y * pixelSize);
        if( drawX >= -pixelSize && drawX < getWidth() + pixelSize && drawY > -pixelSize && drawY < getHeight() + pixelSize ) {
          Color c = new Color(current.getRGB(x, y), true);
          g.setColor(c);
          int drawSize = Math.max((int)pixelSize, 1);
          g.fillRect(drawX, drawY, drawSize, drawSize);
          if( selected[x][y] ) {
            g.setColor(new Color(255, 0, 0, 150));
            g.drawLine(drawX, drawY, drawX + drawSize, drawY + drawSize);
            g.drawLine(drawX + drawSize, drawY, drawX, drawY + drawSize);
  //          g.drawRect(drawX, drawY, pixelSize-1, pixelSize-1);
          }
        }
      }
    }
    int mouseX = (int)((mousePosition.x - xOffset)/pixelSize);
    int mouseY = (int)((mousePosition.y - yOffset)/pixelSize);
    if( mouseX >= 0 && mouseX < current.getWidth() && mouseY >= 0 && mouseY < current.getHeight() ) {
      Color color = new Color(current.getRGB(mouseX, mouseY));
      g.setColor(Color.BLACK);
      g.drawString(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue(), 3, 16);
      g.setColor(Color.YELLOW);
      g.drawString(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue(), 2, 15);
    }
    
  }
  
  public static BufferedImage copyImage(BufferedImage image) {
    BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics g = copy.getGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    return copy;
  }
}
