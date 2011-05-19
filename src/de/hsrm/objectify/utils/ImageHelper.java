package de.hsrm.objectify.utils;

import de.hsrm.objectify.camera.Size;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.util.TypedValue;

/**
 * This class implements some convenience methods for dealing with raw image
 * data and screen resolutions.
 * 
 * @author kwolf001
 * 
 */
public class ImageHelper {
	
	private static Size previewSize, pictureSize;
	private static int imageFormat;
	/**
	 * Converts a byte array into an int array, depending on given
	 * {@link Config}.
	 * 
	 * @param byteArray
	 *            array which will be converted
	 * @param config
	 *            target configuration
	 * @return converted integer array
	 */
	public static int[] convertByteArray(byte[] byteArray, Config config, Size previewSize, Size pictureSize, int imageFormat) {
		ImageHelper.previewSize = previewSize;
		ImageHelper.pictureSize = pictureSize;
		ImageHelper.imageFormat = imageFormat;
		if (config.equals(Config.ARGB_8888)) {
			return convertFromARGB_8888(byteArray);
		} else if (config.equals(Config.RGB_565)) {
			return convertFromRGB_565(byteArray);
		} else {
			return null;
		}
	}
	
	/**
	 * Converting given byte array with <code>RGB_565</code> configuration.
	 * Every pixel here is stored in two bytes.
	 * 
	 * @param byteArray
	 *            array which will be converted
	 * @return converted integer array
	 */
	private static int[] convertFromRGB_565(byte[] byteArray) {
		int[] intArray = new int[(byteArray.length/2)+1];
		Log.d("byteArray", String.valueOf(byteArray.length));
		Log.d("intArray", String.valueOf(intArray.length));
		Log.d("bitsPerPixel", String.valueOf(ImageFormat.getBitsPerPixel(imageFormat)));
		Log.d("ImageFormat JPEG", String.valueOf(ImageFormat.getBitsPerPixel(ImageFormat.JPEG)));
		Log.d("ImageFormat NV16", String.valueOf(ImageFormat.getBitsPerPixel(ImageFormat.NV16)));
		Log.d("ImageFormat NV21", String.valueOf(ImageFormat.getBitsPerPixel(ImageFormat.NV21)));
		Log.d("ImageFormat RGB_565", String.valueOf(ImageFormat.getBitsPerPixel(ImageFormat.RGB_565)));
		Log.d("ImageFormat UNKNOWN", String.valueOf(ImageFormat.getBitsPerPixel(ImageFormat.UNKNOWN)));
		Log.d("ImageFormat YUY2", String.valueOf(ImageFormat.getBitsPerPixel(ImageFormat.YUY2)));
		Log.d("ImageFormat YV12", String.valueOf(ImageFormat.getBitsPerPixel(ImageFormat.YV12)));
		
		int idx = 0;
		for (int i=0;i<byteArray.length;i+=2) {
			if (idx >= intArray.length-20) 
				Log.d("idx", String.valueOf(idx));
			if (idx%2000 == 0)
				Log.d("idx", String.valueOf(idx));
			// assuming a-r-g-b order
			int lo = ((int) byteArray[i] & 0x00FF);
			int hi = ((int) byteArray[i+1] & 0x00FF);
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
	
	/**
	 * Converting given byte array with <code>ARGB_8888</code> configuration.
	 * Every Pixel is stored in four bytes. Every byte contains either
	 * transparency, red, green or blue value.
	 * 
	 * @param byteArray
	 *            array which will be converted
	 * @return converted integer array
	 */
	private static int[] convertFromARGB_8888(byte[] byteArray) {
		int[] intArray = new int[((int) byteArray.length/4)];
		int idx = 0;
		for (int i=0; i<byteArray.length;i+=4) {
			intArray[idx] = (0xFF & byteArray[i]) << 24 |
							(0xFF & byteArray[i+1]) << 16 |
							(0xFF & byteArray[i+2]) << 8 |
							(0xFF & byteArray[i+3]) << 0;
			idx += 1;
		}
		return intArray;
	}

	/**
	 * Converts device dependent pixels into absolute pixels appropriate for the
	 * current device using display metrics.
	 * 
	 * @param dpValue device independent pixel value which should be converted
	 * @param c current Context
	 * @return matching absolute pixels for given dip value
	 */
	public static int dipToPx(int dpValue, Context c) {
		return (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, dpValue, c.getResources().getDisplayMetrics());
	}
}
