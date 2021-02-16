package ok.kpaint;

import javax.swing.*;

public enum BrushMode {
	MOVE("Move", "resources/move.png"), 
	SELECT("Select", "resources/select.png"), 
	BRUSH("Brush", "resources/brush.png"), 
	FILL("Fill", "resources/fill.png"), 
	COLOR_SELECT("Matching Color", "resources/color.png"),
	COLOR_PICKER("Color Picker", "resources/color_picker.png"),
	;
	private String name;
	private ImageIcon image;
	BrushMode(String name, String imageLocation) {
		this.name = name;
		this.image = Utils.resizeImageIcon(Utils.loadImageIconResource(imageLocation), 32, 32);
	}
	public ImageIcon getImageIcon() {
		return image;
	}
	@Override
	public String toString() {
		return name;
	}
}
