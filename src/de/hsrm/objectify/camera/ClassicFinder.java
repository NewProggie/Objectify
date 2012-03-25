package de.hsrm.objectify.camera;

import android.content.Context;
import android.hardware.Camera;

/**
 * Returns a front facing camera from current device guessing from device
 * product id, if there is one available or null, if no front facing camera was
 * found. This class provides a downward compatibility for devices which don't
 * run android 2.3 (gingerbread) but still provide a front facing camera.
 * 
 * @author kwolf001
 * 
 */
public class ClassicFinder extends CameraFinder {

	/**
	 * Return a new instance from front facing camera or null if none was found.
	 */
	public Camera open(Context context) {
		if (android.os.Build.PRODUCT.equals("GT-P1000")) {
			// running on samsung galaxy tab, using only working size and
			// picture
			// format on this device
			Camera camera = Camera.open();
			Camera.Parameters params = camera.getParameters();

			pictureSize = new de.hsrm.objectify.utils.Size(600, 800);
			// dirty hack! Even though this device promises to deliver a jpeg
			// image, BitmapFactory.decodeByteArray(..) fails to create one. So
			// we're setting PreviewFormat instead of PictureFormat and take
			// care of it by ourself.
			imageFormat = params.getSupportedPreviewFormats().get(0);
			params.setPictureSize(pictureSize.getWidth(),
					pictureSize.getHeight());
			params.setPreviewSize(800, 600);
			params.setPictureFormat(imageFormat);
			params.set("device", "GT-P1000");
			params.set("camera-id", 2); // using front-cam (2) instead of
										// back-cam (1)
			params.set("exifOrientation", 90);

			camera.setParameters(params);
			return camera;
		}

		return null;
	}

}
