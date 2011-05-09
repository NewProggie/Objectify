package de.hsrm.objectify.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import de.hsrm.objectify.MainActivity;
import de.hsrm.objectify.R;

public class TakePictureActivity extends Activity {

	private static final String TAG = "TakePictureActivity";
	private LinearLayout lo, ro, lu, ru;
	private Camera camera;
	private Context context;
	private static final int NUMBER_OF_FOTOS = 4;
	private int counter = 1;
	private Runnable takePicture = new Runnable() {
		
		@Override
		public void run() {
			darken();
			switch (counter) {
			case 1:
				lo.setVisibility(View.VISIBLE);
				break;
			case 2:
				ro.setVisibility(View.VISIBLE);
				break;
			case 3:
				lu.setVisibility(View.VISIBLE);
				break;
			case 4:
				ru.setVisibility(View.VISIBLE);
				break;
			}
			camera.startPreview();
			camera.takePicture(shutterCallback(), rawCallback(), new PictureCallback() {
				
				@Override
				public void onPictureTaken(byte[] data, Camera camera) {
					Log.d(TAG, "jpegCallBack()");
					if (counter < NUMBER_OF_FOTOS) {
						counter++;
						handleRunnable.post(takePicture);
					} else {
						camera.release();
						Intent main = new Intent(context, MainActivity.class);
						startActivity(main);
					}
				}
			});
		}
	};
	private Handler handleRunnable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lights);
		context = this;
		
		lo = (LinearLayout) findViewById(R.id.light_lo);
		ro = (LinearLayout) findViewById(R.id.light_ro);
		lu = (LinearLayout) findViewById(R.id.light_lu);
		ru = (LinearLayout) findViewById(R.id.light_ru);
		
		camera = CameraPreview.getCamera();
		handleRunnable = new Handler();
		handleRunnable.post(takePicture);
	}
	
	private void darken() {
		lo.setVisibility(View.INVISIBLE);
		ro.setVisibility(View.INVISIBLE);
		lu.setVisibility(View.INVISIBLE);
		ru.setVisibility(View.INVISIBLE);
	}
	
	private PictureCallback rawCallback() {
		PictureCallback callback = new PictureCallback() {
			
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				Log.d(TAG, "rawPictureTaken");
			}
		};
		return callback;
	}
	
	private ShutterCallback shutterCallback() {
		ShutterCallback callback = new ShutterCallback() {
			
			@Override
			public void onShutter() {
				Log.d(TAG, "shutterCallback()");	
			}
		};
		return callback;
	}
	
	@Override
	protected void onPause() {
		// TODO Kamera freigeben
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		// TODO Kamera wieder holen
		super.onResume();
	}

}
