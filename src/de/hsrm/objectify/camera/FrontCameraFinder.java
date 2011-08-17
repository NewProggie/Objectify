package de.hsrm.objectify.camera;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.hardware.Camera;
import de.hsrm.objectify.R;
import de.hsrm.objectify.SettingsActivity;
import de.hsrm.objectify.utils.Size;

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
	public Camera open(Context context) {
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		
		for (int i=0; i<Camera.getNumberOfCameras();i++) {
			Camera.getCameraInfo(i, cameraInfo);
			
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				Camera camera = Camera.open(i);
				Camera.Parameters params = camera.getParameters();
				
				SharedPreferences preferences = SettingsActivity.getSettings((ContextWrapper) context);
				String userSettingPicDim = preferences.getString(context.getString(R.string.settings_camera_resolutions), "");
				Size picDim;
				if (userSettingPicDim == "") {
					picDim = new Size(params.getSupportedPictureSizes().get(0).width, params.getSupportedPictureSizes().get(0).height);
				} else {
					String[] dims = userSettingPicDim.split("x");
					picDim = new de.hsrm.objectify.utils.Size(Integer.valueOf(dims[0]),Integer.valueOf(dims[1]));
				}
								
				pictureSize = new Size(picDim.getWidth(), picDim.getHeight());
				imageFormat = params.getSupportedPictureFormats().get(0);
				params.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
				params.setPictureFormat(imageFormat);
				camera.setParameters(params);
				camera.setDisplayOrientation(90);
				return camera;
			}
		}
		return null;
	}

}
