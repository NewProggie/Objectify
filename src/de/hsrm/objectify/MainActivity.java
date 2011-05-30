package de.hsrm.objectify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import de.hsrm.objectify.camera.CameraActivity;
import de.hsrm.objectify.gallery.GalleryActivity;
import de.hsrm.objectify.howto.HowToActivity;
import de.hsrm.objectify.ui.BaseActivity;
import de.hsrm.objectify.ui.DashboardLayout;

/**
 * Front door {@link Activity} that displays {@link DashboardLayout} with
 * different features of this app. Inherits an action bar from
 * {@link BaseActivity} and initializes it.
 * 
 * @author kwolf001
 * 
 */
public class MainActivity extends BaseActivity {

	// TODO: Feedback senden in About einbauen

	private Context context;

	@SuppressWarnings("unused")
	private Button galleryButton, howtoButton, exportButton, shareButton, cameraButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		context = this;

		setupActionBar(null, 0);
		addNewActionButton(R.drawable.ic_title_camera, R.string.camera, new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent camera = new Intent(v.getContext(), CameraActivity.class);
				startActivity(camera);
			}
		});

		galleryButton = (Button) findViewById(R.id.dashboard_gallery_button);
		howtoButton = (Button) findViewById(R.id.dashboard_howto_button);
		exportButton = (Button) findViewById(R.id.dashboard_export_button);
		shareButton = (Button) findViewById(R.id.dashboard_share_button);
		cameraButton = (Button) findViewById(R.id.dashboard_camera_button);
	}

	public void buttonClick(View target) {
		switch (target.getId()) {
		case R.id.dashboard_gallery_button:
			Intent gallery = new Intent(context, GalleryActivity.class);
			startActivity(gallery);
			break;
		case R.id.dashboard_howto_button:
			Intent howto = new Intent(context, HowToActivity.class);
			startActivity(howto);
			break;
		case R.id.dashboard_export_button:
			Toast.makeText(context, "export", Toast.LENGTH_SHORT).show();
			break;
		case R.id.dashboard_share_button:
			Toast.makeText(context, "share", Toast.LENGTH_SHORT).show();
			break;
		case R.id.dashboard_camera_button:
			Intent camera = new Intent(context, CameraActivity.class);
			startActivity(camera);
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.option, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_settings:
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			return true;
		case R.id.opt_about:
			Intent about = new Intent(this, AboutActivity.class);
			startActivity(about);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}