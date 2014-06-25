package de.hsrm.objectify.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

import de.hsrm.objectify.R;
import de.hsrm.objectify.camera.CameraPreview;
import de.hsrm.objectify.camera.Constants;
import de.hsrm.objectify.utils.BitmapUtils;
import de.hsrm.objectify.utils.CameraUtils;
import de.hsrm.objectify.utils.Size;

public class CameraActivity extends Activity {

    private final String TAG = "CameraActivity";
    private CameraPreview mCameraPreview;
    private ImageView mCameraLighting;
    private ImageView mCameraLightingMask;
    private Camera mCamera;
    private Size mScreenSize;
    private Button mTriggerPicturesButton;
    public ArrayList<Bitmap> mImageList;
    private final int NUM_PICTURES = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* make the activity fullscreen */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        /* get the display screen size for displaying light pattern */
        mScreenSize = getDisplayScreenSize();

        /* opening front facing camera */
        mCamera = openFrontFacingCamera();

        mCameraPreview = (CameraPreview) findViewById(R.id.camera_surface);
        mCameraPreview.setCamera(mCamera);
        mCameraLighting = (ImageView) findViewById(R.id.camera_lighting);
        mCameraLightingMask = (ImageView) findViewById(R.id.camera_lighting_mask);
        mImageList = new ArrayList<Bitmap>();
        mTriggerPicturesButton = (Button) findViewById(R.id.trigger_images_button);
        mTriggerPicturesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mImageList.clear();
                setupDisplayScreen(true);
                mCameraLighting.setImageBitmap(BitmapUtils.generateLightPattern(mScreenSize,
                        1, NUM_PICTURES));
                mCamera.takePicture(null, null, cameraImageCallback());
            }
        });
    }

    private PictureCallback cameraImageCallback() {
        return new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                Bitmap bmp = CameraUtils.fixRotateMirrorImage(BitmapFactory.decodeByteArray(bytes,
                        0, bytes.length));
                mImageList.add(bmp);
                mCamera.startPreview();
                if (mImageList.size() < NUM_PICTURES) {
                    mCameraLighting.setImageBitmap(BitmapUtils.generateLightPattern(mScreenSize,
                            mImageList.size()+1, NUM_PICTURES));
                    mCamera.takePicture(null, null, cameraImageCallback());
                }

                Intent backToMain = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(backToMain);
            }
        };
    }

    private void setupDisplayScreen(boolean switchLightPatternsON) {
        if (switchLightPatternsON) {
            /* hide camera preview */
            LayoutParams layoutParams = mCameraPreview.getLayoutParams();
            layoutParams.width = 0;
            layoutParams.height = 0;
            mCameraPreview.setLayoutParams(layoutParams);
            /* hide camera trigger button */
            mTriggerPicturesButton.setVisibility(View.INVISIBLE);
            mCameraLighting.setVisibility(View.VISIBLE);
            mCameraLightingMask.setVisibility(View.INVISIBLE);
        } else {
            mCameraLighting.setVisibility(View.INVISIBLE);
            mCameraLightingMask.setVisibility(View.VISIBLE);
        }
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

}
