package de.hsrm.objectify.camera;

import java.io.IOException;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "CameraPreview";
	private SurfaceHolder holder;
	
	public CameraPreview(Context context) {
		super(context);
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			CameraActivity.camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			CameraActivity.camera.release();
			CameraActivity.camera = null;
		}

	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		CameraActivity.camera.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		CameraActivity.camera.setPreviewCallback(null);
		CameraActivity.camera.stopPreview();
		CameraActivity.camera.release();
		CameraActivity.camera = null;
	}
}