package de.hsrm.objectify.utils;

import java.io.ByteArrayOutputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import de.hsrm.objectify.camera.CameraFinder;
import de.hsrm.objectify.filter.GaussianFilter;

/**
 * Helper class for creating bitmaps, considering different picture sizes and
 * image formats depending on the android device.
 * 
 * @author kwolf001
 * 
 */
public class BitmapUtils {

	private static final String TAG = "BitmapUtils";

	/**
	 * Creates a bitmap depending on the specific image format. Can return null.
	 * 
	 * @param data
	 *            Image data provided by the jpeg callback from the camera
	 * @param pictureSize
	 *            Picture size set in {@link CameraFinder}
	 * @param imageFormat
	 *            Image format set in {@link CameraFinder}
	 * @return New bitmap or null
	 */
	public static Bitmap createBitmap(byte[] data, Size pictureSize, int imageFormat) {
		switch (imageFormat) {
		case ImageFormat.JPEG:
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		case ImageFormat.NV21:
			// convert yuv to jpg
			YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, pictureSize.getWidth(),
					pictureSize.getHeight(), null);
			Rect rect = new Rect(0, 0, pictureSize.getWidth(), pictureSize.getHeight());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			yuvImage.compressToJpeg(rect, 100, baos);
			// convert jpg to bmp
			return BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
		case ImageFormat.RGB_565:
			int[] pixels = convertByteArray(data);
			return Bitmap.createBitmap(pixels, pictureSize.getWidth(), pictureSize.getHeight(),
					Config.RGB_565);
		default:
			return null;
		}
	}

	/**
	 * Creates a blured image based on the source image.
	 * 
	 * @param src
	 *            source image.
	 * @return a new blured image based on the source image.
	 */
	public static Image blurBitmap(Image src) {
		GaussianFilter filter = new GaussianFilter(4);
		Image dst = filter.filter(src);
		return dst;
	}

	public static Bitmap createScreenshot(int width, int height, GL10 gl) {
		int b[] = new int[width * height];
		int bt[] = new int[width * height];
		IntBuffer intBuffer = IntBuffer.wrap(b);
		intBuffer.rewind();
		gl.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int pix = b[i * width + j];
				int pb = (pix >> 16) & 0xFF;
				int pr = (pix << 16) & 0x00FF0000;
				int pix1 = (pix & 0xFF00FF00) | pr | pb;
				bt[(height - i - 1) * width + j] = pix1;
			}
		}
		Bitmap screenshot = Bitmap.createBitmap(bt, width, height, Config.ARGB_8888);
		return screenshot;
	}

	/**
	 * Computes a modified automatic contrast with saturated fixed percentage of
	 * given pixels
	 * 
	 * @param image
	 *            image which will be automatic contrast corrected
	 * @param cumulativeHistogramm
	 *            cumulative histogramm of image
	 * @return newly created Image with adjusted contrast.
	 */
	public static Image modAutoContrast(Image image) {
		// ///
		/* get cumulative histogram for each channel */
		int[] hRed = image.getHistogram(0);
		int[] hGreen = image.getHistogram(1);
		int[] hBlue = image.getHistogram(2);
		int[] colorPixels;
		for (int j = 1; j < hRed.length; j++) {
			hRed[j] = hRed[j - 1] + hRed[j];
			hGreen[j] = hGreen[j - 1] + hGreen[j];
			hBlue[j] = hBlue[j - 1] + hBlue[j];
		}
		float s = 0.005f;
		int alowRed = minI(hRed, image.getWidth(), image.getHeight(), s);
		int ahighRed = maxI(hRed, image.getWidth(), image.getHeight(), s);
		int alowGreen = minI(hGreen, image.getWidth(), image.getHeight(), s);
		int ahighGreen = maxI(hGreen, image.getWidth(), image.getHeight(), s);
		int alowBlue = minI(hBlue, image.getWidth(), image.getHeight(), s);
		int ahighBlue = maxI(hBlue, image.getWidth(), image.getHeight(), s);
		int amin = 0;
		int amax = 255;

		colorPixels = image.getPixels();
		for (int i = 0; i < colorPixels.length; i++) {
			int rNew = 0, gNew = 0, bNew = 0;
			int r = Color.red(colorPixels[i]);
			int g = Color.green(colorPixels[i]);
			int b = Color.blue(colorPixels[i]);
			// transfer function for each channel: red
			if (r <= alowRed) {
				rNew = amin;
			} else if (alowRed < r && r < ahighRed) {
				rNew = amin + (r - alowRed) * ((amax - amin) / (ahighRed - alowRed));
			} else if (r >= ahighRed) {
				rNew = amax;
			}
			// green
			if (g <= alowGreen) {
				gNew = amin;
			} else if (alowGreen < g && g < ahighGreen) {
				gNew = amin + (g - alowGreen) * ((amax - amin) / (ahighGreen - alowGreen));
			} else if (g >= ahighGreen) {
				gNew = amax;
			}
			// blue
			if (b <= alowBlue) {
				bNew = amin;
			} else if (alowBlue < b && b < ahighBlue) {
				bNew = amin + (b - alowBlue) * ((amax - amin) / (ahighBlue - alowBlue));
			} else if (b >= ahighBlue) {
				bNew = amax;
			}
			colorPixels[i] = Color.rgb(rNew, gNew, bNew);
		}
		return new Image(Bitmap.createBitmap(colorPixels, image.getWidth(), image.getHeight(),
				image.getConfig()));
	}

	public static Image equalizeHistogram(Image image) {
		int w = image.getWidth();
		int h = image.getHeight();
		int M = w * h;
		int K = 256;
		int[] redPixels = new int[w * h];
		int[] greenPixels = new int[w * h];
		int[] bluePixels = new int[w * h];
		int[] colorPixels = new int[w * h];

		// get colors for each channel
		int idx = 0;
		for (int v = 0; v < h; v++) {
			for (int u = 0; u < w; u++) {
				int color = image.getPixel(u, v);
				redPixels[idx] = Color.red(color);
				greenPixels[idx] = Color.green(color);
				bluePixels[idx] = Color.blue(color);
				idx += 1;
			}
		}

		// get cumulative Histogram
		int[] hRed = image.getHistogram(0);
		int[] hGreen = image.getHistogram(1);
		int[] hBlue = image.getHistogram(2);
		for (int j = 1; j < hRed.length; j++) {
			hRed[j] = hRed[j - 1] + hRed[j];
			hGreen[j] = hGreen[j - 1] + hGreen[j];
			hBlue[j] = hBlue[j - 1] + hBlue[j];
		}

		// equalize the image
		idx = 0;
		for (int v = 0; v < h; v++) {
			for (int u = 0; u < w; u++) {
				int a1 = redPixels[idx];
				int b1 = hRed[a1] * (K - 1) / M;

				int a2 = greenPixels[idx];
				int b2 = hGreen[a2] * (K - 1) / M;

				int a3 = bluePixels[idx];
				int b3 = hBlue[a3] * (K - 1) / M;

				colorPixels[idx] = Color.rgb(b1, b2, b3);
				idx += 1;
			}
		}
		return new Image(Bitmap.createBitmap(colorPixels, w, h, image.getConfig()));
	}

	private static int[] getCumulativeHistogram(Image image) {
		int[] histogram = image.getHistogram();
		int[] cumHistogram = new int[histogram.length];
		for (int i = 0; i < histogram.length; i++) {
			for (int j = 0; j <= i; j++) {
				cumHistogram[i] += histogram[j];
			}
		}
		return cumHistogram;
	}

	/**
	 * Puts given Images onto a stack and returns the average intensity for each
	 * pixel.
	 * 
	 * @param pictureList
	 *            images to put onto stack
	 * @return newly created {@link Image} with average intensity for each pixel
	 */
	public static Image imagesToStack(ArrayList<Image> pictureList) {
		int width = pictureList.get(0).getWidth();
		int height = pictureList.get(0).getHeight();
		Config config = pictureList.get(0).getConfig();
		int[] sumRed = new int[width * height];
		int[] sumGreen = new int[width * height];
		int[] sumBlue = new int[width * height];
		int[] sumColor = new int[width * height];
		int numberOfPictures = pictureList.size();

		for (int i = 0; i < sumRed.length; i++) {
			sumRed[i] = 0;
			sumGreen[i] = 0;
			sumBlue[i] = 0;
		}
		for (Image img : pictureList) {
			int[] current = img.getPixels();
			for (int i = 0; i < current.length; i++) {
				sumRed[i] += Color.red(current[i]);
				sumGreen[i] += Color.green(current[i]);
				sumBlue[i] += Color.blue(current[i]);
			}
		}
		for (int i = 0; i < sumColor.length; i++) {
			int red = clamp((int) (sumRed[i] / numberOfPictures));
			int green = clamp((int) (sumGreen[i] / numberOfPictures));
			int blue = clamp((int) (sumBlue[i] / numberOfPictures));
			sumColor[i] = Color.rgb(red, green, blue);
		}
		return new Image(Bitmap.createBitmap(sumColor, width, height, config));
	}

	private static int minI(int[] cumulativeHistogramm, int M, int N, float s) {
		int t = (int) (M * N * s);
		for (int i = 0; i < cumulativeHistogramm.length; i++) {
			if (t >= cumulativeHistogramm[i]) {
				return i;
			}
		}
		return 255;
	}

	private static int clamp(int value) {
		if (value > 255)
			return 255;
		else
			return value;
	}

	private static int maxI(int[] cumulativeHistogramm, int M, int N, float s) {
		int t = (int) (M * N * (1.0f - s));
		for (int i = cumulativeHistogramm.length - 1; i >= 0; i--) {
			if (t >= cumulativeHistogramm[i]) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * Computes an automatic contrast and returns a new {@link Image} with the
	 * adjusted contrast
	 * 
	 * @param image
	 *            image which will be automatic contrast corrected
	 * @return newly created Image with adjusted contrast.
	 */
	public static Image autoContrast(Image image) {
		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
		int amin = 0, amax = 255;
		int bRmin = 255, bRmax = 0, bGmin = 255, bGmax = 0, bBmin = 255, bBmax = 0;
		for (int i = 0; i < pixels.length; i++) {
			if (Color.red(pixels[i]) < bRmin) {
				bRmin = Color.red(pixels[i]);
			}
			if (Color.red(pixels[i]) > bRmax) {
				bRmax = Color.red(pixels[i]);
			}
			if (Color.green(pixels[i]) < bGmin) {
				bGmin = Color.green(pixels[i]);
			}
			if (Color.green(pixels[i]) > bGmax) {
				bGmax = Color.green(pixels[i]);
			}
			if (Color.blue(pixels[i]) < bBmin) {
				bBmin = Color.blue(pixels[i]);
			}
			if (Color.blue(pixels[i]) > bBmax) {
				bBmax = Color.blue(pixels[i]);
			}
		}
		for (int i = 0; i < pixels.length; i++) {
			int rNew = (int) (amin + (Color.red(pixels[i]) - bRmin) * (amax - bRmin) / (bRmax - bRmin * 1.0f));
			int gNew = (int) (amin + (Color.green(pixels[i]) - bGmin) * (amax - bGmin)
					/ (bGmax - bGmin * 1.0f));
			int bNew = (int) (amin + (Color.blue(pixels[i]) - bBmin) * (amax - bBmin)
					/ (bBmax - bBmin * 1.0f));
			pixels[i] = Color.rgb(rNew, gNew, bNew);
		}
		return new Image(Bitmap.createBitmap(pixels, image.getWidth(), image.getHeight(), image.getConfig()));
	}

	/**
	 * Creates a downscaled bitmap depending on the specific image format and
	 * the scaling factor. Can return null.
	 * 
	 * @param data
	 *            image data provided by the jpeg callback from the camera.
	 * @param pictureSize
	 *            picture size set in {@link CameraFinder}.
	 * @param imageFormat
	 *            image format set in {@link CameraFinder}.
	 * @param factor
	 *            factor for downscaled image. Should be an exponent of two.
	 * @return new downscaled bitmap or null
	 */
	public static Bitmap createScaledBitmap(byte[] data, Size pictureSize, int imageFormat, float factor) {
		// int scaledWidth = (int) (pictureSize.getWidth() * 1 / factor);
		// int scaledHeight = (int) (pictureSize.getHeight() * 1 / factor);
		int scaledWidth = 80;
		int scaledHeight = 60;
		switch (imageFormat) {
		case ImageFormat.JPEG:
			Bitmap tmp = BitmapFactory.decodeByteArray(data, 0, data.length);
			if (tmp != null) {
				return Bitmap.createScaledBitmap(tmp, scaledWidth, scaledHeight, true);
			} else {
				return null;
			}
		case ImageFormat.NV21:
			// convert yuv to jpg
			YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, pictureSize.getWidth(),
					pictureSize.getHeight(), null);
			Rect rect = new Rect(0, 0, pictureSize.getWidth(), pictureSize.getHeight());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			yuvImage.compressToJpeg(rect, 100, baos);
			// convert jpg to bmp
			Bitmap tmp2 = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
			if (tmp2 != null) {
				return Bitmap.createScaledBitmap(tmp2, scaledWidth, scaledHeight, true);
			} else {
				return null;
			}
		case ImageFormat.RGB_565:
			int[] pixels = convertByteArray(data);
			Bitmap tmp3 = Bitmap.createBitmap(pixels, pictureSize.getWidth(), pictureSize.getHeight(),
					Config.RGB_565);
			return Bitmap.createScaledBitmap(tmp3, scaledWidth, scaledHeight, true);
		default:
			return null;
		}
	}

	/**
	 * Creates an integer array from a byte array, assuming argb order
	 * 
	 * @param byteArray
	 *            Byte array data provided by the device camera
	 * @return Converted integer array
	 */
	private static int[] convertByteArray(byte[] byteArray) {
		int[] intArray = new int[byteArray.length / 2];
		int idx = 0;
		for (int i = 0; i < byteArray.length; i += 2) {
			// assuming a-r-g-b order
			int lo = ((int) byteArray[i] & 0x00FF);
			int hi = ((int) byteArray[i + 1] & 0x00FF);
			int rgb = (hi << 8) | lo;
			int r = (rgb & 0xF800) >> 11;
			int g = (rgb & 0x07E00) >> 5;
			int b = rgb & 0x001F;
			r <<= 3;
			g <<= 2;
			b <<= 3;
			intArray[idx] = 0xFF000000 | r << 16 | g << 8 | b;
			idx++;
		}
		return intArray;
	}

	private int getintensity(int color) {
		return Math.round((0.2989f * Color.red(color)) + (0.5870f * Color.green(color))
				+ (0.1140f * Color.blue(color)));
	}

}
