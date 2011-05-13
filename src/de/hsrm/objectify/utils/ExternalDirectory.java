package de.hsrm.objectify.utils;

import java.io.File;

import android.os.Environment;
import android.util.Log;

/**
 * This class takes care of state of external storage and creates folder
 * structure for saving photos and obj files.
 * 
 * @author kwolf001
 * 
 */
public class ExternalDirectory {

	private static final String TAG = "ExternalDirectory";
	
	/**
	 * Takes care of state of external storage and throws a
	 * {@link RuntimeException} if sdcard is unmounted
	 * 
	 * @return path to external storage
	 */
	public static String getExternalDirectory() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File extDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.objectify/");
			if (extDir.mkdirs() || extDir.exists()) {
				return extDir.getAbsolutePath();
			} else {
				throw new RuntimeException("Couldn't create external directory");
			}
		} else {
			throw new RuntimeException("External Storage is currently not available");
		}
	}
}
