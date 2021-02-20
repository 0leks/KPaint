package ok.kpaint;

public interface GUIInterface {

	public void finishedSelection();
	public void changedColor();
	
	public void changeModeHotkey(BrushMode mode);
	public void switchLayout(boolean withTitles);
}
