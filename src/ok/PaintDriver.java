package ok;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;

import ok.ImagePanel.*;

public class PaintDriver {
	public static final Font MAIN_FONT = new Font("Cooper Black", Font.PLAIN, 14);
	public static final Font MAIN_FONT_BIG = new Font("Cooper Black", Font.PLAIN, 16);
	public static final boolean DEBUG = false;
	private JFrame frame;
	private ImagePanel imagePanel;
//	private JComboBox fillSelect;
	private JButton setTransparent;
	private JButton openFile;
	private JButton saveFile;

	private JPanel controlPanel;
	
	private HashMap<Mode, KRadioButton> modeButtons = new HashMap<>();

	final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

	public JButton setupColorButton(String text, HasColor c) {
		int width = 80;
		int height = 40;
		Image backgroundImage = Utils.resizeImageIcon(Utils.loadImageIconResource("resources/transparentBackground.png"), width, height).getImage();
		JButton chooseColorButton = new JButton(text) {
			@Override
			public void paintComponent(Graphics g) {
				g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
				g.setColor(c.getColor());
				g.fillRect(0, 0, getWidth(), getHeight());
				setForeground(Color.white);
				g.setFont(MAIN_FONT_BIG);
				setForeground(Utils.getBestTextColor(c.getColor()));
				super.paintComponent(g);
			}
		};
		chooseColorButton.setOpaque(false);
		chooseColorButton.setContentAreaFilled(false);
		chooseColorButton.setPreferredSize(new Dimension(width, height));
		chooseColorButton.setFocusable(false);
		chooseColorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(null, "Choose Color", c.getColor());
				if(newColor != null) {
					c.setColor(newColor);
				}
			}
		});
		return chooseColorButton;
	}

	public PaintDriver() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		frame = new JFrame("Transparent Paint");
		frame.setSize((int)(Toolkit.getDefaultToolkit().getScreenSize().width*0.9), (int)(Toolkit.getDefaultToolkit().getScreenSize().height*0.9));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);

		frame.setIconImage(Utils.loadImageIconResource("resources/icon.png").getImage());
		frame.setVisible(true);
		
		imagePanel = new ImagePanel();
		frame.add(imagePanel, BorderLayout.CENTER);

		int min = 1;
		int max = 21;
		int spacing = 4;
		JSlider brushSize = new JSlider(JSlider.HORIZONTAL, min, max, 1);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		for (int i = min; i <= max; i += spacing) {
			// int size = (int) Math.pow(2, i);
//			labelTable.put(new Integer(i), new JLabel((i * 2 - 1) + ""));
			labelTable.put(new Integer(i), new JLabel(i + ""));
//			i = i - i % spacing;
			System.out.println(i);
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

		String[] options = new String[ImagePanel.Mode.values().length];
		for (int i = 0; i < options.length; i++) {
			options[i] = ImagePanel.Mode.values()[i].toString();
		}
		controlPanel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		for(Mode mode : ImagePanel.Mode.values()) {
			KRadioButton modeButton = new KRadioButton(mode.toString());
			modeButton.setIcon(mode.getImageIcon());
			modeButton.setBorder(BorderFactory.createLineBorder(Color.black, 1));
			modeButton.setBorderPainted(true);
//			modeButton.setBackground(Color.black);
			modeButton.setFocusable(false);
			modeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					imagePanel.setMode(mode);
				}
			});
			controlPanel.add(modeButton);
			group.add(modeButton);
			if(mode == Mode.BRUSH) {
				modeButton.setSelected(true);
				imagePanel.setMode(mode);
			}
			modeButtons.put(mode, modeButton);
		}
		
		controlPanel.add(brushSize);

		JButton color1 = setupColorButton("Left", new HasColor() {
			@Override
			public Color getColor() {
				return imagePanel.getColor1();
			}
			@Override
			public void setColor(Color color) {
				imagePanel.setColor1(color);
			}
		});
		controlPanel.add(color1);
		
		JButton color2 = setupColorButton("Right", new HasColor() {
			@Override
			public Color getColor() {
				return imagePanel.getColor2();
			}
			@Override
			public void setColor(Color color) {
				imagePanel.setColor2(color);
			}
		});
		controlPanel.add(color2);

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
					String format = path.substring(path.length() - 3);
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

		frame.add(controlPanel, BorderLayout.NORTH);
		frame.validate();
		imagePanel.resetView();
		frame.repaint();
		imagePanel.requestFocus();
		
		GUIInterface guiInterface = new GUIInterface() {
			@Override
			public void finishedSelection() {
//				modeButtons.get(Mode.SELECT).setSelected(false);
//				modeButtons.get(Mode.MOVE).setSelected(true);
				modeButtons.get(Mode.MOVE).doClick();
			}
		};
		imagePanel.setGUIInterface(guiInterface);
	}

	private void openImage(String path) {
		BufferedImage image = loadImage(path);
		if (image != null) {
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
		if (args.length > 0) {
			p.openImage(args[0]);
		}
	}

}
