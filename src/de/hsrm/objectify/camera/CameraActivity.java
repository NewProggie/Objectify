package de.hsrm.objectify.camera;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.LightingColorFilter;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.RemoteViews.ActionException;
import de.hsrm.objectify.R;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.database.DatabaseProvider;
import de.hsrm.objectify.rendering.ObjectModel;
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
	private LinearLayout lightContainer, lightOne, lightTwo, shadow, progress;
	private String image_suffix;
	private int counter = 1;
	private Context context;
	private Camera camera;
	private final int NUMBER_OF_PICTURES = 4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		context = this;
		disableActionBar();
		setScreenBrightness(1);
		
		cameraPreview = (CameraPreview) findViewById(R.id.camera_surface);
		lightContainer = (LinearLayout) findViewById(R.id.light_container);
		lightOne = (LinearLayout) findViewById(R.id.light_one);
		lightTwo = (LinearLayout) findViewById(R.id.light_two);

		shadow = (LinearLayout) findViewById(R.id.shadow);
		progress = (LinearLayout) findViewById(R.id.progress);
		triggerPictures = (Button) findViewById(R.id.trigger_picture_button);
		triggerPictures.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				shadow.setVisibility(View.VISIBLE);
				triggerPictures.setVisibility(View.GONE);
				image_suffix = String.valueOf(System.currentTimeMillis());
				setLights();
				takePictures();
			}
		});	
		
		camera = CameraFinder.INSTANCE.open();
		if (camera == null) {
			showToastAndFinish(getString(R.string.no_ffc_was_found));
		} else {
			cameraPreview.setCamera(camera);
		}
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Log.d(TAG, "ACTION_DOWN");
			setLights();
		}
		return super.onTouchEvent(event);
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
		camera.startPreview();
		setLights();
		// a bit of delay, so the display has a chance to illuminate properly
		SystemClock.sleep(100);
		camera.takePicture(null, null, jpegCallback());
	}
	
	/**
	 * Will be called when no front facing camera was found.
	 * 
	 * @param message
	 *            message which will be displayed
	 */
	private void showToastAndFinish(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		finish();
	}
	
	/**
	 * Set up the white spaces on the display to light up different parts whats
	 * in front
	 */
	private void setLights() {
		 if (lightOne.getVisibility() == View.VISIBLE && lightContainer.getOrientation() == LinearLayout.VERTICAL) {
			lightOne.setVisibility(View.INVISIBLE);
			lightTwo.setVisibility(View.VISIBLE);
		} else if (lightTwo.getVisibility() == View.VISIBLE && lightContainer.getOrientation() == LinearLayout.VERTICAL) {
			lightContainer.setOrientation(LinearLayout.HORIZONTAL);
		} else if (lightTwo.getVisibility() == View.VISIBLE
				&& lightContainer.getOrientation() == LinearLayout.HORIZONTAL) {
			lightTwo.setVisibility(View.INVISIBLE);
			lightOne.setVisibility(View.VISIBLE);
		} else {
			lightOne.setVisibility(View.VISIBLE);
			lightContainer.setOrientation(LinearLayout.VERTICAL);
		}
	}
	
	private PictureCallback jpegCallback() {
		PictureCallback callback = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				if (counter >= NUMBER_OF_PICTURES) {
					Log.d(TAG, "Photos taken, calculating object");
					new CalculateModel().execute(image_suffix);
				} else {
					try {
						String path = ExternalDirectory.getExternalImageDirectory() + "/" + image_suffix + "_"
								+ String.valueOf(counter) + ".png";
						FileOutputStream fos = new FileOutputStream(path);
						BufferedOutputStream bos = new BufferedOutputStream(fos);

						Bitmap image = BitmapUtils.createBitmap(data, CameraFinder.pictureSize,
								CameraFinder.imageFormat);
						image.compress(CompressFormat.PNG, 100, bos);
						bos.flush();
						bos.close();
						counter += 1;
						takePictures();
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
		private ObjectModel objectModel;
		
		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			String path = ExternalDirectory.getExternalImageDirectory() + "/" + params[0] + "_1.png";
			Bitmap image = BitmapFactory.decodeFile(path);
			float[] vertices = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f};
			float[] n_vertices = new float[] { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f };
			short[] faces = new short[] { 1,7,5,1,3,7,1,4,3,1,2,4,3,8,7,3,4,8,5,7,8,5,8,6,1,5,6,1,6,2,2,6,8,2,8,4};

			objectModel = new ObjectModel(vertices, n_vertices, faces, image, image_suffix);
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			progress.setVisibility(View.GONE);
			Intent viewObject = new Intent(context, ObjectViewer.class);
			viewObject.putExtra("objectModel", objectModel);
			startActivity(viewObject);
			((Activity) context).finish();
		}
	}
}
