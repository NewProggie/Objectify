package de.hsrm.objectify;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
	private CameraPreview cameraPreview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		cameraPreview = new CameraPreview(this);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(cameraPreview);
	}
	
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (event != null && event.getAction() == MotionEvent.ACTION_UP) {
			cameraPreview.takePictures();
			Intent backToMain = new Intent(this, MainActivity.class);
			startActivity(backToMain);
		}
		return super.onTrackballEvent(event);
	}
	
	
}

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	
	private static final String TAG = "CameraPreview";
	private SurfaceHolder holder;
	private Camera camera;
	private final int NUMBER_OF_PICTURES = 3;
	private int counter;
	
	public CameraPreview(Context context) {
		super(context);
		
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			camera.release();
			camera = null;
		}
	}
	
	public void takePictures() {
		counter = 0;
		camera.takePicture(shutterCallback(), pictureCallback(), pictureJpegCallback());
	}
		
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
		camera = null;
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Camera.Parameters parameters = camera.getParameters();
		ArrayList<Size> supportedSizes = new ArrayList<Size>(parameters.getSupportedPreviewSizes());
		Size size = camera.getParameters().getPreviewSize();
		// setting highest resolution for pictures
		for (Size tmp : supportedSizes) {
			if (compareSizes(tmp, size)==1) {
				size = tmp;
			}
		}
		parameters.setPreviewSize(size.width, size.height);
		camera.setParameters(parameters);
		camera.startPreview();
	}
	
	private Camera.ShutterCallback shutterCallback() {
		Log.d(TAG, "shutterCallback()");
		ShutterCallback shutterCall = new ShutterCallback() {
			
			@Override
			public void onShutter() {
				
			}
		};
		return shutterCall;
	}
	
	private Camera.PictureCallback pictureJpegCallback() {
		Log.d(TAG, "pictureJpegCallback()");
		counter++;
		Log.d("numberOfPictures: ", String.valueOf(counter));
		if (counter < NUMBER_OF_PICTURES) {
			camera.takePicture(shutterCallback(), pictureCallback(), pictureJpegCallback());
		} 
		return null;
	}
	
	private Camera.PictureCallback pictureCallback() {
		Log.d(TAG, "pictureCallback()");
		Camera.PictureCallback pictureCall = new PictureCallback() {
			
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				
			}
		};
		return pictureCall;
	}
	
	private int compareSizes(Size a, Size b) {
		int a2 = a.width*a.height;
		int b2 = b.width*b.height;
		if (a2 > b2) {
			return 1;
		} else if (a2 == b2) {
			return 0;
		} else {
			return -1;
		}
	}
}
