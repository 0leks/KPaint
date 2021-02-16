package ok.kpaint;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import ok.kui.*;

public class GUIPanel extends JPanel {

	private ControllerInterface controllerInterface;
	private ImagePanelInterface imagePanelInterface;
	private HashMap<BrushMode, KRadioButton> modeButtons = new HashMap<>();
	
	public GUIPanel(ControllerInterface controllerInterface, ImagePanelInterface imagePanelInterface) {
		this.controllerInterface = controllerInterface;
		this.imagePanelInterface = imagePanelInterface;
		this.setLayout(new GridBagLayout());
	}
	
	public void clickModeButton(BrushMode mode) {
		modeButtons.get(BrushMode.MOVE).doClick();
	}
	
	private ButtonGroup setupModeButtons() {
		ButtonGroup group = new ButtonGroup();
		for(BrushMode mode : BrushMode.values()) {
			KRadioButton modeButton = KUI.setupKRadioButton("", mode.toString(), mode.getImageIcon());
			modeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					imagePanelInterface.setBrushMode(mode);
				}
			});
			group.add(modeButton);
			if(mode == BrushMode.MOVE) {
				modeButton.setSelected(true);
				imagePanelInterface.setBrushMode(mode);
			}
			modeButtons.put(mode, modeButton);
		}
		return group;
	}

	
	public void setup() {
		setupModeButtons();
		GridBagConstraints c = new GridBagConstraints();

		KButton openFile = KUI.setupKButton("", "Open File", "resources/open.png");
		openFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				controllerInterface.open();
			}
		});

		KButton saveFile = KUI.setupKButton("", "Save File", "resources/save.png");
		saveFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controllerInterface.save();
			}
		});


		KButton newFile = KUI.setupKButton("", "New Canvas", "resources/new_canvas.png");
		newFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				imagePanelInterface.newCanvas();
			}
		});
		
		
		KButton undoButton = KUI.setupKButton("", "Undo", "resources/undo.png");
		undoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanelInterface.undo();
			}
		});

		KButton redoButton = KUI.setupKButton("", "Redo", "resources/redo.png");
		redoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanelInterface.redo();
			}
		});
		

		
		KButton applyButton = KUI.setupKButton("", "Apply Selection", "resources/apply.png");
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanelInterface.applySelection();
			}
		});

		JToggleButton toggleTiling = KUI.setupKToggleButton("", "Tiling", "resources/tiling_icon.png");
		toggleTiling.addActionListener(e -> {
			imagePanelInterface.showTiling(toggleTiling.isSelected());
		});

		// ############ ROW 4 ################## 
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
			imagePanelInterface.setBrushSize(brushSize.getValue());
		});
		

		JButton brushColor1 = KUI.setupColorButton("Main", new HasColor() {
			@Override
			public Color getColor() {
				return imagePanelInterface.getColor1();
			}
			@Override
			public void setColor(Color color) {
				imagePanelInterface.setColor1(color);
			}
		});

		JButton brushColor2 = KUI.setupColorButton("Shift", new HasColor() {
			@Override
			public Color getColor() {
				return imagePanelInterface.getColor2();
			}
			@Override
			public void setColor(Color color) {
				imagePanelInterface.setColor2(color);
			}
		});

		JPanel fillerPanel = new JPanel();
		fillerPanel.setOpaque(false);

		// ############ ROW 0 ################## 
		c.gridx = 0; c.gridy = 0;
		this.add(openFile, c);
		c.gridx = 1; c.gridy = 0;
		this.add(saveFile, c);
		c.gridx = 2; c.gridy = 0;
		this.add(newFile, c);
		
		

		// ############ ROW 1 ################## 
		c.gridx = 0; c.gridy = 1;
		this.add(undoButton, c);
		c.gridx = 1; c.gridy = 1;
		this.add(redoButton, c);
		
		// ############ ROW 2 ################## 
		c.gridx = 0; c.gridy = 2;
		this.add(modeButtons.get(BrushMode.SELECT), c);
		c.gridx = 1; c.gridy = 2;
		this.add(applyButton, c);
		c.gridx = 2; c.gridy = 2;
		this.add(toggleTiling, c);
		
		// ############ ROW 3 ################## 
		c.gridx = 0; c.gridy = 3;
		this.add(modeButtons.get(BrushMode.MOVE), c);
		c.gridx = 1; c.gridy = 3;
		this.add(modeButtons.get(BrushMode.BRUSH), c);
		c.gridx = 2; c.gridy = 3;
		this.add(modeButtons.get(BrushMode.FILL), c);
		c.gridx = 3; c.gridy = 3;
		this.add(modeButtons.get(BrushMode.COLOR_SELECT), c);

		// ############ ROW 4 ################## 
		c.gridx = 0; c.gridy = 4; c.gridwidth = 5;
		this.add(brushSize, c);
		c.gridwidth = 1;
		

		// ############ ROW 5 ################## 
		c.gridx = 0; c.gridy = 5; c.gridwidth = 2;
		this.add(brushColor1, c);
		c.gridx = 2; c.gridy = 5; c.gridwidth = 2;
		this.add(brushColor2, c);
		c.gridwidth = 1;
		c.gridx = 4; c.gridy = 5;
		this.add(modeButtons.get(BrushMode.COLOR_PICKER), c);


		c.gridx = 0; c.gridy = 6; c.gridwidth = 4;
		this.add(new JSeparator(), c);
		
		// ############ FILLER ################## 
		c.gridx = 0; c.gridy = 7; c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		this.add(fillerPanel, c);
		
		this.validate();
	}
}
