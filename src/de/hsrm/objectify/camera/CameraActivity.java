package de.hsrm.objectify.camera;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import Jama.Matrix;
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
import de.hsrm.objectify.math.Vector3f;
import de.hsrm.objectify.rendering.Circle;
import de.hsrm.objectify.rendering.ObjectModel;
import de.hsrm.objectify.rendering.ObjectViewerActivity;
import de.hsrm.objectify.ui.BaseActivity;
import de.hsrm.objectify.utils.BitmapUtils;
import de.hsrm.objectify.utils.ExternalDirectory;
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
	private ArrayList<Bitmap> pictureList;
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
				pictureList = new ArrayList<Bitmap>();
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
		switch (counter) {
		case 0:
			cameraLighting.putLightSource(-2, -2);
			break;
		case 1:
			cameraLighting.putLightSource(2, -2);
			break;
		case 2:
			cameraLighting.putLightSource(2, 2);
			break;
		case 3:
			cameraLighting.putLightSource(-2, 2);
			break;
		}
	}

	private PictureCallback jpegCallback() {
		PictureCallback callback = new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				if (counter == (numberOfPictures-1)) {
					Log.d(TAG, "Photos taken, calculating object");
					new CalculateModel().execute();
				} else {
//					try {
//						String path = ExternalDirectory.getExternalImageDirectory() + "/" + image_suffix + "_" + String.valueOf(counter) + ".png";
//						FileOutputStream fos = new FileOutputStream(path);
//						BufferedOutputStream bos = new BufferedOutputStream(fos);

						Bitmap image = BitmapUtils.createScaledBitmap(data, CameraFinder.pictureSize, CameraFinder.imageFormat, 8.0f);
						pictureList.add(image);
//						image.compress(CompressFormat.PNG, 100, bos);
//						bos.flush();
//						bos.close();
						counter += 1;
						takePictures();
//					} catch (IOException e) {
//						Log.e(TAG, e.getMessage());
//					}
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
	private class CalculateModel extends AsyncTask<Void, Void, Boolean> {

		private final String TAG = "CalculateModel";
		private ObjectModel objectModel;

		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
			cameraLighting.setVisibility(View.GONE);
			cameraLighting.setZOrderOnTop(false);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			
//			String path1 = ExternalDirectory.getExternalImageDirectory() + "/"
//					+ params[0] + "_0.png";
//			Bitmap image1 = BitmapFactory.decodeFile(path1);
//			String path2 = ExternalDirectory.getExternalImageDirectory() + "/"
//			+ params[0] + "_1.png";
//			Bitmap image2 = BitmapFactory.decodeFile(path2);
//			String path3 = ExternalDirectory.getExternalImageDirectory() + "/"
//			+ params[0] + "_2.png";
//			Bitmap image3 = BitmapFactory.decodeFile(path3);
//			String path4 = ExternalDirectory.getExternalImageDirectory() + "/"
//			+ params[0] + "_3.png";
//			Bitmap image4 = BitmapFactory.decodeFile(path3);
			
			double[][] sValues = new double[4][3];
			sValues[0][0] = 2; 
			sValues[0][1] = 2;
			sValues[0][2] = 0;
			sValues[1][0] = 0;
			sValues[1][1] = 2;
			sValues[1][2] = 0;
			sValues[2][0] = 0;
			sValues[2][1] = 0;
			sValues[2][2] = 0;
			sValues[3][0] = 2;
			sValues[3][1] = 0;
			sValues[3][2] = 0;
			Matrix sMatrix = new Matrix(sValues);
			Matrix sInverse = MathHelper.pinv(sMatrix);

			int imageWidth = pictureList.get(0).getWidth();
			int imageHeight = pictureList.get(0).getHeight();
			
			// Normalenfeld berechnen
			ArrayList<Vector3f> normalField = new ArrayList<Vector3f>();
			Matrix pGradients = new Matrix(imageHeight, imageWidth);
			Matrix qGradients = new Matrix(imageHeight, imageWidth);
			
			for (int h=0; h<imageHeight; h++) {
				for (int w=0; w<imageWidth; w++) {
					Vector3f normal = new Vector3f();
					
					Vector3f intensity = new Vector3f();
					int color1 = pictureList.get(0).getPixel(w, h);
					int red1 = (color1 >> 16) & 0xFF;
					int green1 = (color1 >> 8) & 0xFF;
					int blue1 = (color1 >> 0) & 0xFF;
					intensity.x = ((red1 + green1 + blue1) / 3.0f) / 255.0f;
					//
					int color2 = pictureList.get(1).getPixel(w, h);
					int red2 = (color2 >> 16) & 0xFF;
					int green2 = (color2 >> 8) & 0xFF;
					int blue2 = (color2 >> 0) & 0xFF;
					intensity.y = ((red2 + green2 + blue2) / 3.0f) / 255.0f;
					//
					int color3 = pictureList.get(2).getPixel(w, h);
					int red3 = (color3 >> 16) & 0xFF;
					int green3 = (color3 >> 8) & 0xFF;
					int blue3 = (color3 >> 0) & 0xFF;
					intensity.z = ((red3 + green3 + blue3) / 3.0f) / 255.0f;
					
					Vector3f albedo = new Vector3f();
					albedo.x = (float) (sInverse.get(0, 0) * intensity.x + sInverse.get(0, 1) * intensity.y + sInverse.get(0, 2) * intensity.z);
					albedo.y = (float) (sInverse.get(1, 0) * intensity.x + sInverse.get(1, 1) * intensity.y + sInverse.get(1, 2) * intensity.z);
					albedo.z = (float) (sInverse.get(2, 0) * intensity.x + sInverse.get(2, 1) * intensity.y + sInverse.get(2, 2) * intensity.z);
					
					float reg = (float) Math.sqrt(Math.pow(albedo.x, 2) + Math.pow(albedo.y, 2) + Math.pow(albedo.z, 2));
					normal.x = albedo.x / reg;
					normal.y = albedo.y / reg;
					// TODO: DEbugging, rausnehmen
					Double r = Math.random();
					normal.z = r.floatValue();
//					normal.z = albedo.z / reg;

					normalField.add(normal);
					pGradients.set(h, w, normal.x / normal.z);
					qGradients.set(h, w, normal.y / normal.z);
				}
			}
			
			double[][] heightField = MathHelper.twoDimIntegration(pGradients, qGradients, imageHeight, imageWidth);
			
			// Drei Vertices pro Bildpunkt (x,y,z)
			
			FloatBuffer vertBuffer = FloatBuffer.allocate(imageHeight*imageWidth*3);
			FloatBuffer normBuffer = FloatBuffer.allocate(imageHeight*imageWidth*3);
			ArrayList<Short> indexes = new ArrayList<Short>();
			vertBuffer.rewind();
			normBuffer.rewind();
			// Vertices und Normale
			for (int x=0;x<imageHeight;x++) {
				for (int y=0;y<imageWidth;y++) {
					Double d = heightField[x][y];
					Log.d("heightField[x][y]", String.valueOf(d));
					float[] imgPoint = new float[] { Float.valueOf(y), Float.valueOf(x), d.floatValue() };
					float[] normVec = new float[] { 0.0f, 0.0f, 1.0f };
					vertBuffer.put(imgPoint);
					normBuffer.put(normVec);
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
