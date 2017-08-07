import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private BufferedImage original;
  private BufferedImage current;
  private double scale;
  private int pixelSize;
  private int xOffset;
  private int yOffset;
  private int xStart;
  private int yStart;
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
    FILL_SELECT("Fill Select"), NONE("None");
    private String name;
    Mode(String name) {
      this.name = name;
    }
    @Override
    public String toString() {
      return name;
    }
  }
  private Mode currentMode = Mode.NONE;
  
  private void setScale(double scale) {
    this.scale = scale;
    pixelSize = (int) (20 * this.scale);
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
    this.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
      }
      @Override
      public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
      }
      @Override
      public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
      }
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          if (currentMode == Mode.FILL_SELECT) {
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
        Color c = new Color(current.getRGB(x, y), true);
//        c = new Color(c.getRed(), c.getGreen(), c.getBlue(), );
        g.setColor(c);
        int drawX = xOffset + x * pixelSize;  
        int drawY = yOffset + y * pixelSize;
        g.fillRect(drawX + 1, drawY + 1, pixelSize-2, pixelSize-2);
        if( selected[x][y] ) {
          g.setColor(new Color(255, 0, 0, 150));
          Graphics2D g2d = (Graphics2D)g;
          g2d.setStroke(new BasicStroke(3));
          g.drawLine(drawX, drawY, drawX + pixelSize, drawY + pixelSize);
          g.drawLine(drawX + pixelSize, drawY, drawX, drawY + pixelSize);
//          g.drawRect(drawX, drawY, pixelSize-1, pixelSize-1);
        }
      }
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
