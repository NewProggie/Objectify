package de.hsrm.objectify.camera;

/**
 * Simple class for maintaining preview sizes
 * 
 * @author kwolf001
 * 
 */
public class Size {
	
	private int width;
	private int height;
	
	public Size() {
		this.width = 0;
		this.height = 0;
	}
	
	public Size(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	@Override
	public String toString() {
		return String.valueOf(width) + "x" + String.valueOf(height);
	}

}
