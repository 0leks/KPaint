package ok.kui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class KRadioButton extends JRadioButton {
	
	public static final Color DEFAULT_COLOR = new Color(240, 240, 240);
	public static final Color HOVERED_COLOR = new Color(220, 220, 220);
	public static final Color PRESSED_COLOR = new Color(200, 200, 200);
	public static final Color SELECTED_COLOR = new Color(190, 190, 220);
	
	private boolean hovered = false;
	private boolean pressed = false;
	
	private Image backgroundImage;
	
	public KRadioButton(String name) {
		super(name);
		addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				hovered = true;
			}
			@Override
			public void mouseExited(MouseEvent e) {
				hovered = false;
			}
			@Override
			public void mousePressed(MouseEvent e) {
				pressed = true;
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				pressed = false;
			}
		});
	}
	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
		if(isSelected()) {
			setBackground(SELECTED_COLOR);
		}
		else if(pressed) {
			setBackground(PRESSED_COLOR);
		}
		else if(hovered) {
			setBackground(HOVERED_COLOR);
		}
		else {
			setBackground(DEFAULT_COLOR);
		}
		super.paintComponent(g);
	}
}
