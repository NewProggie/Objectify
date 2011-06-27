package de.hsrm.objectify.camera;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
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
import de.hsrm.objectify.SettingsActivity;
import de.hsrm.objectify.rendering.ObjectModel;
import de.hsrm.objectify.rendering.ObjectViewerActivity;
import de.hsrm.objectify.ui.BaseActivity;
import de.hsrm.objectify.utils.BitmapUtils;
import de.hsrm.objectify.utils.ExternalDirectory;

/**
 * This {@link Activity} shoots photos with the front facing camera, manages
 * light settings with {@link CameraLighting} and calculates the normal- and
 * height map from the given photos.
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
	private int numberOfPictures;
	private int counter = 1;
	private Context context;
	private Camera camera;

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
				/* Fetching number of pictures to shoot from the settings */
				ContextWrapper contextWrapper = new ContextWrapper(context);
				SharedPreferences prefs = SettingsActivity.getSettings(contextWrapper);
				numberOfPictures = prefs.getInt(getString(R.string.settings_amount_pictures), 4);
				/* Setting the trigger button to invisible */
				triggerPictures.setVisibility(View.GONE);
				/* Creating a suffix for the image file names, so we can find them again later */
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
	protected void onPause() {
		super.onPause();
		setScreenBrightness(-1);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setScreenBrightness(1);
	}

	/**
	 * Set screen brightness. A value of less than 0, the default, means to use
	 * the preferred screen brightness. 0 to 1 adjusts the brightness from dark
	 * to full bright.
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
	 * Takes {@code numberOfPictures} pictures in sequence.
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
	 * Displays a white {@link Circle} on the display for lighting up different
	 * parts of the object. As a standard behavior this function looks up the
	 * number of pictures which will be taken and moves the lighting source in a
	 * circle around the display.
	 */
	private void setLights() {
		cameraLighting.setVisibility(View.VISIBLE);
		cameraLighting.setZOrderOnTop(true);
		switch (counter) {
		case 1:
			cameraLighting.putLightSource(-2, -2);
			break;
		case 2:
			cameraLighting.putLightSource(2, -2);
			break;
		case 3:
			cameraLighting.putLightSource(2, 2);
			break;
		case 4:
			cameraLighting.putLightSource(-2, 2);
			break;
		}
	}

	private PictureCallback jpegCallback() {
		PictureCallback callback = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				if (counter >= numberOfPictures) {
					Log.d(TAG, "Photos taken, calculating object");
					new CalculateModel().execute(image_suffix);
				} else {
					try {
						String path = ExternalDirectory
								.getExternalImageDirectory()
								+ "/"
								+ image_suffix
								+ "_"
								+ String.valueOf(counter)
								+ ".png";
						FileOutputStream fos = new FileOutputStream(path);
						BufferedOutputStream bos = new BufferedOutputStream(fos);

						Bitmap image = BitmapUtils.createBitmap(data,
								CameraFinder.pictureSize,
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
	 * Calculates <a href="http://en.wikipedia.org/wiki/Normal_mapping">normalmap</a>  
	 * and <a href="http://en.wikipedia.org/wiki/Heightmap">heightmap</a> from shot
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
			String path = ExternalDirectory.getExternalImageDirectory() + "/"
					+ params[0] + "_1.png";
			Bitmap image = BitmapFactory.decodeFile(path);
			// Drei Vertices pro Bildpunkt (x,y,z)
//			int imageWidth = 12;
//			int imageHeight = 12;
//			FloatBuffer vertBuffer = FloatBuffer.allocate(imageHeight*imageWidth*3);
//			FloatBuffer normBuffer = FloatBuffer.allocate(imageHeight*imageWidth*3);
//			ShortBuffer indexBuffer = ShortBuffer.allocate(imageHeight*imageWidth*2);
//			vertBuffer.rewind();
//			normBuffer.rewind();
//			indexBuffer.rewind();
			// Vertices und Normale
//			for (int x=0;x<imageHeight;x++) {
//				for (int y=0;y<imageWidth;y++) {
//					float[] imgPoint = new float[] { Float.valueOf(x), Float.valueOf(y), 0.0f };
//					float[] normVec = new float[] { 0.0f, 0.0f, 1.0f };
//					vertBuffer.put(imgPoint);
//					normBuffer.put(normVec);
//				}
//			}
			// Faces
//			for (int i=0;i<indexBuffer.limit();i++) {
//				indexBuffer.put((short) i);
//			}
//			float[] vertices = new float[vertBuffer.limit()];
//			float[] normals = new float[normBuffer.limit()];
//			short[] faces = new short[indexBuffer.limit()];
//			vertices = vertBuffer.array();
//			normals = normBuffer.array();
//			faces = indexBuffer.array();
			// DEBUGGING
			float[] vertices = new float[] { -1.0f,  1.0f, -0.0f, 1.0f,  1.0f, -0.0f, -1.0f, -1.0f, -0.0f, 1.0f, -1.0f, -0.0f };
			float[] normals = new float[12];
			for (int i=0;i<12;i++) {
				Double r = Math.random();
				normals[i] = r.floatValue();
			}
//			float[] normals = new float[] { 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f };
			short[] faces = new short[] {0, 1, 1, 1, 0, 0, 1, 0 };

			objectModel = new ObjectModel(vertices, normals, faces, image, image_suffix);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Intent viewObject = new Intent(context, ObjectViewerActivity.class);
			Bundle b = new Bundle();
			b.putParcelable("objectModel", objectModel);
			viewObject.putExtra("bundle", b);
			startActivity(viewObject);
			((Activity) context).finish();
		}
	}

}
