package de.hsrm.objectify.camera;

import android.hardware.Camera;
import android.hardware.Camera.Size;

/**
 * Returns front facing camera from current device using 2.3 (Gingerbread) API.
 * Can and will return null if no front facing camera was found.
 * 
 * @author kwolf001
 * 
 */
public class FrontCameraFinder extends CameraFinder {

	/**
	 * Returns a new instance from front facing camera or null if none was found.
	 */
	public Camera open() {
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		
		for (int i=0; i<Camera.getNumberOfCameras();i++) {
			Camera.getCameraInfo(i, cameraInfo);
			
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				Camera camera = Camera.open(i);
				Camera.Parameters params = camera.getParameters();
				Size s = params.getSupportedPictureSizes().get(0);
				pictureSize = new de.hsrm.objectify.utils.Size(s.width, s.height);
				imageFormat = params.getSupportedPictureFormats().get(0);
				params.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
				params.setPictureFormat(imageFormat);
				camera.setParameters(params);
				camera.setDisplayOrientation(90);
				// TODO setting Camera.Parameters properly, also for nexus s problem
				return camera;
			}
		}
		return null;
	}

}
