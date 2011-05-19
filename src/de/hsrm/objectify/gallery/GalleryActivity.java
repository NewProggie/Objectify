package de.hsrm.objectify.gallery;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.TextView;
import de.hsrm.objectify.R;
import de.hsrm.objectify.database.DatabaseAdapter;
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
	private Gallery gallery;
	private GalleryAdapter adapter;
	private Context context;
	private TextView size, faces, vertices, dimension, date;
	private Uri galleryUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		context = this;
		
		gallery = (Gallery) findViewById(R.id.object_gallery);
		size = (TextView) findViewById(R.id.gallery_size_textview);
		faces = (TextView) findViewById(R.id.gallery_faces_textview);
		vertices = (TextView) findViewById(R.id.gallery_vertices_textview);
		dimension = (TextView) findViewById(R.id.gallery_dimension_textview);
		date = (TextView) findViewById(R.id.gallery_date_textview);
		
		galleryUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath("gallery").build();
		Cursor cursor = this.managedQuery(galleryUri, null, null, null, null);
		adapter = new GalleryAdapter(this, cursor);
		gallery.setAdapter(adapter);
		gallery.setOnItemSelectedListener(galleryItemSelectedListener());
		
		if (adapter.getCount() == 0) {
			showMessageAndExit(getString(R.string.gallery), getString(R.string.no_objects_saved));
		}
	}
	
	private OnItemSelectedListener galleryItemSelectedListener() {
		OnItemSelectedListener listener = new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				String[] args = { String.valueOf(id) };
				Cursor c = getContentResolver().query(galleryUri, null, DatabaseAdapter.GALLERY_ID_KEY+"=?", args, null);
				c.moveToFirst();
				float s = Integer.valueOf(c.getString(DatabaseAdapter.GALLERY_SIZE_COLUMN))/1024;
				size.setText(String.valueOf(s) + " KB");
				faces.setText(c.getString(DatabaseAdapter.GALLERY_FACES_COLUMN));
				vertices.setText(c.getString(DatabaseAdapter.GALLERY_VERTICES_COLUMN));
				dimension.setText(c.getString(DatabaseAdapter.GALLERY_DIMENSIONS_COLUMN));
				Date d = new Date(Long.parseLong(c.getString(DatabaseAdapter.GALLERY_DATE_COLUMN)));
				date.setText(d.toLocaleString());
				c.close();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
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
