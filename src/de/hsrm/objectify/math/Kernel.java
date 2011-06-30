package de.hsrm.objectify.math;

/**
 * The Kernel class provides a matrix. This matrix is stored as a float array
 * which describes how a specified pixel affects the value calculated for the
 * pixel's position in the output image of a filtering operation. The X, Y
 * origins indicate the kernel matrix element which corresponds to the pixel
 * position for which an output value is being calculated.
 * 
 * @author kwolf001
 */
public class Kernel implements Cloneable {

	private final int xOrigin;
	private final int yOrigin;
	private int width;
	private int height;
	float data[];
	
	/**
	 * Instantiates a new Kernel with the specified float array. The
	 * width*height elements of the data array are copied.
	 * 
	 * @param width
	 *            the width of the Kernel.
	 * @param height
	 *            the height of the Kernel.
	 * @param data
	 *            the data of Kernel.
	 */
	public Kernel(int width, int height, float[] data) {
		int dataLength = width * height;
		if (data.length < dataLength) {
			throw new IllegalArgumentException("Length of data should not be less than width*height");
		}
		
		this.width = width;
		this.height = height;
		
		this.data = new float[dataLength];
		System.arraycopy(data, 0, this.data, 0, dataLength);
		
		xOrigin = (width - 1) / 2;
		yOrigin = (height - 1) / 2;
	}
	
	/**
	 * Gets the width of this Kernel.
	 * @return the width of this Kernel.
	 */
	public final int getWidth() {
		return width;
	}
	
	/**
	 * Gets the height of this Kernel.
	 * @return the height of this Kernel.
	 */
	public final int getHeight() {
		return height;
	}
	
	/**
	 * Gets the float data of this Kernel.
	 * @param data the float array where the resulted data will be stored.
	 * @return the float data array of this Kernel.
	 */
	public final float[] getKernelData(float[] data) {
		if (data == null) {
			data = new float[this.data.length];
		}
		System.arraycopy(this.data, 0, data, 0, this.data.length);
		
		return data;
	}
	
	/**
	 * Geths the X origin of this Kernel.
	 * @return the X origin of this Kernel.
	 */
	public final int getXOrigin() {
		return xOrigin;
	}
	
	/**
	 * Gets the Y origin of this Kernel.
	 * @return the Y origin of this Kernel.
	 */
	public final int getYOrigin() {
		return yOrigin;
	}
	
	/**
	 * Returns a copy of this Kernel object.
	 * 
	 * @return the copy of this Kernel object.
	 */ 
	public Object clone() {
		return new Kernel(width, height, data);
	}
}
