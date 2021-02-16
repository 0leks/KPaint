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

public class KPaintDriver {
	public static final Font MAIN_FONT = new Font("Comic Sans MS", Font.PLAIN, 15);
	public static final Font MAIN_FONT_BIG = new Font("Cooper Black", Font.PLAIN, 16);
	public static final boolean DEBUG = false;
	
	
	private JFrame frame;
	private ImagePanel imagePanel;
	private ImagePanelInterface imagePanelInterface;
	private JButton openFile;
	private JButton saveFile;

	private JPanel controlPanel;
	private GUIInterface guiInterface;
	
	private HashMap<BrushMode, KRadioButton> modeButtons = new HashMap<>();

	final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

	public KPaintDriver() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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

		int min = 1;
		int max = 21;
		int spacing = 4;
		JSlider brushSize = new JSlider(JSlider.HORIZONTAL, min, max, 1);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		for (int i = min; i <= max; i += spacing) {
			labelTable.put(i, new JLabel(i + ""));
		}
		brushSize.setLabelTable(labelTable);
		brushSize.setPaintLabels(true);
		brushSize.setMajorTickSpacing(spacing);
		brushSize.setPaintTicks(true);
		brushSize.setFocusable(false);
		brushSize.addChangeListener(e -> {
			// int size = (int) Math.pow(2, brushSize.getValue());
			imagePanel.setBrushSize(brushSize.getValue());
		});

		String[] options = new String[BrushMode.values().length];
		for (int i = 0; i < options.length; i++) {
			options[i] = BrushMode.values()[i].toString();
		}
		controlPanel = new JPanel();
		KButton undoButton = setupKButton("Undo", "resources/undo.png");
		undoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanelInterface.undo();
			}
		});
		controlPanel.add(undoButton);
		KButton redoButton = setupKButton("Redo", "resources/redo.png");
		redoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanelInterface.redo();
			}
		});
		controlPanel.add(redoButton);
		
		KButton applyButton = setupKButton("Apply Selection", "resources/apply.png");
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanelInterface.applySelection();
			}
		});
		controlPanel.add(applyButton);
		
		JToggleButton toggleTiling = setupJToggleButton("Tiling", "resources/tiling_icon.png");
		toggleTiling.addActionListener(e -> {
			imagePanelInterface.showTiling(toggleTiling.isSelected());
		});
		controlPanel.add(toggleTiling);
		
		ButtonGroup group = new ButtonGroup();
		for(BrushMode mode : BrushMode.values()) {
			KRadioButton modeButton = new KRadioButton(mode.toString());
			modeButton.setIcon(mode.getImageIcon());
			modeButton.setBorder(BorderFactory.createLineBorder(Color.black, 1));
			modeButton.setBorderPainted(true);
//			modeButton.setBackground(Color.black);
			modeButton.setFocusable(false);
			modeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					imagePanel.setBrushMode(mode);
				}
			});
			controlPanel.add(modeButton);
			group.add(modeButton);
			if(mode == BrushMode.MOVE) {
				modeButton.setSelected(true);
				imagePanel.setBrushMode(mode);
			}
			modeButtons.put(mode, modeButton);
		}
		
		controlPanel.add(brushSize);

		JButton color1 = KBrushColorButton.setupColorButton("Main", new HasColor() {
			@Override
			public Color getColor() {
				return imagePanel.getColor1();
			}
			@Override
			public void setColor(Color color) {
				imagePanelInterface.setColor1(color);
			}
		});
		controlPanel.add(color1);
		
		JButton color2 = KBrushColorButton.setupColorButton("Shift", new HasColor() {
			@Override
			public Color getColor() {
				return imagePanel.getColor2();
			}
			@Override
			public void setColor(Color color) {
				imagePanelInterface.setColor2(color);
			}
		});
		controlPanel.add(color2);

		openFile = setupKButton("Open File", "resources/open.png");
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

		saveFile = setupKButton("Save File", "resources/save.png");
		saveFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				guiInterface.save();
			}
		});
		controlPanel.add(saveFile);

		KButton newFile = setupKButton("New Canvas", "resources/new_canvas.png");
		newFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				imagePanelInterface.newCanvas();
			}
		});
		controlPanel.add(newFile);

		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				frameResized();
			}
		});
		frame.add(controlPanel, BorderLayout.NORTH);
		frameResized();
		imagePanelInterface.resetView();
		frame.repaint();
		imagePanel.requestFocus();
		
		guiInterface = new GUIInterface() {
			@Override
			public void finishedSelection() {
				modeButtons.get(BrushMode.MOVE).doClick();
			}
			@Override
			public void changedColor() {
				color1.repaint();
				color2.repaint();
				modeButtons.get(BrushMode.BRUSH).doClick();
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
		imagePanel.setGUIInterface(guiInterface);
		
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
	}
	
	private KButton setupKButton(String text, String iconPath) {
		KButton button = new KButton(text);
		button.setIcon(Utils.resizeImageIcon(Utils.loadImageIconResource(iconPath), 32, 32));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFocusable(false);
		button.setFocusPainted(false);
		button.setBackground(Color.black);
		button.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		button.setBorderPainted(true);
		return button;
	}
	private JToggleButton setupJToggleButton(String text, String iconPath) {
		JToggleButton button = new JToggleButton(text);
		button.setIcon(Utils.resizeImageIcon(Utils.loadImageIconResource(iconPath), 32, 32));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFocusable(false);
		button.setFocusPainted(false);
		button.setBackground(Color.black);
		button.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		button.setBorderPainted(true);
		return button;
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
	
	private void frameResized() {
		SwingUtilities.invokeLater(() -> {
			if(frame.getWidth() >= 1400) {
				controlPanel.setPreferredSize(null);
			}
			else if (frame.getWidth() <= 1300){
				controlPanel.setPreferredSize(new Dimension(670, 100));
			}
			controlPanel.validate();
			frame.revalidate();
			frame.repaint();
		});
	}

	public static void main(String[] args) {
		KPaintDriver p = new KPaintDriver();
		if (args.length > 0) {
			p.openImage(args[0]);
		}
	}

}
