package de.hsrm.objectify;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
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

	private Context context;

	@SuppressWarnings("unused")
	private Button galleryButton, howtoButton, shareButton, cameraButton, settingsButton;

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
		shareButton = (Button) findViewById(R.id.dashboard_share_button);
		cameraButton = (Button) findViewById(R.id.dashboard_camera_button);
		settingsButton = (Button) findViewById(R.id.dashboard_settings_button);
	}

	/**
	 * Predefined method from the appropriate xml layout file. Will be called if
	 * one of the dashboard buttons is clicked.
	 * 
	 * @param target
	 *            clicked dashboard button
	 */
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
		case R.id.dashboard_share_button:
			writeMail();
			break;
		case R.id.dashboard_camera_button:
			Intent camera = new Intent(context, CameraActivity.class);
			startActivity(camera);
			break;
		case R.id.dashboard_settings_button:
			Intent settings = new Intent(context, SettingsActivity.class);
			startActivity(settings);
			break;
		}
	}
	
	private void writeMail() {
		Intent tellOthers = new Intent(android.content.Intent.ACTION_SEND);
		tellOthers.setType("plain/text");
		PackageManager pm = getPackageManager();
		List<ResolveInfo> list = pm.queryIntentActivities(tellOthers, PackageManager.MATCH_DEFAULT_ONLY);
		if (list.size() > 0) {
			tellOthers.putExtra(android.content.Intent.EXTRA_SUBJECT, "Android " + getString(R.string.app_name));
			tellOthers.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.mailtext));
			startActivity(tellOthers);
		} else {
			Toast.makeText(context,getString(R.string.no_mailclient_found),Toast.LENGTH_LONG).show();
		}
	}

}