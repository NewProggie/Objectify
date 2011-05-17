package de.hsrm.objectify.utils;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.util.TypedValue;

public class ImageHelper {
	
	public static int[] convertByteArray(byte[] byteArray, Config config) {
		if (config.equals(Config.ARGB_8888)) {
			return convertFromARGB_8888(byteArray);
		} else if (config.equals(Config.RGB_565)) {
			return convertFromRGB_565(byteArray);
		} else {
			return null;
		}
	}
	
	private static int[] convertFromRGB_565(byte[] byteArray) {
		int[] intArray = new int[byteArray.length/2];
		int idx = 0;
		for (int i=0;i<byteArray.length;i+=2) {
			// assuming argb
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
		}
		return intArray;
	}
	
	public static void conv_rgb565_to_rgb24(int[] out, byte[] data, int width, int height) {
		int pos = 0;
		  int bound = (width * height) << 1;
		  for (int i = 0; i < bound; ++i) {
		    int lo = ((int) data[i] & 0x00FF);
		    int hi = ((int) data[i + 1] & 0x00FF);
		    int rgb = (hi << 8) | lo;
		    int r = (rgb & 0xF800) >> 11;
		    int g = (rgb & 0x07E00) >> 5;
		    int b = rgb & 0x001F;
		    r <<= 3;
		    g <<= 2;
		    b <<= 3;
		    out[pos++] = 0xff000000 | r << 16 | g << 8 | b;
		  }
	}
	
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

	public static int dipToPx(int dpValue, Context c) {
		return (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, dpValue, c.getResources().getDisplayMetrics());
	}
}
