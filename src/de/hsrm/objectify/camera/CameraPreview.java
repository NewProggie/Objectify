package de.hsrm.objectify.camera;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "CameraPreview";
	private SurfaceHolder holder;
	private Camera camera;

	public CameraPreview(Context context) {
		super(context);
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		camera = openFrontFacingCamera();
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			camera.release();
			camera = null;
		}
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(width, height);
		parameters.set("orientation", "portrait");
		camera.setParameters(parameters);
		camera.startPreview();
	}

	private Camera openFrontFacingCamera() {
		Camera camera = null;

		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentApiVersion > android.os.Build.VERSION_CODES.FROYO) {
			// gingerbread (2.3) and above
			int cameraCount = 0;
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			cameraCount = Camera.getNumberOfCameras();
			for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
				Camera.getCameraInfo(camIdx, cameraInfo);
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					try {
						camera = Camera.open(camIdx);
					} catch (RuntimeException e) {
						Log.e(TAG, "Failed to open camera:" + e.getMessage());
					}
				}
			}
		} else {
			// froyo (2.2)
			camera = Camera.open();
			if (android.os.Build.PRODUCT.equals("GT-P1000")) {
				// we're running on samsung galaxy tab
				Camera.Parameters params = camera.getParameters();
				params.set("camera-id", 2); // using front-cam (2) instead
											// of back-cam (1)
				camera.setParameters(params);
			} else {
				camera = Camera.open();
			}
		}
		return camera;
	}

}
