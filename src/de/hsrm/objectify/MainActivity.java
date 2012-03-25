package de.hsrm.objectify;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import de.hsrm.objectify.actionbarcompat.ActionBarActivity;
import de.hsrm.objectify.camera.CameraActivity;
import de.hsrm.objectify.gallery.GalleryActivity;
import de.hsrm.objectify.howto.HowToActivity;
import de.hsrm.objectify.ui.DashboardLayout;

/**
 * Front door {@link Activity} that displays {@link DashboardLayout} with
 * different features of this app. Inherits an action bar from
 * {@link BaseActivity} and initializes it.
 * 
 * @author kwolf001
 * 
 */
public class MainActivity extends ActionBarActivity {

	private Context context;
	// private LicenseCheckerCallback licenseCheckerCallback;
	// private LicenseChecker checker;
	private Handler handler;
	@SuppressWarnings("unused")
	private Button galleryButton, howtoButton, shareButton, cameraButton,
			settingsButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		context = this;

		handler = new Handler();
		setTitle(R.string.app_name);
//		addNewActionButton(R.drawable.ic_title_camera, R.string.camera,
//				new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						Intent camera = new Intent(v.getContext(),
//								CameraActivity.class);
//						startActivity(camera);
//					}
//				});

		galleryButton = (Button) findViewById(R.id.dashboard_gallery_button);
		howtoButton = (Button) findViewById(R.id.dashboard_howto_button);
		shareButton = (Button) findViewById(R.id.dashboard_share_button);
		cameraButton = (Button) findViewById(R.id.dashboard_camera_button);
		settingsButton = (Button) findViewById(R.id.dashboard_settings_button);

		// Construct the LicenseCheckerCallback. The library calls this when
		// done
		// licenseCheckerCallback = new ObjectifyLicenseCheckerCallback();

		// Construct the LicenseChecker with a Policy
		deviceId = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
		// checker = new LicenseChecker(context, new
		// ServerManagedPolicy(context,
		// new AESObfuscator(SALT, getPackageName(), deviceId)),
		// BASE_64_PUBLIC_KEY);
		// validate this copy of objectify
		// doCheck();
	}

	private void doCheck() {
		// checker.checkAccess(licenseCheckerCallback);
	}

	protected Dialog onCreateDialog(int id) {
		// We have only one dialog.
		return new AlertDialog.Builder(this)
				.setTitle(R.string.unlicensed_dialog_title)
				.setMessage(R.string.unlicensed_dialog_body)
				.setPositiveButton(R.string.buy_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								Intent marketIntent = new Intent(
										Intent.ACTION_VIEW,
										Uri.parse("http://market.android.com/details?id="
												+ getPackageName()));
								startActivity(marketIntent);
							}
						})
				.setNegativeButton(R.string.quit_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						}).create();
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
		List<ResolveInfo> list = pm.queryIntentActivities(tellOthers,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (list.size() > 0) {
			tellOthers.putExtra(android.content.Intent.EXTRA_SUBJECT,
					"Android " + getString(R.string.app_name));
			tellOthers.putExtra(android.content.Intent.EXTRA_TEXT,
					getString(R.string.mailtext)
							+ "\nhttp://market.android.com/details?id="
							+ getPackageName());
			startActivity(tellOthers);
		} else {
			Toast.makeText(context, getString(R.string.no_mailclient_found),
					Toast.LENGTH_LONG).show();
		}
	}

	private void displayResult(final String result) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_settings:
			Intent toSettings = new Intent(context, SettingsActivity.class);
			startActivity(toSettings);
			break;
		case R.id.opt_about:
			Intent toAbout = new Intent(context, AboutActivity.class);
			startActivity(toAbout);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// checker.onDestroy();
	}

	private static final String BASE_64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyvnIeG1BPK0Gnjj7dZmzWqyCT9YHFl4h7HR2JaKLgpgaD1mmseIMVwSh+2uVr0AS/CEfUawiHKPAmm6D9EqXs2kL/Odh41TagwYU2OxfXaB1R4wx9mZ7+kJ47uzvs6i+2mbkgdIgtIwsoI/jpspCB7bSZE8nrk+bG5OZdq/i2cWYBtbYQsXO9BwI90DK0gX4kZnO6OAfnoaazIghIEy4hIYQpnBnEBpnCY85K5WDmKNEb9nip6Rb8n3hNDHXA8hTKAGBtQBp/zArCuKSR9d4Un5Zu+DYp2/iAxIHLzB067WqUAWOfhrfkRSmHfaQHLVvTQxgJ4cUPojRAUKhRjPL+QIDAQAB";
	private static final byte[] SALT = new byte[] { -17, 4, 89, -118, -10, -79,
			23, -4, 11, 28, 99, -34, 62, -12, -50, -16, -25, 30, -16, 55 };
	private String deviceId;

	// private class ObjectifyLicenseCheckerCallback implements
	// LicenseCheckerCallback {
	//
	// @Override
	// public void allow() {
	// if (isFinishing()) {
	// return;
	// }
	// }
	//
	// @Override
	// public void dontAllow() {
	// if (isFinishing()) {
	// return;
	// }
	// showDialog(0);
	// }
	//
	// @Override
	// public void applicationError(ApplicationErrorCode errorCode) {
	// if (isFinishing()) {
	// return;
	// }
	// String result = String.format(getString(R.string.application_error),
	// errorCode);
	// displayResult(result);
	// }
	//
	// }

}