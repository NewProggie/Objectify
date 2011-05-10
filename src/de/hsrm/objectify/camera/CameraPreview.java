package de.hsrm.objectify.camera;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "CameraPreview";
	private SurfaceHolder holder;
	private static Camera camera;

	public CameraPreview(Context context) {
		super(context);
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	
	public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}


	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
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
	
	public static Camera getCamera() {
		return camera;
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		camera.startPreview();
	}
	
	public void takePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback jpeg) {
		camera.takePicture(shutter, raw, jpeg);
	}
	
	public void startPreview() {
		camera.startPreview();
	}
	
	public static Camera openFrontFacingCamera() {
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
			// TODO remove hack for devices without front-facing cam
			if (camera == null) {
				camera = Camera.open();
			}
		} else {
			// froyo (2.2)
			camera = Camera.open();
			if (android.os.Build.PRODUCT.equals("GT-P1000")) {
				// we're running on samsung galaxy tab
				Camera.Parameters params = camera.getParameters();
				// only working size for samsung galaxy tab
				params.setPictureSize(800, 600);
				params.setPreviewSize(800, 600);
				params.set("camera-id", 2); // using front-cam (2) instead
											// of back-cam (1)
				camera.setParameters(params);
			} 
		}
		return camera;
	}

}
