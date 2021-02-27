package ok.kpaint;

import java.awt.*;

/**
 * Used for actions that occur on the image panel that need to have effect on the GUI
 */
public interface GUIInterface {

	public void finishedSelection();
	public void changedColor(Color newColor);
	
	public void changeModeHotkey(BrushMode mode);
	public void switchLayout(boolean withTitles);
}
