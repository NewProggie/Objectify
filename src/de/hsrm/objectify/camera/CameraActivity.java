package de.hsrm.objectify.camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.hsrm.objectify.R;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.database.DatabaseProvider;
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

	private static final String TAG = "CameraActivity";
	private CameraPreview cameraPreview;
	private Button triggerPicture;
	private LinearLayout left, right, up, down, shadow, progress;
	/**
	 * used for saving shot photos onto the sd using the same suffix for later identifying.
	 */
	private String image_suffix;
	private Context context;
	public static Camera camera;
	private CameraFinder cameraFinder;
	private int counter = 1;
	private static final int NUMBER_OF_PICTURES = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		setContentView(R.layout.camera);
		context = this;
		disableActionBar();
		
		cameraPreview = (CameraPreview) findViewById(R.id.camera_surface);
		cameraFinder = CameraFinder.INSTANCE;
		left = (LinearLayout) findViewById(R.id.light_left);
		right = (LinearLayout) findViewById(R.id.light_right);
		up = (LinearLayout) findViewById(R.id.light_up);
		down = (LinearLayout) findViewById(R.id.light_down);
		shadow = (LinearLayout) findViewById(R.id.shadow);
		progress = (LinearLayout) findViewById(R.id.progress);
		triggerPicture = (Button) findViewById(R.id.trigger_picture_button);
		triggerPicture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				shadow.setVisibility(View.VISIBLE);
				image_suffix = String.valueOf(System.currentTimeMillis());
				takePictures();
			}
		});
		
		setScreenBrightness(1);
		
		camera = cameraFinder.open();
		if (camera == null)
			showToastAndFinish(getString(R.string.no_ffc_was_found));
	}

	/**
	 * Setting screen brightness. A value of less than 0, the default, means to
	 * use the preferred screen brightness.
	 * 
	 * @param intensity
	 *            value between 0 and 1, where 0 is darkness and 1 is brightness
	 */
	private void setScreenBrightness(float intensity) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = intensity;
		getWindow().setAttributes(lp);
	}
	
	/**
	 * Takes several pictures in sequence. 
	 */
	private void takePictures() {
		camera.startPreview();
		setLightning();
		// a bit of delay, so the display has a chance to illuminate properly
		SystemClock.sleep(100);
		camera.takePicture(null, null, jpegCallback());
	}
	
	/**
	 * Sets up the white spaces on the screen to light up different parts of the
	 * object
	 */
	private void setLightning() {
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
			darken();
			left.setVisibility(View.VISIBLE);
		}
		
	}
	
	/**
	 * Makes the whole screen black.
	 */
	private void darken() {
		left.setVisibility(View.INVISIBLE);
		right.setVisibility(View.INVISIBLE);
		up.setVisibility(View.INVISIBLE);
		down.setVisibility(View.INVISIBLE);
	}
	
	private void showToastAndFinish(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		finish();
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
						
						Bitmap image = BitmapUtils.createBitmap(data, cameraFinder.pictureSize, cameraFinder.imageFormat);
						image.compress(CompressFormat.PNG, 100, bos);
						bos.flush();
						bos.close();
						long length = new File(path).length();
						writeToDatabase(path, length, 0, 0, cameraFinder.pictureSize.toString());
					} catch (IOException e) {
						Log.e(TAG, e.getMessage());
					}
					
				}
			}
		};

		return callback;
	}
	
	private void writeToDatabase(String imagePath, long size, int faces, int vertices, String dimensions) {
		ContentValues values = new ContentValues();
		values.put(DatabaseAdapter.GALLERY_IMAGE_PATH_KEY, imagePath);
		values.put(DatabaseAdapter.GALLERY_SIZE_KEY, String.valueOf(size));
		values.put(DatabaseAdapter.GALLERY_FACES_KEY, String.valueOf(faces));
		values.put(DatabaseAdapter.GALLERY_VERTICES_KEY, String.valueOf(vertices));
		values.put(DatabaseAdapter.GALLERY_DIMENSIONS_KEY, dimensions);
		values.put(DatabaseAdapter.GALLERY_DATE_KEY, String.valueOf(Calendar.getInstance().getTimeInMillis()));
		getContentResolver().insert(DatabaseProvider.CONTENT_URI.buildUpon().appendPath("gallery").build(), values);
	}
	
	/**
	 * Calculates normalmap and heightmap from given photos. The parameters
	 * for the AsyncTask are: <ul>
	 * <li>String: Path to an image used for texture</li>
	 * <li>Void: We use the static {@link CompositePicture} for memory reasons</li>
	 * <li>Boolean: Indicating whether we were successful or not</li></ul>
	 * 
	 * @author kwolf001
	 * 
	 */
	private class CalculateModel extends AsyncTask<Void, Void, Boolean> {
		
		private static final String TAG = "CalculateModel";
		private String path;
		private ContentResolver cr;
		
		@Override
		protected void onPreExecute() {
			darken();
			progress.setVisibility(View.VISIBLE);
			cr = getContentResolver();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			return true;
		}
		
		
		
		@Override
		protected void onPostExecute(Boolean result) {
			progress.setVisibility(View.GONE);
			Intent objectViewer = new Intent(context, ObjectViewer.class);
			objectViewer.putExtra("image_path", path);
			startActivity(objectViewer);
			((Activity) context).finish();
		}
		

	}
	
}
