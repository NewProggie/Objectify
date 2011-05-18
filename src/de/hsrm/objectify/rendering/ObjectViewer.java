package de.hsrm.objectify.rendering;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hsrm.objectify.utils.ExternalDirectory;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Display;
import android.view.View.MeasureSpec;

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
//		glSurfaceView.setDrawingCacheEnabled(true);
//		glSurfaceView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//		glSurfaceView.layout(0, 0, glSurfaceView.getMeasuredWidth(), glSurfaceView.getMeasuredHeight());
//		glSurfaceView.buildDrawingCache(true);
//		Bitmap screenshot = Bitmap.createBitmap(glSurfaceView.getDrawingCache());
//		try {
//			FileOutputStream fos = new FileOutputStream(ExternalDirectory.getExternalImageDirectory() + "/screenshot.png");
//			BufferedOutputStream bos = new BufferedOutputStream(fos);
//			screenshot.compress(CompressFormat.PNG, 90, bos);
//			bos.flush();
//			bos.close();
//			glSurfaceView.setDrawingCacheEnabled(false);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		glSurfaceView.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		glSurfaceView.onResume();
	}
}
