package de.hsrm.objectify.camera;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import de.hsrm.objectify.R;

public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
	private CameraPreview cameraPreview;
	private Button triggerPicture;
	private static final int NUMBER_OF_FOTOS = 4;
	private int counter = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);

		cameraPreview = (CameraPreview) findViewById(R.id.camera_surface);
		triggerPicture = (Button) findViewById(R.id.trigger_picture_button);
		triggerPicture.setOnClickListener(onClickListener());
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	private OnClickListener onClickListener() {
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Camera cam = cameraPreview.getCamera();
				cam.takePicture(null, rawCallback(), jpegCallback());
			}
		};
		return listener;
	}
	
	private Camera.PictureCallback rawCallback() {
		PictureCallback callback = new PictureCallback() {
			
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {

			}
		};
		return callback;
	}
	
	private Camera.PictureCallback jpegCallback() {
		PictureCallback callback = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {

			}
		};
		return callback;
	}

	/**
	 * Setting screen brightness. A value of less than 0, the default, means to
	 * use the preferred screen brightness.
	 * 
	 * @param intensity
	 *            value between 0 and 1
	 */
	private void setScreenBrightness(float intensity) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = intensity;
		getWindow().setAttributes(lp);
	}

	
}
