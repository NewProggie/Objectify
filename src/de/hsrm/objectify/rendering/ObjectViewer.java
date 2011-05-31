package de.hsrm.objectify.rendering;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import de.hsrm.objectify.R;
import de.hsrm.objectify.ui.BaseActivity;
import de.hsrm.objectify.utils.ExternalDirectory;

/**
 * This {@link Activity} holds a {@link TouchSurfaceView} which contains the
 * actual rendered object and takes care of the different states such as
 * <code>onPause</code> and <code>onResume</code>.
 * 
 * @author kwolf001
 * 
 */
public class ObjectViewer extends BaseActivity {

	private static final String TAG = "ObjectViewer";
	private TouchSurfaceView glSurfaceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar(getString(R.string.object_viewer), 0);
		addNewActionButton(R.drawable.ic_title_share, R.string.share, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("image/jpeg");
				Bitmap screenshot = glSurfaceView.getScreenshot();
				String path = ExternalDirectory.getExternalImageDirectory() + "/screenshot.png";
				try {
					FileOutputStream fos = new FileOutputStream(path);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					screenshot.compress(CompressFormat.PNG, 100, bos);
					bos.flush();
					bos.close();
					share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path));
					startActivity(Intent.createChooser(share, getString(R.string.share)));
				} catch (FileNotFoundException e) {
					Log.e(TAG, e.getMessage());
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
				
			}
		});
		addNewActionButton(R.drawable.ic_title_info, R.string.info, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		Display display = getWindowManager().getDefaultDisplay();
		Bundle b = getIntent().getBundleExtra("bundle");
		ObjectModel objectModel = b.getParcelable("objectModel");
		glSurfaceView = new TouchSurfaceView(this, objectModel, display.getWidth(), display.getHeight());
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
