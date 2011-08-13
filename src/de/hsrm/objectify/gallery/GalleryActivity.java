package de.hsrm.objectify.gallery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import de.hsrm.objectify.R;
import de.hsrm.objectify.database.DatabaseProvider;
import de.hsrm.objectify.ui.BaseActivity;

/**
 * An activity with a gallery included, showing all images made within this app
 * and displaying some info about the 3D objects.
 * 
 * @author kwolf001
 * 
 */
public class GalleryActivity extends BaseActivity {

	private static final String TAG = "GalleryActivity";
	private GridView galleryGrid;
	private GalleryAdapter adapter;
	private Context context;
	private Uri galleryUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		setupActionBar(getString(R.string.gallery), 0);
				
		context = this;
		
		galleryGrid = (GridView) findViewById(R.id.galleryGrid);
		galleryUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath("gallery").build();
		Cursor cursor = this.managedQuery(galleryUri, null, null, null, null);
		adapter = new GalleryAdapter(this, cursor);
		galleryGrid.setAdapter(adapter);
		galleryGrid.setOnItemClickListener(galleryItemClickListener());
		
		if (adapter.getCount() == 0) {
			showMessageAndExit(getString(R.string.gallery), getString(R.string.no_objects_saved));
		}
	}
	
	private OnItemClickListener galleryItemClickListener() {
		OnItemClickListener listener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Intent showDetails = new Intent(parent.getContext(), GalleryDetailsActivity.class);
				showDetails.putExtra("id", id);
				startActivity(showDetails);
			}
		};
		return listener;
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
