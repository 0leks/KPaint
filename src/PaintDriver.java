import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class PaintDriver {
  private JFrame frame;
  private ImagePanel imagePanel;
  private JComboBox fillSelect;
  private JButton setTransparent;
  private JButton openFile;
  private JButton saveFile;
  
  private JPanel controlPanel;
  
  final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
  
  public PaintDriver() {
    frame = new JFrame("Paint");
    frame.setSize(1000, 800);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    URL iconURL = getClass().getResource("resources/icon.png");
    if( iconURL != null ) {
      ImageIcon icon = new ImageIcon(iconURL);
      frame.setIconImage(icon.getImage());
    }
    frame.setVisible(true);
    
    
    imagePanel = new ImagePanel(new BufferedImage(100, 100, BufferedImage.TYPE_4BYTE_ABGR));
    frame.add(imagePanel, BorderLayout.CENTER);
    
    String[] options = new String[ImagePanel.Mode.values().length];
    for( int i = 0; i < options.length; i++ ) {
      options[i] = ImagePanel.Mode.values()[i].toString();
    }
    fillSelect = new JComboBox(options);
    fillSelect.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        imagePanel.setMode(fillSelect.getSelectedIndex());
      }
    });
    fillSelect.setSelectedIndex(0);
    controlPanel = new JPanel();
    controlPanel.add(fillSelect);
    
    setTransparent = new JButton("Set Transparent");
    setTransparent.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        imagePanel.setTransparent();
      }
    });
    controlPanel.add(setTransparent);
    
    openFile = new JButton("Open File");
    openFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int returnVal = fc.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            BufferedImage image = loadImage(file.getAbsolutePath());
            if( image != null ) {
              imagePanel.setImage(image);
            }
        }
      }
    });
    controlPanel.add(openFile);
    
    saveFile = new JButton("Save File");
    saveFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int returnVal = fc.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String path = file.getAbsolutePath();
            String format = path.substring(path.length()-3);
            BufferedImage current = imagePanel.getCurrentImage();
            try {
              ImageIO.write(current, format, file);
            } catch (IOException e1) {
              System.err.println("FileName = " + path);
              e1.printStackTrace();
            }
        }
      }
    });
    controlPanel.add(saveFile);
    
    frame.add(controlPanel, BorderLayout.NORTH );
    frame.validate();
    frame.repaint();
  }
  
  public static BufferedImage loadImage(String fileName) {
    File file = new File(fileName);
    try {
      BufferedImage read = ImageIO.read(file);
      return read;
    } catch (IOException e) {
      System.err.println("File name = " + fileName);
      e.printStackTrace();
    }
    return null;
  }
  
  public static void main(String[] args) {
    new PaintDriver();
  }

}
