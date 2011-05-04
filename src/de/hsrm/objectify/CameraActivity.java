package de.hsrm.objectify;

import java.io.IOException;

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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;

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
		
		new ShowLights().execute();
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		cameraPreview = new CameraPreview(this);
//		setContentView(cameraPreview);
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
