package de.hsrm.objectify.camera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import de.hsrm.objectify.utils.CameraUtils;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context) {
        super(context);
        initialize();
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        if (this.mCamera == camera) {
            return;
        }

        this.mCamera = camera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (mCamera == null) {
            return;
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException ex) {
            String TAG = "CameraPreview";
            Log.e(TAG, ex.getLocalizedMessage());
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (mCamera == null) {
            return;
        }

        Camera.Parameters params = mCamera.getParameters();
        Size targetSize = CameraUtils.determineTargetPictureSize(params,
                Constants.IMAGE_RESOLUTION);
        params.setPreviewSize(targetSize.width, targetSize.height);
        mCamera.setParameters(params);
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera == null) {
            return;
        }

        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

}
