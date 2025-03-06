package ok.kpaint;

public class Vec4i {
	public int x;
	public int y;
	public int w;
	public int h;
	public Vec4i() {
		this(0, 0, 0, 0);
	}
	public Vec4i(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	public Vec4i(Vec4i v) {
		this(v.x, v.y, v.w, v.h);
	}
	
	public boolean contains(Vec2i point) {
		return (point.x >= x && point.x < x + w && point.y >= y && point.y < y + h);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Vec4i) {
			Vec4i pixel = (Vec4i)other;
			return this.x == pixel.x && this.y == pixel.y && this.w == pixel.w && this.h == pixel.h;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	@Override
	public String toString() {
		return x + "," + y + "," + w + "," + h;
	}
}
