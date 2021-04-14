package ok.kpaint;

import java.awt.image.*;
import java.util.*;

public class History {

	private static final int MAX_HISTORY_SIZE = 40;
	private int historyCursor = 0;
	private LinkedList<BufferedImage> history = new LinkedList<>();
	private boolean modified;
	
	private BufferedImage current;
	
	
	public int getCursor() {
		return historyCursor;
	}
	
	public LinkedList<BufferedImage> getHistory() {
		return history;
	}
	
	public BufferedImage getCurrent() {
		return current;
	}
	
	public void setCurrentImage(BufferedImage newcurrent) {
		current = newcurrent;
	}
	public void setInitialImage(BufferedImage initial) {
		current = initial;
		modified();
		pushVersion();
	}
	
	public void modified() {
		if(!modified) {
			current = Utils.copyImage(current);
		}
		modified = true;
	}

	public void pushVersion() {
		resetVersionHead();
		history.addFirst(current);
		modified = false;
		if(history.size() > MAX_HISTORY_SIZE) {
			history.removeLast();
		}
	}
	public void rewindVersion() {
		if(historyCursor == 0 && modified) {
			pushVersion();
		}
		if(historyCursor < history.size() - 1) {
			historyCursor++;
			current = history.get(historyCursor);
			modified = false;
		}
	}
	public void upwindVersion() {
		if(historyCursor > 0) {
			historyCursor--;
			current = history.get(historyCursor);
			modified = false;
		}
	}
	public void resetVersionHead() {
		while(historyCursor > 0) {
			history.removeFirst();
			historyCursor--;
		}
	}
}
