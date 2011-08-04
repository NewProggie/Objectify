package de.hsrm.objectify.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;

/**
 * This class behaves mostly as a {@link Bitmap}, but extends its functionality
 * by some convenience methods.
 * 
 * @author kwolf001
 * 
 */
public class Image {

	private Bitmap bitmap;
	
	public Image(int width, int height, Config config) {
		bitmap = Bitmap.createBitmap(width, height, config);
	}
	
	public Image(Bitmap bitmap, boolean shouldRotateAndFlip) {
		if (shouldRotateAndFlip) {
			// TODO: Wieder rausnehmen, sobald der Fehler bei der 3D-Rekonstruktion gefixt ist.
			////// Dreht das Bild um 90° nach rechts und spiegelt es vertikal
			Matrix rotMatrix = new Matrix();
			rotMatrix.postRotate(90);
			Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotMatrix, true);
			Matrix flipMatrix = new Matrix();
			flipMatrix.preScale(1.0f, -1.0f);
			this.bitmap = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), flipMatrix, true);
		} else {
			this.bitmap = Bitmap.createBitmap(bitmap);
		}
	}
	
	/**
	 * Converts picture into a grayscaled picture and calculates a grayscale
	 * splay
	 */
	public void toGrayscale() {
		short[] grayPixels = getIntensity(true);
		int[] newPixels = new int[getWidth()*getHeight()];
		for(int i=0; i<grayPixels.length; i++) {
			newPixels[i] = Color.rgb(grayPixels[i], grayPixels[i], grayPixels[i]);
		}
		bitmap = Bitmap.createBitmap(newPixels, getWidth(), getHeight(), getConfig());
	}
	
	public void setPixel(int x, int y, int color) {
		bitmap.setPixel(x, y, color);
	}

	public int[] getPixels() {
		int[] pixels = new int[bitmap.getWidth()*bitmap.getHeight()];
		bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, getWidth(), getHeight());
		return pixels;
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
		Image img = new Image(bitmap, false);
		return img;
	}

	public void compress(CompressFormat format, int quality, BufferedOutputStream bos) {
		bitmap.compress(format, quality, bos);
	}
	
	public void compress(CompressFormat format, int quality, ByteArrayOutputStream baos) {
		bitmap.compress(format, quality, baos);
	}
	
	/**
	 * Returns the intensity of this image as a float array
	 * 
	 * @param doGreyscaleCorrection
	 *            calculates a grayscale splay if true, else returns current
	 *            intensity
	 * @return intensity per pixel as float array
	 */
	private short[] getIntensity(boolean doGreyscaleCorrection) {
		int[] pixels = getPixels();
		short[] map = new short[getPixels().length];
		for (int i=0; i<map.length;i++) {
			map[i] = getGreyscale(pixels[i]);
		}
		if (doGreyscaleCorrection) {
			short gmin = 255, gmax = 0;
			for (int i=0; i<map.length; i++) {
				if (map[i] < gmin) { gmin = map[i]; }	
				if (map[i] > gmax) { gmax = map[i]; }
			}
			for (int i=0; i<map.length; i++) {
				map[i] = (short) ((1 - ((gmax-map[i])/(gmax-gmin*1.0f))) * 255);
			}
		}
		return map;
	}
	
	public float getIntensity(int x, int y) {
		int pixel = bitmap.getPixel(x, y);
		return getGreyscale(pixel);
	}
	
	private short getGreyscale(int pixelColor) {
		int red = (pixelColor >> 16) & 0xFF;
		int green = (pixelColor >> 8) & 0xFF;
		int blue = (pixelColor >> 0) & 0xFF;
		if (red==0 || green==0 || blue==0) {
			return 0;
		} else {
			return (short) ((red + green + blue) / 3.0f);
		}
	}


}
