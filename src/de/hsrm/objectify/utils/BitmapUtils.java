package de.hsrm.objectify.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;

public class BitmapUtils {
	
	private static final String TAG = "BitmapUtils";
	
	public static Bitmap createBitmap(byte[] data, Size pictureSize, int imageFormat) {
		switch (imageFormat) {
		case ImageFormat.JPEG:
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		case ImageFormat.RGB_565:
			int[] pixels = convertByteArray(data);
			return Bitmap.createBitmap(pixels, pictureSize.getWidth(), pictureSize.getHeight(), Config.RGB_565);
		default:
			return null;
		}
	}
	
	private static int[] convertByteArray(byte[] byteArray) {
		int[] intArray = new int[byteArray.length/2];
		int idx = 0;
		for (int i=0; i<byteArray.length; i+=2) {
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

}
