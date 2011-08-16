package de.hsrm.objectify.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import de.hsrm.objectify.utils.Size;

/**
 * This (abstract) class is implemented by {@link ClassicFinder} and
 * {@link FrontCameraFinder} to provide some downward compatibility for devices
 * which don't run 2.3 (Gingerbread) but still have front facing cameras, such
 * as the samsung galaxy tab. Can and will return null if there's no front
 * facing camera available on the current device.
 * 
 * @author kwolf001
 * 
 */
public abstract class CameraFinder {

	public abstract Camera open(Context context);
	static int imageFormat;
	static Size pictureSize;
	public static CameraFinder INSTANCE = buildFinder();
	
	private static CameraFinder buildFinder() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return (new FrontCameraFinder());
		} else {
			return (new ClassicFinder());
		}
	}
}
