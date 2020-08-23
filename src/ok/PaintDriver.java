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
	public static final boolean DEBUG = false;
	private JFrame frame;
	private ImagePanel imagePanel;
//	private JComboBox fillSelect;
	private JButton setTransparent;
	private JButton openFile;
	private JButton saveFile;

	private JPanel controlPanel;

	final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

	private static Image icon;

	{
		URL a = PaintDriver.class.getClassLoader().getResource("icon.png");
		if (a != null) {
			ImageIcon ii = new ImageIcon(a);
			icon = ii.getImage();
		}
	}
	
	
	public static Color getBestTextColor(Color c) {
		return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
	}
	
	public JButton setupColorButton(String text, HasColor c) {
		JButton color2 = new JButton(text) {
			@Override
			public void paintComponent(Graphics g) {
				g.setColor(c.getColor());
				g.fillRect(0, 0, getWidth(), getHeight());
//				g.fillRect(2, 2, getWidth() - 4, 10);
//				g.fillRect(2, getHeight() - 12, getWidth() - 4, 10);
				setForeground(Color.white);
				g.setFont(MAIN_FONT);
				setForeground(getBestTextColor(c.getColor()));
				super.paintComponent(g);
			}
		};
		color2.setOpaque(false);
		color2.setContentAreaFilled(false);
		color2.setPreferredSize(new Dimension(80, 40));
		color2.setFocusable(false);
		color2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(null, "Choose Color", c.getColor());
				if(newColor != null) {
					c.setColor(newColor);
				}
			}
		});
		return color2;
	}
	
	public static final ImageIcon loadImageIconResource(String location) {
		URL iconURL = PaintDriver.class.getResource(location);
		if (iconURL != null) {
			return new ImageIcon(iconURL);
		}
		return null;
	}
	public static final ImageIcon resizeImageIcon(ImageIcon icon, int width, int height) {
		Image image = icon.getImage(); // transform it 
		Image newimg = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		return new ImageIcon(newimg);  // transform it back
	}

	public PaintDriver() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		frame = new JFrame("Transparent Paint");
		frame.setSize(1000, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(icon);
		frame.setLocationRelativeTo(null);

		frame.setIconImage(loadImageIconResource("resources/icon.png").getImage());
		frame.setVisible(true);

		imagePanel = new ImagePanel();
		frame.add(imagePanel, BorderLayout.CENTER);

		int min = 1;
		int max = 20;
		int spacing = 5;
		JSlider brushSize = new JSlider(JSlider.HORIZONTAL, min, max, 1);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		for (int i = min; i <= max; i += spacing) {
			// int size = (int) Math.pow(2, i);
			labelTable.put(new Integer(i), new JLabel((i * 2 - 1) + ""));
			i = i - i % spacing;
			System.out.println(i);
		}
		brushSize.setLabelTable(labelTable);
		brushSize.setPaintLabels(true);
		brushSize.setMajorTickSpacing(spacing);
		brushSize.setPaintTicks(true);
		brushSize.setFocusable(false);
		brushSize.addChangeListener(e -> {
			// int size = (int) Math.pow(2, brushSize.getValue());
			imagePanel.setBrushSize(brushSize.getValue() - 1);
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
