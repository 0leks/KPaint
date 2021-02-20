package ok.kpaint;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import ok.*;
import ok.kpaint.ImagePanel.*;
import ok.kui.*;

public class DriverKPaint {
	public static final Font MAIN_FONT = new Font("Comic Sans MS", Font.PLAIN, 15);
	public static final Font MAIN_FONT_BIG = new Font("Cooper Black", Font.PLAIN, 16);
	public static final boolean DEBUG = false;
	
	
	private JFrame frame;
	private ImagePanel imagePanel;
	private ImagePanelInterface imagePanelInterface;

	private GUIInterface guiInterface;
	private ControllerInterface controllerInterface;
	

	final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

	public DriverKPaint() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		controllerInterface = new ControllerInterface() {
			@Override
			public void open() {
				int returnVal = fc.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					openImage(file.getAbsolutePath());
				}
			}
			
			@Override
			public void save() {
				int returnVal = fc.showSaveDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String path = file.getAbsolutePath();
					String ext = getExtension(path);
					if(ext == null) {
						ext = "png";
						file = new File(path + "." + ext);
//						JOptionPane.showMessageDialog(frame, "Failed to save, no file extension specified");
					}
					BufferedImage current = imagePanel.getCurrentImage();
					try {
						ImageIO.write(current, ext, file);
					} catch (IOException e1) {
						System.err.println("FileName = " + path);
						e1.printStackTrace();
					}
				}
			}
		};
		
		frame = new JFrame("KPaint 1.0");
		frame.setSize((int)(Toolkit.getDefaultToolkit().getScreenSize().width*0.9), (int)(Toolkit.getDefaultToolkit().getScreenSize().height*0.9));
		frame.setMinimumSize(new Dimension(670, 500));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);

		frame.setIconImage(Utils.loadImageIconResource("resources/icon.png").getImage());
		frame.setVisible(true);
		
		imagePanel = new ImagePanel();
		imagePanelInterface = imagePanel.getInterface();
		frame.add(imagePanel, BorderLayout.CENTER);
		
		GUIPanel guiPanel = new GUIPanel(controllerInterface, imagePanelInterface);
		guiPanel.setupWithTitles();
		frame.add(guiPanel, BorderLayout.WEST);

		imagePanelInterface.resetView();
		frame.repaint();
		imagePanel.requestFocus();
		
		guiInterface = new GUIInterface() {
			@Override
			public void finishedSelection() {
				guiPanel.clickModeButton(BrushMode.MOVE);
			}
			@Override
			public void changedColor() {
				frame.repaint();
				guiPanel.clickModeButton(BrushMode.BRUSH);
			}
			@Override
			public void changeModeHotkey(BrushMode mode) {
				guiPanel.clickModeButton(mode);
			}
			@Override
			public void switchLayout(boolean withTitles) {
				if(withTitles) {
					guiPanel.setupWithTitles();
				}
				else {
					guiPanel.setupCompact();
				}
			}
		};
		
		imagePanel.setGUIInterface(guiInterface);
		imagePanel.setControllerInterface(controllerInterface);
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException ex) {
				}
			}
		});
		
		
		
		
		frame.repaint();
		frame.revalidate();
		imagePanelInterface.resetView();
	}
	
	private String getExtension(String filename) {
		int lastDot = filename.lastIndexOf(".");
		if(lastDot == -1) {
			return null;
		}
		return filename.substring(lastDot+1);
	}

	private void openImage(String path) {
		BufferedImage image = loadImage(path);
		if (image != null) {
			imagePanel.setImage(image);
			imagePanelInterface.resetView();
		}
	}

	public BufferedImage loadImage(String fileName) {
		File file = new File(fileName);
		try {
			BufferedImage read = ImageIO.read(file);
			fc.setCurrentDirectory(file.getParentFile());
			fc.setSelectedFile(file);
			return read;
		} catch (IOException e) {
			System.err.println("File name = " + fileName);
			e.printStackTrace();
		}
		return null;
	}

	// old stuff from when gui was a bar at the top of the app
//	private void frameResized() {
//		SwingUtilities.invokeLater(() -> {
//			if(frame.getWidth() >= 1400) {
//				controlPanel.setPreferredSize(null);
//			}
//			else if (frame.getWidth() <= 1300){
//				controlPanel.setPreferredSize(new Dimension(670, 100));
//			}
//			controlPanel.validate();
//			frame.revalidate();
//			frame.repaint();
//		});
//	}

	public static void main(String[] args) {
		DriverKPaint p = new DriverKPaint();
		if (args.length > 0) {
			p.openImage(args[0]);
		}
	}

}
