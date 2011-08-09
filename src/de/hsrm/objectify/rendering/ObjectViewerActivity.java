package de.hsrm.objectify.rendering;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
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
public class ObjectViewerActivity extends BaseActivity {

	private static final String TAG = "ObjectViewer";
	private TouchSurfaceView glSurfaceView;
	private FrameLayout frameLayout;
	private Context context;
	private ObjectModel objectModel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		frameLayout = new FrameLayout(this);
		setContentView(frameLayout);
		setupActionBar(getString(R.string.object_viewer), 0);
		addNewActionButton(R.drawable.ic_title_share, R.string.share, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("image/jpeg");
				String path = ExternalDirectory.getExternalImageDirectory() + "/objectify_screenshot.png";
				try {
					FileOutputStream fos = new FileOutputStream(path);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					Bitmap screenshot = glSurfaceView.getSurfaceBitmap();
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
		// TODO: KŸnftiges Feature: Export als obj
//		addNewActionButton(R.drawable.ic_title_export, R.string.export, new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		Display display = getWindowManager().getDefaultDisplay();
		Bundle b = getIntent().getBundleExtra("bundle");
		objectModel = b.getParcelable("objectModel");
		glSurfaceView = new TouchSurfaceView(this, objectModel, display.getWidth(), display.getHeight());
		frameLayout.addView(glSurfaceView);
		// Adding controls
		LayoutInflater inflater = LayoutInflater.from(this);
		LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.objectviewer_controls, null);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT,Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
		frameLayout.addView(ll, params);
	}
	
	public void actionControl(View view) {
		String tag = (String) view.getTag();
		if (tag.equals("texture")) {
			objectModel.setRenderingMode(GL10.GL_TRIANGLES);
			glSurfaceView.requestRender();
		} else if (tag.equals("points")) {
			objectModel.setRenderingMode(GL10.GL_POINTS);
			glSurfaceView.requestRender();
		} else if (tag.equals("wireframe")) {
			objectModel.setRenderingMode(GL10.GL_LINES);
			glSurfaceView.requestRender();
		}
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
