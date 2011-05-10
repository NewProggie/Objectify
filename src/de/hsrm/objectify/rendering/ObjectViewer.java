package de.hsrm.objectify.rendering;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class ObjectViewer extends Activity {

	private static final String TAG = "ObjectViewer";
	private GLSurfaceView glSurfaceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
