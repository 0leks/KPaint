import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;

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
	try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }catch(Exception ex) {
        ex.printStackTrace();
    }
    frame = new JFrame("Transparent Paint");
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
    fillSelect = new JComboBox<String>(options);
    fillSelect.setFocusable(false);
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
    setTransparent.setFocusable(false);
    setTransparent.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        imagePanel.setTransparent();
      }
    });
    controlPanel.add(setTransparent);
    
    openFile = new JButton("Open File");
    openFile.setFocusable(false);
    openFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int returnVal = fc.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            openImage(file.getAbsolutePath());
        }
      }
    });
    controlPanel.add(openFile);
    
    saveFile = new JButton("Save File");
    saveFile.setFocusable(false);
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
    imagePanel.requestFocus();
  }
  
  private void openImage(String path) {
      BufferedImage image = loadImage(path);
      if( image != null ) {
        imagePanel.setImage(image);
      }
  }
  
  public BufferedImage loadImage(String fileName) {
    File file = new File(fileName);
    try {
      BufferedImage read = ImageIO.read(file);
	  fc.setCurrentDirectory(file.getParentFile());
      return read;
    } catch (IOException e) {
      System.err.println("File name = " + fileName);
      e.printStackTrace();
    }
    return null;
  }
  
  public static void main(String[] args) {
	PaintDriver p = new PaintDriver();
	if(args.length > 0) {
		p.openImage(args[0]);
	}
  }

}
