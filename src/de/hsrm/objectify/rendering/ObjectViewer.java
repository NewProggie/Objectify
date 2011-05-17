package de.hsrm.objectify.rendering;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;

/**
 * This {@link Activity} holds a {@link TouchSurfaceView} which contains the
 * actual rendered object and takes care of the different states such as
 * <code>onPause</code> and <code>onResume</code>.
 * 
 * @author kwolf001
 * 
 */
public class ObjectViewer extends Activity {

	private static final String TAG = "ObjectViewer";
	private TouchSurfaceView glSurfaceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Display display = getWindowManager().getDefaultDisplay();
		String path = getIntent().getStringExtra("image_path");
		glSurfaceView = new TouchSurfaceView(this, path, display.getWidth(), display.getHeight());
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
