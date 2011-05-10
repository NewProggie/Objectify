package de.hsrm.objectify.camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import de.hsrm.objectify.MainActivity;
import de.hsrm.objectify.R;
import de.hsrm.objectify.utils.ExternalDirectory;

public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
	private CameraPreview cameraPreview;
	private Button triggerPicture;
	private LinearLayout left, right, up, down, shadow, progress;
	private CompositePicture pic;
	private int counter = 1;
	private Context context;
	private static final int NUMBER_OF_PICTURES = 4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		context = this;
		
		cameraPreview = (CameraPreview) findViewById(R.id.camera_surface);
		left = (LinearLayout) findViewById(R.id.light_left);
		right = (LinearLayout) findViewById(R.id.light_right);
		up = (LinearLayout) findViewById(R.id.light_up);
		down = (LinearLayout) findViewById(R.id.light_down);
		shadow = (LinearLayout) findViewById(R.id.shadow);
		progress = (LinearLayout) findViewById(R.id.progress);
		pic = new CompositePicture();
		triggerPicture = (Button) findViewById(R.id.trigger_picture_button);
		triggerPicture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				shadow.setVisibility(View.VISIBLE);
				takePictures();
			}
		});
		
		setScreenBrightness(1);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
	
	private void takePictures() {
		cameraPreview.startPreview();
		setLightning();
		SystemClock.sleep(1000);
		cameraPreview.takePicture(null, null, jpegCallback());
	}
	
	private void setLightning() {
		switch (counter) {
		case 1:
			left.setVisibility(View.VISIBLE);
			break;
		case 2:
			left.setVisibility(View.INVISIBLE);
			right.setVisibility(View.VISIBLE);
			break;
		case 3:
			right.setVisibility(View.INVISIBLE);
			up.setVisibility(View.VISIBLE);
			break;
		case 4:
			up.setVisibility(View.INVISIBLE);
			down.setVisibility(View.VISIBLE);
			break;
		default:
			darken();
		}
		
	}
	
	private void darken() {
		left.setVisibility(View.INVISIBLE);
		right.setVisibility(View.INVISIBLE);
		up.setVisibility(View.INVISIBLE);
		down.setVisibility(View.INVISIBLE);
	}
	
	private PictureCallback jpegCallback() {
		PictureCallback callback = new PictureCallback() {
			
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				switch (counter) {
				case 1:
					pic.setLeft(data);
					break;
				case 2:
					pic.setRight(data);
					break;
				case 3:
					pic.setUp(data);
					break;
				case 4:
					pic.setDown(data);
					break;
				}
				if (counter < NUMBER_OF_PICTURES) {
					counter++;
					takePictures();
				} else {
					Log.d("Alles fertig", "mit der Cam");
					new Calc3DObject().execute(pic);
				}
			}
		};

		return callback;
	}
	
	private class Calc3DObject extends AsyncTask<CompositePicture, Void, Boolean> {
		
		private static final String TAG = "Calc3DObject";
		
		@Override
		protected void onPreExecute() {
			darken();
			progress.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Boolean doInBackground(CompositePicture... params) {
			CompositePicture pic = params[0];
			Bitmap image = Bitmap.createBitmap(convertByteArray(pic.getUp()), 800, 600, Config.RGB_565);
			try {
				FileOutputStream fos = new FileOutputStream(ExternalDirectory.getExternalDirectory() + "/foo.png");
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				image.compress(CompressFormat.PNG, 100, bos);
				bos.flush();
				bos.close();
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			SystemClock.sleep(1000);
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			progress.setVisibility(View.GONE);
			Intent main = new Intent(context, MainActivity.class);
			startActivity(main);
		}
		
		public int[] convertByteArray(byte[] byteArray) {
			int[] intArray = new int[((int) byteArray.length/4)];
			int idx = 0;
			for (int i=16; i<byteArray.length;i+=4) {
				intArray[idx] = (byteArray[i] & 0xFF) << 24 +
								(byteArray[i+1] & 0xFF) << 16 +
								(byteArray[i+2] & 0xFF) << 8 +
								(byteArray[i+3] & 0xFF) << 0;
				idx += 1;
			}
			return intArray;
		}
	}
	
}
