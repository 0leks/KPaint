package ok.kui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;

import ok.kpaint.*;

public class ColorSwatches extends JPanel {
	private Image background = Utils.resizeImageIcon(Utils.loadImageIconResource("/transparentBackground.png"), 32, 32).getImage();
	class ColorButton extends JButton {
		public ColorButton() {
			this.addActionListener(e -> {
				if((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
					imagepanelinterface.setAltColor(c);
				}
				else {
					imagepanelinterface.setMainColor(c);
				}
			});
		}
		private Color c = Color.black;
		public void setColor(Color c) {
			this.c = c;
			ColorButton.this.repaint();
		}
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(background, 0, 0, null);
			g.setColor(c);
			g.fillRect(0, 0, getWidth()-1, getHeight()-1);
			g.setColor(Color.black);
			g.drawRect(0, 0, getWidth()-1, getHeight()-1);
		}
	}

	private ArrayList<Color> recentColors;
	private ArrayList<ColorButton> colorButtons;
	private ImagePanelInterface imagepanelinterface;
	public ColorSwatches(ImagePanelInterface imagepanelinterface) {
		this.imagepanelinterface = imagepanelinterface;
		recentColors = new ArrayList<>();
		colorButtons = new ArrayList<>();
		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		c.weightx = 1;
		c.weighty = 1;
		Color[] defaultColors = new Color[] {
				Color.red,
				Color.green,
				Color.blue,
				Color.cyan,
				Color.magenta,
				Color.yellow,
				Color.black,
				Color.gray,
				Color.white,
				new Color(0, 0, 0, 0)
		};
		for(int row = 0; row < 2; row++) {
			c.gridy = row;
			for(int col = 0; col < 5; col++) {
				ColorButton b = new ColorButton();
//				b.setColor(defaultColors[row*5 + col]);
				b.setFocusable(false);
				colorButtons.add(b);
				c.gridx = col;
				this.add(b, c);
			}
		}
		for(Color color : defaultColors) {
			choseColor(color);
		}
	}
	
	public void choseColor(Color newColor) {
		for(Color c : recentColors) {
			if(c.getRGB() == newColor.getRGB()) {
				return;
			}
		}
		recentColors.add(newColor);
		updateButtons();
	}
	private void updateButtons() {
		while(recentColors.size() > colorButtons.size()) {
			recentColors.remove(0);
		}
		for(int i = 0; i < recentColors.size(); i++) {
			colorButtons.get(i).setColor(recentColors.get(i));
		}
		revalidate();
		repaint();
	}
	
}
