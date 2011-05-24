package de.hsrm.objectify.utils;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

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
