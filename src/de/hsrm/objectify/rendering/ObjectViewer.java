package de.hsrm.objectify.rendering;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Display;

public class ObjectViewer extends Activity {

	private static final String TAG = "ObjectViewer";
	private TouchSurfaceView glSurfaceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Display display = getWindowManager().getDefaultDisplay();
		glSurfaceView = new TouchSurfaceView(this, null, display.getWidth(), display.getHeight());
		setContentView(glSurfaceView);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		glSurfaceView.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		glSurfaceView.onResume();
	}
}
