package de.hsrm.objectify.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.NinePatchDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Surface;
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

    public static final String RECONSTRUCTION = "new_reconstruction";
    private CameraPreview mCameraPreview;
    private ImageView mCameraLighting;
    private ImageView mCameraLightingMask;
    private Button mTriggerPicturesButton;
    private LinearLayout mProgressScreen;
    private Camera mCamera;
    private String mDirName;
    private int mImgCounter;
    private int mCameraRotation;
    private ArrayList<NinePatchDrawable> mLightSourcesList;

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
                mImgCounter = 0;
                mDirName = Storage.getRandomName(10);
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
        mCameraLighting.setImageDrawable(mLightSourcesList.get(mImgCounter));
        /* give the light source view a little time to update itself */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mCamera.takePicture(null, null, cameraImageCallback());
            }
        }, 25);
    }

    private PictureCallback cameraImageCallback() {
        return new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                Bitmap bmp = CameraUtils.fixRotateMirrorImage(
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                /* rotate camera image according to camera rotation (portrait vs. landscape) */
                Matrix matrix = new Matrix();
                /* compensate the mirror */
                matrix.postRotate((360 - mCameraRotation) % 360);
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

                String fileName = Constants.IMAGE_NAME + mImgCounter + "." + Constants.IMAGE_FORMAT;
                BitmapUtils.saveBitmap(BitmapUtils.convertToGrayscale(bmp), mDirName, fileName);
                mImgCounter += 1;
                mCamera.startPreview();
                if (mImgCounter <= Constants.NUM_IMAGES) {
                    takePicture();
                } else {
                    /* start 3d reconstruction asynchronously in background */
                    Intent photometricStereo = new Intent(
                            getApplicationContext(), ReconstructionService.class);
                    photometricStereo.putExtra(
                            ReconstructionService.DIRECTORY_NAME, mDirName);
                    startService(photometricStereo);
                    /* move to 3d viewer already */
                    Intent view3DModel = new Intent(getApplicationContext(),
                            ReconstructionListActivity.class);
                    view3DModel.putExtra(RECONSTRUCTION, true);
                    startActivity(view3DModel);
                    finish();
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
        mTriggerPicturesButton.setVisibility(View.INVISIBLE);   /* hide camera trigger button */
        mCameraLightingMask.setVisibility(View.INVISIBLE);      /* hide lighting mask */
        mCameraLighting.setVisibility(View.VISIBLE);            /* show light sources on screen */
    }

    private Camera openFrontFacingCamera() {
        Camera camera;
        int camId = CameraInfo.CAMERA_FACING_FRONT;
        try {
            camera = Camera.open(camId);
        } catch (RuntimeException ex) {
            /* no front camera found, trying the first one found */
            camId = 0;
            camera = Camera.open(camId);
        }

        /* determine current rotation of device */
        mCameraRotation = getDisplayRotation();
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(camId, info);

        /* set front facing camera to portrait mode */
        int result = (info.orientation + mCameraRotation) % 360;
        /* compensate the mirror */
        result = (360 - result) % 360;
        camera.setDisplayOrientation(result);
        Camera.Parameters params = camera.getParameters();

        /* set camera picture size to preferred image resolution */
        Camera.Size targetSize = CameraUtils.determineTargetPictureSize(params,
                Constants.IMAGE_RESOLUTION);
        params.setPictureSize(targetSize.width, targetSize.height);
        camera.setParameters(params);

        return camera;
    }

    private int getDisplayRotation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }

        return 0;
    }

    private Size getDisplayScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return new Size(size.x, size.y);
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCameraPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private class PrepareLightSources extends AsyncTask<Size, Void, Void> {

        private NinePatchDrawable getCamLighting(Resources res, Size size, int drawableId) {
            NinePatchDrawable npd = (NinePatchDrawable) res.getDrawable(drawableId);
            npd.setBounds(0, 0, size.width, size.height);
            return npd;
        }

        @Override
        protected Void doInBackground(Size... sizes) {
            mLightSourcesList = new ArrayList<NinePatchDrawable>();

            Resources res = getResources();
            mLightSourcesList.add(getCamLighting(res, sizes[0], R.drawable.camera_lighting_black));
            mLightSourcesList.add(getCamLighting(res, sizes[0], R.drawable.camera_lighting_left));
            mLightSourcesList.add(getCamLighting(res, sizes[0], R.drawable.camera_lighting_top));
            mLightSourcesList.add(getCamLighting(res, sizes[0], R.drawable.camera_lighting_right));
            mLightSourcesList.add(getCamLighting(res, sizes[0], R.drawable.camera_lighting_bottom));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressScreen.setVisibility(View.INVISIBLE);
        }
    }

}
