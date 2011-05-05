package de.hsrm.objectify;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.LinearLayout;

public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
	private CameraPreview cameraPreview;
	private LinearLayout lo, ro, lu, ru;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lights);
		lo = (LinearLayout) findViewById(R.id.light_lo);
		ro = (LinearLayout) findViewById(R.id.light_ro);
		lu = (LinearLayout) findViewById(R.id.light_lu);
		ru = (LinearLayout) findViewById(R.id.light_ru);

//		new ShowLights().execute();
		 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		 cameraPreview = new CameraPreview(this);
		 setContentView(cameraPreview);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (event != null && event.getAction() == MotionEvent.ACTION_UP) {

		}
		return super.onTrackballEvent(event);
	}

	private class ShowLights extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			publishProgress(1);
			SystemClock.sleep(1000);
			publishProgress(2);
			SystemClock.sleep(1000);
			publishProgress(3);
			SystemClock.sleep(1000);
			publishProgress(4);
			SystemClock.sleep(1000);
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			int which = values[0];
			switch (which) {
			case 1:
				lo.setBackgroundColor(Color.BLUE);
				break;
			case 2:
				lo.setBackgroundColor(Color.BLACK);
				ro.setBackgroundColor(Color.BLUE);
				break;
			case 3:
				ro.setBackgroundColor(Color.BLACK);
				lu.setBackgroundColor(Color.BLUE);
				break;
			case 4:
				lu.setBackgroundColor(Color.BLACK);
				ru.setBackgroundColor(Color.BLUE);
				break;
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			ru.setBackgroundColor(Color.BLACK);
		}

	}
}

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder holder;
	private Camera camera;
	private static final String TAG = "CameraPreview";

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
	
	/**
	 * Open the camera. First attempt to find and open the front-facing camera.
	 * If that attempt fails, then fall back to whatever camera is available.
	 * 
	 * @return a Camera object
	 */
	private Camera openFrontFacingCamera() {
		Camera camera = null;

		// Look for front-facing camera, using the Gingerbread API.
		// Java reflection is used for backwards compatibility with
		// pre-Gingerbread APIs.
		try {
			Class<?> cameraClass = Class.forName("android.hardware.Camera");
			Object cameraInfo = null;
			Field field = null;
			int cameraCount = 0;
			Method getNumberOfCamerasMethod = cameraClass.getMethod("getNumberOfCameras");
			if (getNumberOfCamerasMethod != null) {
				cameraCount = (Integer) getNumberOfCamerasMethod.invoke(null, (Object[]) null);
			}
			Class<?> cameraInfoClass = Class.forName("android.hardware.Camera$CameraInfo");
			if (cameraInfoClass != null) {
				cameraInfo = cameraInfoClass.newInstance();
			}
			if (cameraInfo != null) {
				field = cameraInfo.getClass().getField("facing");
			}
			Method getCameraInfoMethod = cameraClass.getMethod("getCameraInfo", Integer.TYPE, cameraInfoClass);
			if (getCameraInfoMethod != null && cameraInfoClass != null && field != null) {
				for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
					getCameraInfoMethod.invoke(null, camIdx, cameraInfo);
					int facing = field.getInt(cameraInfo);
					if (facing == 1) { // Camera.CameraInfo.CAMERA_FACING_FRONT
						try {
							Method cameraOpenMethod = cameraClass.getMethod("open", Integer.TYPE);
							if (cameraOpenMethod != null) {
								camera = (Camera) cameraOpenMethod.invoke(null, camIdx);
							}
						} catch (RuntimeException e) {
							Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
						}
					}
				}
			}
		}
		// Ignore the bevy of checked exceptions the Java Reflection API throws
		// - if it fails, who cares.
		catch (ClassNotFoundException e) {
			Log.e(TAG, "ClassNotFoundException" + e.getLocalizedMessage());
		} catch (NoSuchMethodException e) {
			Log.e(TAG, "NoSuchMethodException" + e.getLocalizedMessage());
		} catch (NoSuchFieldException e) {
			Log.e(TAG, "NoSuchFieldException" + e.getLocalizedMessage());
		} catch (IllegalAccessException e) {
			Log.e(TAG, "IllegalAccessException" + e.getLocalizedMessage());
		} catch (InvocationTargetException e) {
			Log.e(TAG, "InvocationTargetException" + e.getLocalizedMessage());
		} catch (InstantiationException e) {
			Log.e(TAG, "InstantiationException" + e.getLocalizedMessage());
		} catch (SecurityException e) {
			Log.e(TAG, "SecurityException" + e.getLocalizedMessage());
		}

		if (camera == null) {
			// Try using the pre-Gingerbread APIs to open the camera.
			try {
				camera = Camera.open();
				// Samsung Galaxy S hack
				Camera.Parameters parameters = camera.getParameters();
				parameters.set("camera-id", 2);
				camera.setParameters(parameters);
			} catch (RuntimeException e) {
				Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
			}
		}

		return camera;
	}

	public void takePicture() {
		camera.takePicture(shutterCallback(), pictureCallback(), null);
	}

	private Camera.ShutterCallback shutterCallback() {
		ShutterCallback shutterCall = new ShutterCallback() {

			@Override
			public void onShutter() {

			}
		};
		return shutterCall;
	}

	private Camera.PictureCallback pictureCallback() {
		Camera.PictureCallback pictureCall = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {

			}
		};
		return pictureCall;
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
}
