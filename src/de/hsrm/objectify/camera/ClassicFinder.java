package de.hsrm.objectify.camera;

import android.hardware.Camera;
import android.hardware.Camera.Size;

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
	Camera open() {
		if (android.os.Build.PRODUCT.equals("GT-P1000")) {
			// running on samsun galaxy tab, using only working size and picture format on this device
			Camera camera = Camera.open();
			Camera.Parameters params = camera.getParameters();
			Size s = params.getSupportedPictureSizes().get(0);
			pictureSize = new de.hsrm.objectify.utils.Size(s.width, s.height);
			imageFormat = params.getSupportedPictureFormats().get(0);
			params.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
			params.setPictureFormat(imageFormat);
			params.set("camera-id", 2); 
			camera.setParameters(params);
			return camera;
		}
		return null;
//		camera = Camera.open();		
//		Camera.Parameters params = camera.getParameters();
//		if (android.os.Build.PRODUCT.equals("GT-P1000")) {
//			// we're running on samsung galaxy tab
//			// only working size and picture format for samsung galaxy tab
//			previewSize = new Size(800,600);
//			pictureSize = new Size(800, 600);
//			imageFormat = params.getSupportedPictureFormats().get(0);
//			params.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
//			params.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
//			params.setPictureFormat(imageFormat);
//			params.set("camera-id", 2); // using front-cam (2) instead
//										// of back-cam (1)
	}

}
