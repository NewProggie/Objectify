package de.hsrm.objectify.camera;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
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
import de.hsrm.objectify.math.Matrix;
import de.hsrm.objectify.math.Vector3f;
import de.hsrm.objectify.math.VectorNf;
import de.hsrm.objectify.rendering.Circle;
import de.hsrm.objectify.rendering.ObjectModel;
import de.hsrm.objectify.rendering.ObjectViewerActivity;
import de.hsrm.objectify.ui.BaseActivity;
import de.hsrm.objectify.utils.BitmapUtils;
import de.hsrm.objectify.utils.ExternalDirectory;
import de.hsrm.objectify.utils.Image;
import de.hsrm.objectify.utils.MathHelper;

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
	private ArrayList<Image> pictureList;
	private int numberOfPictures;
	private int counter = 0;
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
				/* New ArrayList for storing the pictures temporarily */
				pictureList = new ArrayList<Image>();
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
		SystemClock.sleep(200);
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
		cameraLighting.putLightSource(numberOfPictures, counter);
	}
	
	/**
	 * This function is just for debugging and should be deleted before publishing
	 * @param data byte data from camera callback
	 * @param suffix suffix which will be appended to the file name for better distinguish
	 */
	private void storeOnSD(byte[] data, int suffix) {
		Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
		try {
			String id = String.valueOf(SystemClock.elapsedRealtime());
			FileOutputStream out = new FileOutputStream(ExternalDirectory.getExternalImageDirectory()+"/"+id+"pic"+numberOfPictures+"_"+suffix+".png");
			BufferedOutputStream bos = new BufferedOutputStream(out);
			bmp.compress(CompressFormat.PNG, 100, out);
			bos.flush();
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private PictureCallback jpegCallback() {
		PictureCallback callback = new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
//				storeOnSD(data, counter);
				Image image = new Image(BitmapUtils.createScaledBitmap(data, CameraFinder.pictureSize, CameraFinder.imageFormat, 8.0f), true);
				pictureList.add(image);
				counter += 1;
				if (counter == numberOfPictures) {
					Log.d(TAG, "Photos taken, calculating object");
					new CalculateModel().execute(); 
				} else {
					takePictures();
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
	 * <li>Void: all pictures will be held by the {@link CameraActivity}.</li>
	 * <li>Void: We don't need to update any progress by now</li>
	 * <li>Boolean: Indicating whether we were successful calculating an object</li>
	 * </ul>
	 * 
	 * @author kwolf001
	 * 
	 */
	private class CalculateModel extends AsyncTask<Void, Void, Boolean> {

		private ObjectModel objectModel;

		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
			cameraLighting.setVisibility(View.GONE);
			cameraLighting.setZOrderOnTop(false);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Matrix sMatrix = cameraLighting.getLightMatrixS(numberOfPictures);
			Matrix sInverse = sMatrix.pseudoInverse();
			
			// TODO: Debugging wieder rausnehmen
			pictureList = new ArrayList<Image>();
			AssetManager assetManager = getAssets();
			try {
//				InputStream is1 = assetManager.open("86629pic4_0.png");
				InputStream is2 = assetManager.open("bunny_1.png");
				InputStream is3 = assetManager.open("bunny_2.png");
				InputStream is4 = assetManager.open("bunny_3.png");
//				Image img1 = new Image(BitmapFactory.decodeStream(is1), false);
				Image img2 = new Image(BitmapFactory.decodeStream(is2), false);
				Image img3 = new Image(BitmapFactory.decodeStream(is3), false);
				Image img4 = new Image(BitmapFactory.decodeStream(is4), false);
//				pictureList.add(img1);
				pictureList.add(img2);
				pictureList.add(img3);
				pictureList.add(img4);
			} catch (IOException e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
			////////
			
			int imageWidth = pictureList.get(0).getWidth();
			int imageHeight = pictureList.get(0).getHeight();

			ArrayList<Vector3f> normalField = new ArrayList<Vector3f>();
			Matrix pGradients = new Matrix(imageHeight, imageWidth);
			Matrix qGradients = new Matrix(imageHeight, imageWidth);
			
			for (int h=0; h<imageHeight; h++) {
				for (int w=0; w<imageWidth; w++) {
					Vector3f normal = new Vector3f();
					VectorNf intensity = new VectorNf(numberOfPictures);
					for (int i=0; i<numberOfPictures; i++) {
						intensity.set(i, pictureList.get(i).getIntensity(w, h));
					}
					Vector3f albedo = sInverse.multiply(intensity);
					
					float reg = (float) Math.sqrt(Math.pow(albedo.x, 2) + Math.pow(albedo.y, 2) + Math.pow(albedo.z, 2));
					normal.x = albedo.x / reg;
					normal.y = albedo.y / reg;
					normal.z = albedo.z / reg;

					normalField.add(normal);
					pGradients.set(h, w, normal.x / normal.z);
					qGradients.set(h, w, normal.y / normal.z);
				}
			}
			
			double[][] heightField = MathHelper.twoDimIntegration(pGradients, qGradients, imageHeight, imageWidth);
						
			FloatBuffer vertBuffer = FloatBuffer.allocate(imageHeight*imageWidth*3);
			FloatBuffer normBuffer = FloatBuffer.allocate(imageHeight*imageWidth*3);
			ArrayList<Short> indexes = new ArrayList<Short>();
			vertBuffer.rewind();
			normBuffer.rewind();
			// Vertices und Normale
			int idx = 0;
			for (int x=0;x<imageHeight;x++) {
				for (int y=0;y<imageWidth;y++) {
					float[] imgPoint = new float[] { Float.valueOf(y), Float.valueOf(x), (float) heightField[x][y] };
					float[] normVec = new float[] { normalField.get(idx).x, normalField.get(idx).y, normalField.get(idx).z };
					vertBuffer.put(imgPoint);
					normBuffer.put(normVec);
					idx += 1;
				}
			}
			// Faces
			for (int i=0; i<imageHeight-1;i++) {
				for (int j=0; j<imageWidth-1;j++) {
					short index = (short) (j + (i*imageWidth));
					indexes.add((short) (index));
					indexes.add((short) (index+imageWidth));
					indexes.add((short) (index+1));
					
					indexes.add((short) (index+1));
					indexes.add((short) (index+imageWidth));
					indexes.add((short) (index+imageWidth+1));
				}
			}
			ShortBuffer indexBuffer = ShortBuffer.allocate(indexes.size());
			indexBuffer.rewind();
			for (int i=0; i<indexes.size(); i++) {
				indexBuffer.put(indexes.get(i));
			}
			
			float[] vertices = new float[vertBuffer.limit()];
			float[] normals = new float[normBuffer.limit()];
			short[] faces = new short[indexBuffer.limit()];
			vertices = vertBuffer.array();
			normals = normBuffer.array();
			faces = indexBuffer.array();

			objectModel = new ObjectModel(vertices, normals, faces, pictureList.get(0));
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
