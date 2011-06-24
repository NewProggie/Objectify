package de.hsrm.objectify.camera;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.hsrm.objectify.R;
import de.hsrm.objectify.rendering.ObjectModel;
import de.hsrm.objectify.rendering.ObjectViewerActivity;
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
	private LinearLayout progress;
	private CameraLighting cameraLighting;
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
		cameraLighting = (CameraLighting) findViewById(R.id.camera_lighting);		
		progress = (LinearLayout) findViewById(R.id.progress);
		triggerPictures = (Button) findViewById(R.id.trigger_picture_button);
		triggerPictures.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
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
		cameraLighting.setVisibility(View.VISIBLE);
		cameraLighting.setZOrderOnTop(true);
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
			cameraLighting.setVisibility(View.GONE);
			cameraLighting.setZOrderOnTop(false);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			String path = ExternalDirectory.getExternalImageDirectory() + "/" + params[0] + "_1.png";
			Bitmap image = BitmapFactory.decodeFile(path);
			float[] vertices = new float[] { -1.0f, -1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, -1.0f, 0.0f };
			float[] n_vertices = new float[] { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f };
			short[] faces = new short[] { 1,2,3,4,5,6 };

			objectModel = new ObjectModel(vertices, n_vertices, faces, image, image_suffix);
			SystemClock.sleep(2000);
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			progress.setVisibility(View.GONE);
			Intent viewObject = new Intent(context, ObjectViewerActivity.class);
			Bundle b = new Bundle();
			b.putParcelable("objectModel", objectModel);
			viewObject.putExtra("bundle", b);
			startActivity(viewObject);
			((Activity) context).finish();
		}
	}
}
