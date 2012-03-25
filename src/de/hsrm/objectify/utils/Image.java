package de.hsrm.objectify.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Matrix;

/**
 * This class behaves mostly as a {@link Bitmap}, but extends its functionality
 * by some convenience methods and basic methods from digital image processing.
 * 
 * @author kwolf001
 * 
 */
public class Image {

	private Bitmap bitmap;

	public Image(int width, int height, Config config) {
		bitmap = Bitmap.createBitmap(width, height, config);
	}

	public Image(Bitmap bitmap) {
		this.bitmap = Bitmap.createBitmap(bitmap);
	}

	/**
	 * Constructor for Image.
	 * 
	 * @param bitmap
	 * @param fromCamera
	 */
	public Image(Bitmap bitmap, boolean fromCamera) {
		if (fromCamera) {
			Matrix flipMatrix = new Matrix();
			flipMatrix.preScale(1.0f, -1.0f);
			this.bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), flipMatrix, true);
		} else {
			Matrix rotMatrix = new Matrix();
			rotMatrix.postRotate(90);
			Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
					bitmap.getWidth(), bitmap.getHeight(), rotMatrix, true);
			Matrix flipMatrix = new Matrix();
			flipMatrix.preScale(1.0f, -1.0f);
			this.bitmap = Bitmap.createBitmap(rotatedBitmap, 0, 0,
					rotatedBitmap.getWidth(), rotatedBitmap.getHeight(),
					flipMatrix, true);
		}
	}

	public Image(Bitmap bitmap, int degrees) {
		Matrix rotMatrix = new Matrix();
		rotMatrix.postRotate(degrees);
		Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), rotMatrix, true);
		Matrix flipMatrix = new Matrix();
		flipMatrix.preScale(1.0f, -1.0f);
		this.bitmap = Bitmap.createBitmap(rotatedBitmap, 0, 0,
				rotatedBitmap.getWidth(), rotatedBitmap.getHeight(),
				flipMatrix, true);
	}

	public void setPixel(int x, int y, int color) {
		bitmap.setPixel(x, y, color);
	}

	public int[] getPixels() {
		int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
		bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, getWidth(),
				getHeight());
		return pixels;
	}

	public void getPixels(int[] pixels, int offset, int stride, int x, int y,
			int width, int height) {
		bitmap.getPixels(pixels, offset, stride, x, y, width, height);
	}

	public int[] getHistogram() {
		int[] histogram = new int[256];
		for (int i = 0; i < histogram.length; i++) {
			histogram[i] = 0;
		}

		int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
		bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, getWidth(),
				getHeight());

		for (int i = 0; i < pixels.length; i++) {
			int intensity = getintensity(pixels[i]);
			histogram[intensity] += 1;
		}
		return histogram;
	}

	/**
	 * returns histogram for given channel
	 * 
	 * @param channel
	 *            channel for picture. Either 0=red, 1=green, 2=blue
	 * @return histogram for given channel
	 */
	public int[] getHistogram(int channel) {
		int[] histogram = new int[256];
		for (int i = 0; i < histogram.length; i++) {
			histogram[i] = 0;
		}
		int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
		int[] tmp = new int[bitmap.getWidth() * bitmap.getHeight()];
		bitmap.getPixels(tmp, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
				bitmap.getHeight());
		switch (channel) {
		case 0:
			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = Color.red(tmp[i]);
			}
			break;
		case 1:
			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = Color.green(tmp[i]);
			}
			break;
		case 2:
			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = Color.blue(tmp[i]);
			}
			break;
		}
		for (int i = 0; i < pixels.length; i++) {
			int intensity = pixels[i];
			histogram[intensity] += 1;
		}
		return histogram;
	}

	public int getWidth() {
		return bitmap.getWidth();
	}

	public int getHeight() {
		return bitmap.getHeight();
	}

	public int getPixel(int x, int y) {
		return bitmap.getPixel(x, y);
	}

	public Config getConfig() {
		return bitmap.getConfig();
	}

	public Image copy() {
		Image img = new Image(bitmap);
		return img;
	}

	public void compress(CompressFormat format, int quality,
			BufferedOutputStream bos) {
		bitmap.compress(format, quality, bos);
	}

	public void compress(CompressFormat format, int quality,
			ByteArrayOutputStream baos) {
		bitmap.compress(format, quality, baos);
	}

	private static int getintensity(int color) {
		return Math.round((0.2989f * Color.red(color))
				+ (0.5870f * Color.green(color))
				+ (0.1140f * Color.blue(color)));
	}

	public int getIntensity(int x, int y) {
		int pixel = bitmap.getPixel(x, y);
		return getGreyscale(pixel);
	}

	private short getGreyscale(int pixelColor) {
		int red = (pixelColor >> 16) & 0xFF;
		int green = (pixelColor >> 8) & 0xFF;
		int blue = (pixelColor >> 0) & 0xFF;
		if (red == 0 & green == 0 & blue == 0) {
			return 1;
		} else {
			return (short) ((red + green + blue) / 3.0f);
		}
	}

	public void rotate(int degrees) {
		Matrix rotMatrix = new Matrix();
		rotMatrix.postRotate(degrees);
		this.bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), rotMatrix, true);
	}

}
