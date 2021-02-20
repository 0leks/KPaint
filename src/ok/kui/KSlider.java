package ok.kui;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class KSlider extends JPanel {
	
	private static final Font labelFont = new Font("Comic Sans MS", Font.PLAIN, 10);
	
	private List<ChangeListener> listeners = new ArrayList<>();
	
	private JTextField text;
	private JPanel slider;
	private int value;
	
	private int min;
	private int currentMax;
	public KSlider(int min, int initialMax) {
		this.min = min;
		this.currentMax = initialMax;
		this.setMinimumSize(new Dimension(20, 20));
		this.setPreferredSize(new Dimension(150, 40));
		
		text = new JTextField(5);
		text.setFocusable(true);
		text.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				changed();
			}
			public void removeUpdate(DocumentEvent e) {
				changed();
			}
			public void insertUpdate(DocumentEvent e) {
				changed();
			}
			private void changed() {
				try {
					int targetVal = Integer.parseInt(text.getText());
					if(targetVal != value) {
						setValue(targetVal, true);
						rescale();
					}
				}
				catch(NumberFormatException e) {
					
				}
			}
		});
		text.setText(value + "");
		slider = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth()-1, getHeight()-1);
				g.setColor(Color.black);
				g.drawRect(0, 0, getWidth()-1, getHeight()-1);
				
				int tickHeight = getHeight() / 8;
				int xpos = getXPixelFromValue(value, getWidth());
				g.drawLine(xpos, 0, xpos, getHeight()-1);
				g.fillRect(0, tickHeight + 2, xpos, getHeight() - 1 - 2 * (tickHeight + 2));
				
				g.setFont(labelFont);
				g.setColor(Color.black);
				for(int i = 1; i < 10; i++) {
					int v = min + (currentMax - min) * i / 10;
					int x = getXPixelFromValue(v, getWidth());
					int height = tickHeight;
					if(i % 2 == 0) {
						height = tickHeight*2;
					}
					g.drawLine(x, 0, x, height);
					g.drawLine(x, getHeight() - height - 1, x, getHeight()-1);
				}
				
				String trueLabel = value + "";
				int trueLabelWidth = g.getFontMetrics().stringWidth(trueLabel);
				int trueX = getXPixelFromValue(value, getWidth());
				Color trueLabelColor;
				if(trueX - trueLabelWidth < 2) {
					trueX += 1;
					trueLabelColor = Color.black;
				}
				else {
					trueX -= trueLabelWidth - 1;
					trueLabelColor = Color.white;
				}
				
				for(int i = 1; i < 5; i++) {
					int v = min + (currentMax - min) * i / 5;
					String label = v + "";
					int potentialStringWidth = g.getFontMetrics().stringWidth(label);
					int potentialX = getXPixelFromValue(v, getWidth()) - potentialStringWidth/2;
					if((potentialX >= trueX && potentialX <= trueX + trueLabelWidth)
							|| (potentialX + potentialStringWidth >= trueX && potentialX + potentialStringWidth <= trueX + trueLabelWidth)) {
						continue;
					}
					g.setColor(Color.black);
					if(v <= value) {
						g.setColor(Color.white);
					}
					g.drawString(label, potentialX, (getHeight() + labelFont.getSize())/2);
				}
				g.setColor(trueLabelColor);
				g.drawString(trueLabel, trueX, (getHeight() + labelFont.getSize())/2);
			}
		};
		slider.setFocusable(false);
		slider.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mouseAction(e.getX(), slider.getWidth());
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				mouseAction(e.getX(), slider.getWidth());
				rescale();
			}
		});
		slider.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				mouseAction(e.getX(), slider.getWidth());
			}
		});
		this.setLayout(new BorderLayout());
//		this.add(text, BorderLayout.EAST);
		this.add(slider, BorderLayout.CENTER);
		
		setValue(1);
	}
	
	private void rescale() {
		double ratio = (1.0 * value - min + 1) / (currentMax - min + 1);
		int newMax = currentMax;
		if(ratio >= 0.8 || ratio <= 0.2) {
			newMax = 2*value - min + 1;
		}
		if(newMax > min && newMax != currentMax) {
			currentMax = newMax;
			repaint();
		}
	}
	
	private void mouseAction(int x, int width) {
		int targetVal = getValueFromPixel(x, width);
		setValue(targetVal);
	}
	
	private int getXPixelFromValue(int v, int width) {
		return width * (v - min + 1) / (currentMax - min + 1);
	}
	private int getValueFromPixel(int x, int width) {
		return (int)Math.ceil(1.0 * x * (currentMax - min + 1) / width) + min - 1;
	}
	
	private void setValue(int value, boolean fromTextListener) {
		if(value < min || value == this.value) {
			return;
		}
		this.value = value;
		ChangeEvent e = new ChangeEvent(this);
		for(ChangeListener l : listeners) {
			l.stateChanged(e);
		}
		if(!fromTextListener) {
			text.setText(value + "");
		}
		repaint();
	}
	
	
	public void setValue(int value) {
		setValue(value, false);
	}
	public int getValue() {
		return value;
	}
	
	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}
	
}
