package de.hsrm.objectify.utils;

import java.io.File;

import android.os.Environment;
import android.util.Log;

public class ExternalDirectory {

	private static final String TAG = "ExternalDirectory";
	
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
