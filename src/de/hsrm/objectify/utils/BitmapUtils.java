package de.hsrm.objectify.utils;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.SystemClock;
import android.util.Log;
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
	 * @param data Image data provided by the jpeg callback from the camera
	 * @param pictureSize Picture size set in {@link CameraFinder}
	 * @param imageFormat Image format set in {@link CameraFinder}
	 * @return New bitmap or null
	 */
	public static Bitmap createBitmap(byte[] data, Size pictureSize, int imageFormat) {
		switch (imageFormat) {
		case ImageFormat.JPEG:
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		case ImageFormat.NV21:
			// convert yuv to jpg
			YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, pictureSize.getWidth(), pictureSize.getHeight(),
					null);
			Rect rect = new Rect(0, 0, pictureSize.getWidth(), pictureSize.getHeight());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			yuvImage.compressToJpeg(rect, 100, baos);
			// convert jpg to bmp
			return BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
		case ImageFormat.RGB_565:
			int[] pixels = convertByteArray(data);
			return Bitmap.createBitmap(pixels, pictureSize.getWidth(), pictureSize.getHeight(), Config.RGB_565);
		default:
			return null;
		}
	}
	
	/**
	 * Creates a blured image based on the source image.
	 * @param src source image.
	 * @return a new blured image based on the source image.
	 */
	public static Image blurBitmap(Image src) {
		GaussianFilter filter = new GaussianFilter(4);
		Image dst = filter.filter(src);
		return dst;
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
    	int[] pixels = new int[image.getWidth()*image.getHeight()];
    	image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
    	int amin = 0, amax = 255;
    	int bRmin = 255, bRmax = 0, bGmin = 255, bGmax = 0, bBmin = 255, bBmax = 0;
    	for (int i=0; i<pixels.length; i++) {
    		if (Color.red(pixels[i]) < bRmin) { bRmin = Color.red(pixels[i]); }
    		if (Color.red(pixels[i]) > bRmax) { bRmax = Color.red(pixels[i]); }
    		if (Color.green(pixels[i]) < bGmin) { bGmin = Color.green(pixels[i]); }
    		if (Color.green(pixels[i]) > bGmax) { bGmax = Color.green(pixels[i]); }
    		if (Color.blue(pixels[i]) < bBmin) { bBmin = Color.blue(pixels[i]); }
    		if (Color.blue(pixels[i]) > bBmax) { bBmax = Color.blue(pixels[i]); }
    	}
    	for (int i=0; i<pixels.length; i++) {
    		int rNew = (int) (amin + (Color.red(pixels[i]) - bRmin) * (amax-bRmin)/(bRmax-bRmin*1.0f));
    		int gNew = (int) (amin + (Color.green(pixels[i]) - bGmin) * (amax-bGmin)/(bGmax-bGmin*1.0f));
    		int bNew = (int) (amin + (Color.blue(pixels[i]) - bBmin) * (amax-bBmin)/(bBmax-bBmin*1.0f));
    		pixels[i] = Color.rgb(rNew, gNew, bNew);
    	}
    	return new Image(Bitmap.createBitmap(pixels, image.getWidth(), image.getHeight(), image.getConfig()));
    }
	
	/**
	 * Creates a downscaled bitmap depending on the specific image format and the scaling factor. Can return null.
	 * @param data image data provided by the jpeg callback from the camera.
	 * @param pictureSize picture size set in {@link CameraFinder}.
	 * @param imageFormat image format set in {@link CameraFinder}.
	 * @param factor factor for downscaled image. Should be an exponent of two.
	 * @return new downscaled bitmap or null
	 */
	public static Bitmap createScaledBitmap(byte[] data, Size pictureSize, int imageFormat, float factor) {
		int scaledWidth = (int) (pictureSize.getWidth() * 1/factor);
		int scaledHeight = (int) (pictureSize.getHeight() * 1/factor);
		switch (imageFormat) {
		case ImageFormat.JPEG:
			Bitmap tmp = BitmapFactory.decodeByteArray(data, 0, data.length);
			if (tmp != null) {
				return Bitmap.createScaledBitmap(tmp, scaledWidth,
						scaledHeight, true);
			} else {
				return null;
			}
		case ImageFormat.NV21:
			// convert yuv to jpg
			YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,
					pictureSize.getWidth(), pictureSize.getHeight(), null);
			Rect rect = new Rect(0, 0, pictureSize.getWidth(),
					pictureSize.getHeight());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			yuvImage.compressToJpeg(rect, 100, baos);
			// convert jpg to bmp
			Bitmap tmp2 = BitmapFactory.decodeByteArray(baos.toByteArray(), 0,
					baos.size());
			if (tmp2 != null) {
				return Bitmap.createScaledBitmap(tmp2, scaledWidth, scaledHeight, true);
			} else {
				return null;
			}
		case ImageFormat.RGB_565:
			int[] pixels = convertByteArray(data);
			Bitmap tmp3 =  Bitmap.createBitmap(pixels, pictureSize.getWidth(),
					pictureSize.getHeight(), Config.RGB_565);
			return Bitmap.createScaledBitmap(tmp3, scaledWidth, scaledHeight, true);
		default:
			return null;
		}
	}
	
	/**
	 * Creates an integer array from a byte array, assuming argb order
	 * @param byteArray Byte array data provided by the device camera
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

}
