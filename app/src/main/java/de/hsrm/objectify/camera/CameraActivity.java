package de.hsrm.objectify.camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.SingularValueDecomposition;

import de.hsrm.objectify.R;
import de.hsrm.objectify.SettingsActivity;
import de.hsrm.objectify.rendering.Circle;
import de.hsrm.objectify.rendering.ObjectModel;
import de.hsrm.objectify.utils.ExternalDirectory;
import de.hsrm.objectify.utils.Image;

/**
 * This {@link Activity} shoots photos with the front facing camera, manages
 * light settings with {@link CameraLighting} and calculates the normal- and
 * height map from the given photos.
 * 
 * @author kwolf001
 * 
 */
public class CameraActivity extends Activity {

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
	private Image texture;
	public static Runnable shootPicture;
	public static Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		context = this;
		setScreenBrightness(1);

		cameraPreview = (CameraPreview) findViewById(R.id.camera_surface);
		cameraLighting = (CameraLighting) findViewById(R.id.camera_lighting);
		cameraLighting.setZOrderOnTop(true);
		progress = (LinearLayout) findViewById(R.id.progress);
		triggerPictures = (Button) findViewById(R.id.trigger_picture_button);
		triggerPictures.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/* Fetching number of pictures to shoot from the settings */
				ContextWrapper contextWrapper = new ContextWrapper(context);
				SharedPreferences prefs = SettingsActivity
						.getSettings(contextWrapper);
				numberOfPictures = prefs.getInt(
						getString(R.string.settings_amount_pictures), 4);
				/* Setting the trigger button to invisible */
				triggerPictures.setVisibility(View.GONE);
				/* New ArrayList for storing the pictures temporarily */
				pictureList = new ArrayList<Image>();
				setLights();
			}
		});

		camera = CameraFinder.INSTANCE.open(context);
		if (camera == null) {
			showToastAndFinish(getString(R.string.no_ffc_was_found));
		} else {
			cameraPreview.setCamera(camera);
		}

		handler = new Handler();
		shootPicture = new Runnable() {

			@Override
			public void run() {
				camera.startPreview();
				camera.takePicture(null, null, jpegCallback());
			}
		};

	}

	@Override
	protected void onPause() {
		super.onPause();
		setScreenBrightness(-1);
		((Activity) context).finish();
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

	private PictureCallback jpegCallback() {
		PictureCallback callback = new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {

                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inSampleSize = 5;
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                Image image;

                if(bmp.getHeight() < bmp.getWidth()) {
                    android.graphics.Matrix m = new android.graphics.Matrix();
                    m.postRotate(270);
                    Bitmap bmpRot = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
                    image = new Image(bmpRot);
                } else {
                    image = new Image(bmp);
                }

				pictureList.add(image);
				counter += 1;

				if (counter == numberOfPictures) {
					new CalculateModel().execute();
				} else {
					setLights();
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
		private ContentResolver cr;
		private boolean sdIsMounted = true;
		private boolean useBlurring = false;

		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
			cameraLighting.setVisibility(View.GONE);
			cameraLighting.setZOrderOnTop(false);
			cr = getContentResolver();
			SharedPreferences settings = SettingsActivity
					.getSettings((ContextWrapper) context);
			useBlurring = settings.getBoolean(
					getString(R.string.settings_use_blurring), false);
		}

        Bitmap computeNormals(ArrayList<Image> pictureList) {

            int imageWidth = pictureList.get(0).getWidth();
            int imageHeight = pictureList.get(0).getHeight();
            int numPics = pictureList.size();

            /* populate A */
            double[][] a = new double[imageHeight*imageWidth][numPics];

            for (int k = 0; k < numPics; k++) {
                int idx = 0;
                for (int i = 0; i < imageHeight; i++) {
                    for (int j = 0; j < imageWidth; j++) {
                        a[idx++][k] = pictureList.get(k).getIntensity(j,i);
                    }
                }
            }

            DenseMatrix64F A = new DenseMatrix64F(a);
            SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows, A.numCols, true, true, true);
            if( !svd.decompose(A) )
                throw new RuntimeException("Decomposition failed");

            DenseMatrix64F U = svd.getU(null,false);
            Bitmap S = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.RGB_565);
            int idx = 0;
            for (int i = 0; i < imageHeight; i++) {
                for (int j = 0; j < imageWidth; j++) {

                    double rSxyz = 1.0f / Math.sqrt( Math.pow(U.get(idx, 0), 2) + Math.pow(U.get(idx, 1), 2) + Math.pow(U.get(idx, 2), 2) );

				    /* U contanis eigenvectors of AAT, corresponding to z,x,y components of each pixels surface normal */
                    int sz = (int) (128.0f + 127.0f * Math.signum(U.get(idx, 0)) * Math.abs(U.get(idx, 0)) * rSxyz);
                    int sx = (int) (128.0f + 127.0f * Math.signum(U.get(idx, 1)) * Math.abs(U.get(idx, 1)) * rSxyz);
                    int sy = (int) (128.0f + 127.0f * Math.signum(U.get(idx, 2)) * Math.abs(U.get(idx, 2)) * rSxyz);
                    S.setPixel(j, i, Color.rgb(sx, sy, sz));
                    idx += 1;
                }
            }

            return S;
        }

        Bitmap localHeightfield(Bitmap Normals) {

            int height = Normals.getHeight();
            int width = Normals.getWidth();
            Image Z = new Image(Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565));
            for (int k = 0; k < 300; k++) {
                for (int i = 1; i < height-1; i++) {
                    for (int j = 1; j < width-1; j++) {
                        float zU = Z.getIntensity(j,i);
                        float zD = Z.getIntensity(j,i+1);
                        float zL = Z.getIntensity(j-1,i);
                        float zR = Z.getIntensity(j+1,i);
                        float nxC = Color.red(Normals.getPixel(j,i));
                        float nyC = Color.green(Normals.getPixel(j,i));
                        float nxU = Color.red(Normals.getPixel(j,i-1));
                        float nyL = Color.green(Normals.getPixel(j-1,i));
                        int intens = (int) (1.0f/4.0f * (zD + zU + zR + zL + nxU - nxC + nyL - nyC));
                        Z.setPixel(j, i, Color.rgb(intens, intens, intens));
                    }
                }
            }

            return Z.getBitmap();
        }

		@Override
		protected Boolean doInBackground(Void... params) {

            Bitmap S = computeNormals(pictureList);
            Bitmap Result = localHeightfield(S);

			if (ExternalDirectory.isMounted()) {
				try {
                    /* temp debugging normalmap */
                    FileOutputStream fos = new FileOutputStream(ExternalDirectory.getExternalImageDirectory()+"/export.png");
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    Result.compress(CompressFormat.PNG, 100, bos);
                    bos.flush();
                    bos.close();
				} catch (FileNotFoundException e) {
					Log.e(TAG, e.getLocalizedMessage());
					e.printStackTrace();
					return false;
				} catch (IOException e) {
					Log.e(TAG, e.getLocalizedMessage());
					e.printStackTrace();
					return false;
				}
			} else {
				sdIsMounted = false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean createdSuccessfully) {
			if (createdSuccessfully) {
				if (!sdIsMounted) {
					Toast.makeText(context,
							getString(R.string.obj_could_not_be_saved),
							Toast.LENGTH_SHORT).show();
				}
                /* temp debugging normal map */
                Intent viewNormal = new Intent();
                viewNormal.setAction(Intent.ACTION_VIEW);
                File bmp = new File(ExternalDirectory.getExternalImageDirectory()+"/export.png");
                viewNormal.setDataAndType(Uri.fromFile(bmp), "image/png");
                startActivity(viewNormal);
				((Activity) context).finish();
			} else {
				Toast.makeText(context,
						getString(R.string.error_while_creating_object),
						Toast.LENGTH_LONG).show();
				((Activity) context).finish();
			}

		}

	}

}
