package ok.kpaint.history;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

import ok.kpaint.*;
import ok.kpaint.gui.layers.*;

public class ImageEditRecorder {
	
	private static Layer layer;
	private static BufferedImage image;
	private static Point lowerBound;
	private static Point upperBound;

	// TODO change this to ints for upper and lower bounds to avoid making objects
	public static void drawing(Layer newlayer, Point p) {
		drawing(newlayer, p, p);
	}
	public static void drawing(Layer newlayer, Point newLower, Point newUpper) {
		if(newUpper.x < newLower.x || newUpper.y < newLower.y) {
			return;
		}
		if(image == null) {
			layer = newlayer;
			image = Utils.copyImage(newlayer.image());
			lowerBound = new Point(newLower);
			upperBound = new Point(newUpper);
		}
		lowerBound.x = Math.min(lowerBound.x, newLower.x);
		lowerBound.y = Math.min(lowerBound.y, newLower.y);
		upperBound.x = Math.max(upperBound.x, newUpper.x);
		upperBound.y = Math.max(upperBound.y, newUpper.y);
	}
	public static Edit makeImageEdit(Layer layer, ImageSegment segment) {
		return new Edit(() -> {
				int[] applyrgbs = segment.image.getRGB(0, 0, 
				                                       segment.image.getWidth(), 
				                                       segment.image.getHeight(), 
				                                       null, 0, 
				                                       segment.image.getWidth());
				layer.image().setRGB(segment.offset.x, 
				                    segment.offset.y, 
				                    segment.image.getWidth(), 
				                    segment.image.getHeight(), 
				                    applyrgbs, 0, 
				                    segment.image.getWidth());
			},
            () -> {
				ImageSegment replacement = ImageSegment.makeSegment(layer.image(), 
				                                                    segment.offset.x, 
				  		                                          	segment.offset.y, 
				  		                                          	segment.image.getWidth(), 
				  		                                          	segment.image.getHeight());
				return makeImageEdit(layer, replacement);
			});
	}
	public static void finishedDrawing() {
		if(image != null) {
			ImageSegment segment = ImageSegment.makeSegment(image, 
			                                                lowerBound.x, lowerBound.y, 
			          			                          	upperBound.x - lowerBound.x + 1, 
			          			                          	upperBound.y - lowerBound.y + 1);
			History.push(makeImageEdit(layer, segment));
		}
		layer = null;
		image = null;
		lowerBound = null;
		upperBound = null;
	}
}
