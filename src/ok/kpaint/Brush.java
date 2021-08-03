package ok.kpaint;

public class Brush {
	
	private int brushSize;
	private BrushShape shape;
	private BrushMode brushMode;
	
	public Brush(int brushSize, BrushShape shape, BrushMode mode) {
		this.brushSize = brushSize;
		this.shape = shape;
		this.brushMode = mode;
	}

	public BrushShape getShape() {
		return shape;
	}

	public void setShape(BrushShape shape) {
		this.shape = shape;
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
