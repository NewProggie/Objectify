package de.hsrm.objectify.gallery;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Gallery;
import de.hsrm.objectify.R;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.database.DatabaseProvider;
import de.hsrm.objectify.ui.BaseActivity;
import de.hsrm.objectify.utils.ExternalDirectory;

/**
 * An activity with a gallery included, showing all images made within this app
 * and displaying some info about the 3D objects.
 * 
 * @author kwolf001
 * 
 */
public class GalleryActivity extends BaseActivity {

	private static final String TAG = "GalleryActivity";
	private Gallery gallery;
	private GalleryAdapter adapter;
	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		
		gallery = new Gallery(this);
		setContentView(gallery);
		
		Uri uri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath("gallery").build();
		Cursor cursor = this.managedQuery(uri, null, null, null, null);
		adapter = new GalleryAdapter(this, cursor);
		gallery.setAdapter(adapter);
		
		if (adapter.getCount() == 0) {
			showMessageAndExit(getString(R.string.gallery), getString(R.string.no_objects_saved));
		}
	}
	
	/**
	 * Shows specific Dialog to user and finishes current Activity
	 * 
	 * @param title
	 *            title of dialog
	 * @param msg
	 *            message of dialog
	 */
	private void showMessageAndExit(String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setMessage(msg).setCancelable(false).setPositiveButton(getString(R.string.submit), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((Activity) context).finish();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
