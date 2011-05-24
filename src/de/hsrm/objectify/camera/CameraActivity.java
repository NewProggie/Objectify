package de.hsrm.objectify.camera;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.hsrm.objectify.R;
import de.hsrm.objectify.rendering.ObjectViewer;
import de.hsrm.objectify.ui.BaseActivity;
import de.hsrm.objectify.utils.BitmapUtils;
import de.hsrm.objectify.utils.ExternalDirectory;

/**
 * This activity opens up the camera and processes the shot photos.
 * 
 * @author kwolf001
 * 
 */
public class CameraActivity extends BaseActivity {

	private String TAG = "CameraActivity";
	private CameraPreview cameraPreview;
	private Button triggerPictures;
	private LinearLayout left, right, up, down, shadow, progress;
	private String image_suffix;
	private int counter = 1;
	private Context context;
	public static Camera camera;
	private final int NUMBER_OF_PICTURES = 4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		context = this;
		disableActionBar();
		setScreenBrightness(1);
		
		cameraPreview = (CameraPreview) findViewById(R.id.camera_surface);
		left = (LinearLayout) findViewById(R.id.light_left);
		right = (LinearLayout) findViewById(R.id.light_right);
		up = (LinearLayout) findViewById(R.id.light_up);
		down = (LinearLayout) findViewById(R.id.light_down);
		shadow = (LinearLayout) findViewById(R.id.shadow);
		progress = (LinearLayout) findViewById(R.id.progress);
		triggerPictures = (Button) findViewById(R.id.trigger_picture_button);
		triggerPictures.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				shadow.setVisibility(View.VISIBLE);
				image_suffix = String.valueOf(System.currentTimeMillis());
				takePictures();
			}
		});	
		
		camera = CameraFinder.INSTANCE.open();
		if (camera == null) {
			showToastAndFinish(getString(R.string.no_ffc_was_found));
		}
	}
	
	/**
	 * Set screen brightness. A value of less than 0, the default, means to use
	 * the preferred screen brightness
	 * 
	 * @param intensity
	 *            value between 0 and 1
	 */
	private void setScreenBrightness(float intensity) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = intensity;
		getWindow().setAttributes(lp);
	}
	
	/**
	 * Takes {@code NUMBER_OF_PICTURES} pictures in sequence.
	 */
	private void takePictures() {
		
	}
	
	/**
	 * Will be called when no front facing camera was found.
	 * 
	 * @param message
	 *            message which will be displayed
	 */
	private void showToastAndFinish(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Set up the white spaces on the display to light up different parts whats
	 * in front
	 */
	private void setLights() {
		if (left.getVisibility() == View.VISIBLE) {
			left.setVisibility(View.INVISIBLE);
			right.setVisibility(View.VISIBLE);
		} else if (right.getVisibility() == View.VISIBLE) {
			right.setVisibility(View.INVISIBLE);
			up.setVisibility(View.VISIBLE);
		} else if (up.getVisibility() == View.VISIBLE) {
			up.setVisibility(View.INVISIBLE);
			down.setVisibility(View.VISIBLE);
		} else if (down.getVisibility() == View.VISIBLE) {
			down.setVisibility(View.INVISIBLE);
			left.setVisibility(View.VISIBLE);
		} else {
			left.setVisibility(View.VISIBLE);
		}
	}
	
	private PictureCallback jpegCallback() {
		PictureCallback callback = new PictureCallback() {
			
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				if (counter >= NUMBER_OF_PICTURES) {
					Log.d(TAG, "Photos taken, calculating object");
					new CalculateModel().execute();
				} else {
					try {
						String path = ExternalDirectory.getExternalImageDirectory() + "/" + image_suffix + ".png";
						FileOutputStream fos = new FileOutputStream(path);
						BufferedOutputStream bos = new BufferedOutputStream(fos);
						
						Bitmap image = BitmapUtils.createBitmap(data, CameraFinder.pictureSize, CameraFinder.imageFormat);
						image.compress(CompressFormat.PNG, 100, bos);
						bos.flush();
						bos.close();
					} catch (IOException e) {
						Log.e(TAG, e.getMessage());
					}
				}
			}
		};
		
		return callback;
	}
	
	/**
	 * Calculates <a
	 * href="http://en.wikipedia.org/wiki/Normal_mapping">normalmap</a> and <a
	 * href="http://en.wikipedia.org/wiki/Heightmap">heightmap</a> from shot
	 * photos. The parameters for the AsyncTask are:
	 * <ul>
	 * <li>String: image_suffix for identifying the shot photos stored at the sd
	 * card</li>
	 * <li>Void: We don't need to update any progress by now</li>
	 * <li>Boolean: Indicating whether we were successful calculating an object</li>
	 * </ul>
	 * 
	 * @author kwolf001
	 * 
	 */
	private class CalculateModel extends AsyncTask<String, Void, Boolean> {
		
		private final String TAG = "CalculateModel";
		private String image_suffix;
		
		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			progress.setVisibility(View.GONE);
			Intent viewObject = new Intent(context, ObjectViewer.class);
			startActivity(viewObject);
			((Activity) context).finish();
		}
	}
}
