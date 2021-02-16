package ok.kpaint;

import java.awt.*;

public interface ImagePanelInterface {

	public void undo();
	public void redo();
	public void resetView();
	public void applySelection();
	public void clearSelection();
	public void pasteFromClipboard();

	public void showTiling(boolean enabled);

	public void setColor1(Color color1);
	public void setColor2(Color color2);

	public void newCanvas();
}
