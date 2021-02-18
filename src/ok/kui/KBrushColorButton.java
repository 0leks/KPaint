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
		g.setFont(DriverKPaint.MAIN_FONT_BIG);
		setForeground(Utils.getBestTextColor(hasColor.getColor()));
		super.paintComponent(g);
	}
}
