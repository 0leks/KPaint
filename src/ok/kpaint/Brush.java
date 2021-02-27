package ok.kpaint;

public class Brush {
	
	private int brushSize;
	private BrushMode brushMode;
	
	public Brush(int brushSize, BrushMode mode) {
		this.brushSize = brushSize;
		this.brushMode = mode;
	}

	public int getBrushSize() {
		return brushSize;
	}

	public void setBrushSize(int brushSize) {
		this.brushSize = brushSize;
	}

	public BrushMode getMode() {
		return brushMode;
	}

	public void setMode(BrushMode brushMode) {
		this.brushMode = brushMode;
	}
	
	
	
	
}
