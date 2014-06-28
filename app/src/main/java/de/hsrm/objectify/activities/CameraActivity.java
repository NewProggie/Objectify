package de.hsrm.objectify.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import de.hsrm.objectify.R;
import de.hsrm.objectify.camera.CameraPreview;
import de.hsrm.objectify.camera.Constants;
import de.hsrm.objectify.rendering.ReconstructionService;
import de.hsrm.objectify.utils.BitmapUtils;
import de.hsrm.objectify.utils.CameraUtils;
import de.hsrm.objectify.utils.Size;
import de.hsrm.objectify.utils.Storage;

public class CameraActivity extends Activity {

    private CameraPreview mCameraPreview;
    private ImageView mCameraLighting;
    private ImageView mCameraLightingMask;
    private Button mTriggerPicturesButton;
    private LinearLayout mProgressScreen;
    private Camera mCamera;
    private String mImageFileName;
    private int mImageCounter;
    private ArrayList<Bitmap> mLightSourcesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* make the activity fullscreen */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        /* opening front facing camera */
        mCamera = openFrontFacingCamera();

        mProgressScreen = (LinearLayout) findViewById(R.id.preparing);
        mCameraPreview = (CameraPreview) findViewById(R.id.camera_surface);
        mCameraPreview.setCamera(mCamera);
        mCameraLighting = (ImageView) findViewById(R.id.camera_lighting);
        mCameraLightingMask = (ImageView) findViewById(R.id.camera_lighting_mask);
        mTriggerPicturesButton = (Button) findViewById(R.id.trigger_images_button);
        mTriggerPicturesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mImageCounter = 0;
                mImageFileName = Storage.getRandomFileName(10);
                setupDisplayScreen();
                takePicture();
            }
        });

        /* prepare the different light sources */
        new PrepareLightSources().execute(getDisplayScreenSize());
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void takePicture() {
        mCameraLighting.setImageBitmap(mLightSourcesList.get(mImageCounter));
        /* give the light source view a little time to update itself */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mCamera.takePicture(null, null, cameraImageCallback());
            }
        }, 100);
    }

    private PictureCallback cameraImageCallback() {
        return new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                Bitmap bmp = CameraUtils.fixRotateMirrorImage(
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                BitmapUtils.saveBitmap(
                        bmp,
                        mImageFileName + "_" + mImageCounter + "." + Constants.IMAGE_FORMAT);
                mImageCounter += 1;
                mCamera.startPreview();
                if (mImageCounter <= Constants.NUM_IMAGES) {
                    takePicture();
                } else {
                    Intent photometricStereo = new Intent(
                            getApplicationContext(), ReconstructionService.class);
                    photometricStereo.putExtra(
                            ReconstructionService.IMAGE_PREFIX_NAME, mImageFileName);
                    startService(photometricStereo);
                }
            }
        };
    }

    private void setupDisplayScreen() {
        /* hide camera preview */
        LayoutParams layoutParams = mCameraPreview.getLayoutParams();
        layoutParams.width = 0;
        layoutParams.height = 0;
        mCameraPreview.setLayoutParams(layoutParams);
            /* hide camera trigger button */
        mTriggerPicturesButton.setVisibility(View.INVISIBLE);
            /* hide lighting mask */
        mCameraLightingMask.setVisibility(View.INVISIBLE);
            /* show light sources on screen */
        mCameraLighting.setVisibility(View.VISIBLE);
    }

    private Camera openFrontFacingCamera() {
        Camera camera;
        /* opening front facing camera */
        try {
            camera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
        } catch (RuntimeException ex) {
            /* no front camera found, trying the first one found */
            camera = Camera.open(0);
        }

        /* set camera to portrait mode */
        camera.setDisplayOrientation(90);
        Camera.Parameters params = camera.getParameters();

        /* set camera picture size to preferred image resolution (640x480) */
        Camera.Size targetSize = CameraUtils.determineTargetPictureSize(params,
                Constants.IMAGE_RESOLUTION);
        params.setPictureSize(targetSize.width, targetSize.height);
        camera.setParameters(params);

        return camera;
    }

    private Size getDisplayScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return new Size(size.x, size.y);
    }

    private void releaseCamera(){
        if (mCamera != null) {
            mCameraPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private class PrepareLightSources extends AsyncTask<Size, Void, Void> {

        @Override
        protected Void doInBackground(Size... sizes) {
            mLightSourcesList = new ArrayList<Bitmap>();
            mLightSourcesList.add(BitmapUtils.generateBlackBitmap(sizes[0]));
            for (int i=1; i <= Constants.NUM_IMAGES; i++) {
                mLightSourcesList.add(
                        BitmapUtils.generateLightPattern(sizes[0], i, Constants.NUM_IMAGES));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressScreen.setVisibility(View.INVISIBLE);
        }
    }

}
