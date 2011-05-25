package de.hsrm.objectify.gallery;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;
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
	private TextView size, faces, vertices, dimension, date;
	private Uri galleryUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		setupActionBar(getString(R.string.gallery), 0);
		addNewActionButton(R.drawable.ic_title_share, R.string.share, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("image/jpeg");
				long id = gallery.getSelectedItemId();
				Cursor c = getContentResolver().query(galleryUri, null, DatabaseAdapter.GALLERY_ID_KEY+"=?", new String[] { String.valueOf(id) }, null);
				c.moveToFirst();
				try {
					byte[] bb = c.getBlob(DatabaseAdapter.GALLERY_IMAGE_COLUMN);
					Bitmap screenshot = BitmapFactory.decodeByteArray(bb, 0, bb.length);
					String path = ExternalDirectory.getExternalImageDirectory() + "/screenshot.png";
					FileOutputStream fos = new FileOutputStream(path);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					screenshot.compress(CompressFormat.PNG, 100, bos);
					bos.flush();
					bos.close();
					share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path));
					startActivity(Intent.createChooser(share, getString(R.string.share)));
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
				c.close();
			}
		});
		addNewActionButton(R.drawable.ic_title_delete, R.string.delete, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage(getString(R.string.chosen_object_really_delete))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.submit), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							long id = gallery.getSelectedItemId();
							deleteFromDatabase(id);
							Toast.makeText(context, getString(R.string.deleted_successfully), Toast.LENGTH_SHORT).show();
							dialog.cancel();
						}
					})
					.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
				AlertDialog alert = builder.create();
				alert.show();
				
				
			}
		});
				
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
	 * Deletes all images with the given id from the storage and database and
	 * calls adapter to refresh itself.
	 * 
	 * @param id
	 *            gallery id in database
	 */
	private void deleteFromDatabase(long id) {
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(galleryUri, null, DatabaseAdapter.GALLERY_ID_KEY+"=?", new String[] { String.valueOf(id) }, null);
		c.moveToFirst();
		final String image_suffix = c.getString(DatabaseAdapter.GALLERY_SUFFIX_COLUMN);
		File imageDir = new File(ExternalDirectory.getExternalImageDirectory());
		File[] images = imageDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				return (filename.contains(image_suffix));
			}
		});
		for (File img : images) {
			img.delete();
		}
		
		cr.delete(galleryUri, DatabaseAdapter.GALLERY_ID_KEY+"=?", new String[] { String.valueOf(id) } );
		c.close();
		adapter.cursor.requery();
		adapter.notifyDataSetChanged();
		if (adapter.getCount() == 0) {
			finish();
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
