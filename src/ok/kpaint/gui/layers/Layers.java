package ok.kpaint.gui.layers;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import ok.kpaint.*;

public class Layers {
	private static final Layer DUMMY = new Layer(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)) {
		public boolean shown() {return false;};
	};
	
	private LinkedList<LayersListener> listeners = new LinkedList<>();
	private ArrayList<Layer> layers = new ArrayList<>();
	private Layer active = DUMMY;
	
	public Layers() {
	}
	
	public BufferedImage compose() {
		Rectangle bounds = getBoundingRect();
		BufferedImage composed = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = composed.createGraphics();
		for(Layer layer : layers) {
			if(layer.shown()) {
				g.drawImage(layer.image(), -bounds.x + layer.x(), -bounds.y + layer.y(), null);
			}
		}
		g.dispose();
		return composed;
	}
	
	public Layer active() {
		return active;
	}
	
	public Rectangle getBoundingRect() {
		if(layers.isEmpty()) {
			return new Rectangle(0, 0, 1, 1);
		}
		Vec2i min = new Vec2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
		Vec2i max = new Vec2i(Integer.MIN_VALUE, Integer.MIN_VALUE);
		Vec2i overallmin = new Vec2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
		Vec2i overallmax = new Vec2i(Integer.MIN_VALUE, Integer.MIN_VALUE);
		boolean atleastone = false;
		for(Layer layer : layers) {
			if(layer.shown()) {
				min.x = Math.min(min.x, layer.x());
				min.y = Math.min(min.y, layer.y());
				max.x = Math.max(max.x, layer.x() + layer.w());
				max.y = Math.max(max.y, layer.y() + layer.h());
				atleastone = true;
			}
			overallmin.x = Math.min(overallmin.x, layer.x());
			overallmin.y = Math.min(overallmin.y, layer.y());
			overallmax.x = Math.max(overallmax.x, layer.x() + layer.w());
			overallmax.y = Math.max(overallmax.y, layer.y() + layer.h());
		}
		if(atleastone) {
			return new Rectangle(min.x, min.y, max.x-min.x, max.y-min.y);
		}
		else {
			return new Rectangle(overallmin.x, overallmin.y, overallmax.x-overallmin.x, overallmax.y-overallmin.y);
		}
	}
	
	public void draw(Vec2i pixel, Brush brush) {
		if(active == DUMMY) {
			return;
		}
		active.draw(pixel, brush);
	}
	
	private void centerLayer(Layer layer) {
		Rectangle bounds = getBoundingRect();
		layer.centerAround(new Vec2i((int)bounds.getCenterX(), (int)bounds.getCenterY()));
	}
	public void add(BufferedImage image) {
		Layer newLayer = new Layer(image);
		centerLayer(newLayer);
		if(active != DUMMY) {
			add(newLayer, layers.indexOf(active) + 1);
		}
		else {
			add(newLayer, layers.size());
		}
	}
	public void add() {
		if(active != DUMMY) {
			add(layers.indexOf(active) + 1);
		}
		else {
			add(layers.size());
		}
	}
	public void add(int index) {
		Layer newLayer = new Layer();
		centerLayer(newLayer);
		add(newLayer, index);
	}

	private void add(Layer layer, int index) {
		if(index <= layers.size()) {
			layers.add(index, layer);
			setActive(layer);
			notifyListeners();
		}
	}
	
	public void extract(Rectangle extraction, Color altColor) {
		Rectangle bounds = getBoundingRect();
		BufferedImage image = compose();
		BufferedImage extracted = image.getSubimage(extraction.x - bounds.x, extraction.y - bounds.y, extraction.width, extraction.height);
		
		for(Layer layer : layers) {
			if(layer.shown()) {
				layer.erase(extraction, altColor);
			}
		}
		
		add(extracted);
		active().setPosition(new Vec2i(extraction.x, extraction.y));
	}
	
	public void deleteAll() {
		layers.clear();
		active = DUMMY;
		notifyListeners();
	}
	public void delete(Layer layer) {
		int index = layers.indexOf(layer);
		if(index == -1) {
			return;
		}
		layers.remove(layer);
		if(layer == active) {
			if(!layers.isEmpty()) {
				if(index >= layers.size()) {
					index--;
				}
				active = layers.get(index);
			}
			else {
				active = DUMMY;
			}
		}
		notifyListeners();
	}
	public void applyLayer(Layer layer) {
		int index = layers.indexOf(layer);
		if(index > 0) {
			Layer applyon = layers.get(index - 1);
			Rectangle before = applyon.bounds();
			Rectangle extra = layer.bounds();
			Rectangle newbounds = before.union(extra);
			// TODO figure out how to use correct altColor for this
			applyon.resize(newbounds, new Color(0, 0, 0, 0));
			Graphics2D g = applyon.image().createGraphics();
			g.translate(layer.x() - applyon.x(), layer.y() - applyon.y());
			g.drawImage(layer.image(), 0, 0, null);
			g.dispose();
			
			delete(layer);
		}
	}
	
	public void toggleShown(Layer layer) {
		if(layer != DUMMY) {
			layer.toggleShown();
		}
		notifyListeners();
	}
	
	public void setActive(Layer layer) {
		active = layer;
		notifyListeners();
	}
	
	public void move(Layer layer, int direction) {
		for(int index = 0; index < layers.size(); index++) {
			if(layers.get(index) == layer) {
				if(index + direction >= 0 && index + direction < layers.size()) {
					Layer temp = layers.get(index + direction);
					layers.set(index + direction, layer);
					layers.set(index, temp);
					notifyListeners();
				}
				return;
			}
		}
	}
	
	public void addListener(LayersListener listener) {
		listeners.add(listener);
		listener.update();
	}
	private void notifyListeners() {
		for(LayersListener listener : listeners) {
			listener.update();
		}
	}

	
	public ArrayList<Layer> getLayers() {
		return layers;
	}
}
