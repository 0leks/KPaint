package ok.kui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

import ok.*;
import ok.kpaint.*;

public class KBrushColorButton extends JButton {

	private HasColor hasColor;
	private Image backgroundImage;

	public KBrushColorButton(String name, HasColor hasColor, Image backgroundImage) {
		super(name);
		this.hasColor = hasColor;
		this.backgroundImage = backgroundImage;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
		g.setColor(hasColor.getColor());
		g.fillRect(0, 0, getWidth(), getHeight());
		setForeground(Color.white);
		g.setFont(KPaintDriver.MAIN_FONT_BIG);
		setForeground(Utils.getBestTextColor(hasColor.getColor()));
		super.paintComponent(g);
	}

	public static JButton setupColorButton(String text, HasColor c) {
		int width = 80;
		int height = 40;
		Image background = Utils.resizeImageIcon(
				Utils.loadImageIconResource("resources/transparentBackground.png"), width, height).getImage();
		JButton chooseColorButton = new KBrushColorButton(text, c, background);
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
}
