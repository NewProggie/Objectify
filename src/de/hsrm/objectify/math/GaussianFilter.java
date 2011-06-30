package de.hsrm.objectify.math;

import android.graphics.Bitmap;

public class GaussianFilter {

	private float radius;
	private Kernel kernel;
	public static int ZERO_EDGES = 0;
	public static int CLAMP_EDGES = 1;
	public static int WRAP_EDGES = 2;
	public boolean alpha = true;
	
	/** 
	 * Construct a Gaussian Filter
	 */
	public GaussianFilter() {
		this.radius = 2;
	}
	
	/**
	 * Construct a Gaussian Filter
	 * @param radius blur radius in pixels
	 */
	public GaussianFilter(float radius) {
		setRadius(radius);
	}
	
	/**
	 * Set the radius of the kernel, and hence the amount of blur. The bigger the radius, the longer this filter will take.
	 * @param radius the radius of the blur in pixels.
	 */
	public void setRadius(float radius) {
		this.radius = radius;
		kernel = makeKernel(radius);
	}
	
	/**
	 * Get the radius of the kernel.
	 * @return the radius
	 */
	public float getRadius() {
		return radius;
	}
	
	public Bitmap filter( Bitmap src, Bitmap dst) {
		int width = src.getWidth();
		int height = src.getHeight();
		
		if (dst == null) {
			dst = Bitmap.createBitmap(width, height, src.getConfig());
		}
		
		int[] inPixels = new int[width*height];
		int[] outPixels = new int[width*height];
		src.getPixels(inPixels, 0, width, 0, 0, width, height);
		
		convolveAndTranspose(kernel, inPixels, outPixels, width, height, alpha, CLAMP_EDGES);
		convolveAndTranspose(kernel, outPixels, inPixels, height, width, alpha, CLAMP_EDGES);
		
		dst.setPixels(inPixels, 0, width, 0, 0, width, height);
		return dst;
	}
	
	public static void convolveAndTranspose(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
		float[] matrix = kernel.getKernelData(null);
		int cols = kernel.getWidth();
		int cols2 = cols/2;
		
		for (int y = 0; y < height; y++) {
			int index = y;
			int ioffset = y*width;
			for (int x = 0; x < width; x++) {
				float r = 0, g = 0, b = 0, a = 0;
				int moffset = cols2;
				for (int col = -cols2; col <= cols2; col++) {
					float f = matrix[moffset+col];
					
					if (f != 0) {
						int ix = x+col;
						if (ix < 0) {
							if (edgeAction == CLAMP_EDGES) {
								ix = 0;
							} else if (edgeAction == WRAP_EDGES) {
								ix = (x+width) % width;
							} 
						} else if (ix >= width) {
							if (edgeAction == CLAMP_EDGES) {
								ix = width-1;
							} else if (edgeAction == WRAP_EDGES) {
								ix = (x+width) % width;
							}
						}
						int rgb = inPixels[ioffset+ix];
						a += f * ((rgb >> 24) & 0xff);
						r += f * ((rgb >> 16) & 0xff);
						g += f * ((rgb >> 8) & 0xff);
						b += f * (rgb & 0xff);
					}
				}
				int ia = alpha ? clamp((int)(a+0.5)) : 0xff;
				int ir = clamp((int)(r+0.5));
				int ig = clamp((int)(g+0.5));
				int ib = clamp((int)(b+0.5));
				outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
				index += height;
			}
		}
	}
	
	/**
	 * Make a Gaussian blur kernel.
	 */
	public static Kernel makeKernel(float radius) {
		int r = (int) Math.ceil(radius);
		int rows = r*2+1;
		float[] matrix = new float[rows];
		float sigma = radius/3;
		float sigma22 = 2*sigma*sigma;
		float sigmaPi2 = (float) (2*Math.PI*sigma);
		float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
		float radius2 = radius*radius;
		float total = 0;
		int index = 0;
		for (int row = -r; row <= r; row++) {
			float distance = row*row;
			if (distance > radius2) {
				matrix[index] = 0;
			} else {
				matrix[index] = (float) Math.exp(-(distance)/sigma22) / sqrtSigmaPi2;
			}
			total += matrix[index];
			index++;
		}
		for (int i=0; i < rows; i++) {
			matrix[i] /= total;
		}
		
		return new Kernel(rows, 1, matrix);
	}
	
	private static int clamp(int c) {
		if (c < 0) {
			return 0;
		} else if (c > 255) {
			return 255;
		}
		return c;
	}
}
