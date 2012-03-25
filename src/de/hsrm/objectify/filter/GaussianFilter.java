package de.hsrm.objectify.filter;

import android.graphics.Bitmap;
import de.hsrm.objectify.utils.Image;

/**
 * This class apllies a gaussian blur to an image.
 * 
 * @author kwolf001
 * 
 */
public class GaussianFilter {

	private float radius;
	private Kernel kernel;

	/**
	 * Constructs a gaussian filter.
	 */
	public GaussianFilter(float radius) {
		setRadius(radius);
	}

	/**
	 * Set the radius of the kernel. The size of the radius influences the
	 * Blurriness of the image and affects the computing time. The bigger the
	 * radius the longer it will take.
	 * 
	 * @param radius
	 *            the radius of the kernel.
	 */
	public void setRadius(float radius) {
		this.radius = radius;
		kernel = makeKernel(radius);
	}

	/**
	 * Creates a gaussian blur kernel.
	 * 
	 * @param radius
	 *            radius of the kernel.
	 * @return new kernel.
	 */
	private Kernel makeKernel(float radius) {
		int r = (int) Math.ceil(radius);
		int rows = r * 2 + 1;
		float[] matrix = new float[rows];
		float sigma = radius / 3;
		float sigma22 = 2 * sigma * sigma;
		float sigmaPi2 = (float) (2 * Math.PI * sigma);
		float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
		float radius2 = radius * radius;
		float total = 0;
		int index = 0;
		for (int row = -r; row <= r; row++) {
			float distance = row * row;
			if (distance > radius2)
				matrix[index] = 0;
			else
				matrix[index] = (float) Math.exp(-(distance) / sigma22)
						/ sqrtSigmaPi2;
			total += matrix[index];
			index++;
		}
		for (int i = 0; i < rows; i++)
			matrix[i] /= total;

		return new Kernel(rows, 1, matrix);
	}

	/**
	 * Get the radius of the kernel.
	 * 
	 * @return the radius of the kernel.
	 */
	public float getRadius() {
		return radius;
	}

	/**
	 * Blurs the src image and returns a newly created blured image.
	 * 
	 * @param src
	 *            the source image which should be blured.
	 * @return a blured version of the source image.
	 */
	public Image filter(Image src) {
		int width = src.getWidth();
		int height = src.getHeight();

		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		src.getPixels(inPixels, 0, src.getWidth(), 0, 0, src.getWidth(),
				src.getHeight());

		convolveAndTranspose(kernel, inPixels, outPixels, width, height);
		convolveAndTranspose(kernel, outPixels, inPixels, height, width);
		Image dst = new Image(Bitmap.createBitmap(inPixels, width, height,
				src.getConfig()));
		return dst;
	}

	private void convolveAndTranspose(Kernel kernel, int[] inPixels,
			int[] outPixels, int width, int height) {
		float[] matrix = kernel.getKernelData(null);
		int cols = kernel.getWidth();
		int cols2 = cols / 2;

		for (int y = 0; y < height; y++) {
			int index = y;
			int ioffset = y * width;
			for (int x = 0; x < width; x++) {
				float r = 0, g = 0, b = 0, a = 0;
				int moffset = cols2;
				for (int col = -cols2; col <= cols2; col++) {
					float f = matrix[moffset + col];
					if (f != 0) {
						int ix = x + col;
						if (ix < 0) {
							ix = 0;
						} else if (ix >= width) {
							ix = width - 1;
						}
						int rgb = inPixels[ioffset + ix];
						a += f * ((rgb >> 24) & 0xff);
						r += f * ((rgb >> 16) & 0xff);
						g += f * ((rgb >> 8) & 0xff);
						b += f * (rgb & 0xff);
					}
				}
				int ia = clamp((int) (a + 0.5));
				int ir = clamp((int) (r + 0.5));
				int ig = clamp((int) (g + 0.5));
				int ib = clamp((int) (b + 0.5));
				outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
				index += height;

			}
		}
	}

	/**
	 * Clamp a value to the range 0..255
	 */
	public static int clamp(int c) {
		if (c < 0)
			return 0;
		if (c > 255)
			return 255;
		return c;
	}
}
