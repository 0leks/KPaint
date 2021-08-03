package ok.kpaint;

import java.awt.*;
import java.awt.image.*;

public class ImageSegment {
	public BufferedImage image;
	public Point offset;
	private ImageSegment(BufferedImage image, Point offset) {
		this.image = image;
		this.offset = offset;
	}
	
	public Rectangle bounds() {
		return new Rectangle(offset.x, offset.y, image.getWidth(), image.getHeight());
	}
	
	public static ImageSegment makeSegment(BufferedImage image, int x, int y, int w, int h) {
		return new ImageSegment(Utils.copyImage(image.getSubimage(x, y, w, h)), new Point(x, y));
	}
	public static ImageSegment makeSegment(BufferedImage image, Rectangle bounds) {
		return makeSegment(image, bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
}
