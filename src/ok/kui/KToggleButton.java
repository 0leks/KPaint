package ok.kui;

import java.awt.*;

import javax.swing.*;

public class KToggleButton extends JToggleButton {
	
	public KToggleButton(String text) {
		super(text);
		this.setOpaque(false);
		this.setContentAreaFilled(false);
	}

	@Override
	public void paintComponent(Graphics g) {
		if(isSelected()) {
			setBackground(KButton.SELECTED_COLOR);
		}
//		else if(pressed) {
//			setBackground(KButton.PRESSED_COLOR);
//		}
//		else if(hovered) {
//			setBackground(KButton.HOVERED_COLOR);
//		}
		else {
			setBackground(KButton.DEFAULT_COLOR);
		}
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		// TODO this paints over the whole button, including the background
		super.paintComponent(g);
	}

}
