package de.hsrm.objectify.gallery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.Toast;
import de.hsrm.objectify.R;
import de.hsrm.objectify.actionbarcompat.ActionBarActivity;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.database.DatabaseProvider;
import de.hsrm.objectify.rendering.ObjectModel;
import de.hsrm.objectify.rendering.ObjectViewerActivity;
import de.hsrm.objectify.utils.ExternalDirectory;

/**
 * An activity with a gallery included, showing all images made within this app
 * and displaying some info about the 3D objects.
 * 
 * @author kwolf001
 * 
 */
public class GalleryActivity extends ActionBarActivity {

	private static final String TAG = "GalleryActivity";
	private GridView galleryGrid;
	private GalleryAdapter adapter;
	private Context context;
	private Uri galleryUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		setTitle(getString(R.string.gallery));

		context = this;

		galleryGrid = (GridView) findViewById(R.id.galleryGrid);
		galleryUri = DatabaseProvider.CONTENT_URI.buildUpon()
				.appendPath("gallery").build();
		Cursor cursor = this.managedQuery(galleryUri, null, null, null, null);
		adapter = new GalleryAdapter(this, cursor);
		galleryGrid.setAdapter(adapter);
		galleryGrid.setOnItemClickListener(galleryItemClickListener());
		galleryGrid.setOnItemLongClickListener(galleryItemLongClickListener());

		if (adapter.getCount() == 0) {
			showMessageAndExit(getString(R.string.gallery),
					getString(R.string.no_objects_saved));
		}
	}

	private OnItemClickListener galleryItemClickListener() {
		OnItemClickListener listener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent showDetails = new Intent(parent.getContext(),
						GalleryDetailsActivity.class);
				showDetails.putExtra("id", id);
				startActivity(showDetails);
			}
		};
		return listener;
	}

	private OnItemLongClickListener galleryItemLongClickListener() {
		OnItemLongClickListener listener = new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, final long id) {
				final CharSequence[] items = { getString(R.string.delete),
						getString(R.string.show) };

				AlertDialog.Builder builder = new AlertDialog.Builder(
						v.getContext());
				builder.setTitle(getString(R.string.edit));
				builder.setItems(items, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri galleryItemUri = DatabaseProvider.CONTENT_URI
								.buildUpon().appendPath("gallery").build();
						ContentResolver cr = getContentResolver();
						String galleryId = String.valueOf(id);
						Cursor c = cr.query(galleryItemUri, null,
								DatabaseAdapter.GALLERY_ID_KEY + "=?",
								new String[] { galleryId }, null);
						c.moveToFirst();
						String objectId = c
								.getString(DatabaseAdapter.GALLERY_OBJECT_ID_COLUMN);
						c.close();
						switch (which) {
						case 0:
							new DeleteObject().execute(galleryId, objectId);
							break;
						case 1:
							new LoadingObject().execute(objectId);
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				return true;
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
		builder.setTitle(title)
				.setMessage(msg)
				.setCancelable(false)
				.setPositiveButton(getString(R.string.submit),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								((Activity) context).finish();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private class LoadingObject extends AsyncTask<String, Void, ObjectModel> {

		private ProgressDialog pleaseWait;
		private ContentResolver cr;

		@Override
		protected void onPreExecute() {
			pleaseWait = ProgressDialog.show(context, "",
					getString(R.string.please_wait), true);
			cr = getContentResolver();
		}

		@Override
		protected ObjectModel doInBackground(String... params) {
			String objectId = params[0];
			Uri objectUri = DatabaseProvider.CONTENT_URI.buildUpon()
					.appendPath("object").build();
			ObjectModel objectModel = null;
			Cursor c = cr.query(objectUri, null, DatabaseAdapter.OBJECT_ID_KEY
					+ "=?", new String[] { objectId }, null);
			c.moveToFirst();
			try {
				FileInputStream inputStream = new FileInputStream(
						c.getString(DatabaseAdapter.OBJECT_FILE_PATH_COLUMN));
				ObjectInputStream objInput = new ObjectInputStream(inputStream);
				objectModel = (ObjectModel) objInput.readObject();
				objectModel.setup();
				return objectModel;
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getLocalizedMessage());
				e.printStackTrace();
			} catch (Exception e) {
				Log.e(TAG, e.getLocalizedMessage());
				e.printStackTrace();
			}
			pleaseWait.dismiss();
			return objectModel;
		}

		@Override
		protected void onPostExecute(ObjectModel result) {
			pleaseWait.dismiss();
			if (result != null) {
				Intent viewObject = new Intent(context,
						ObjectViewerActivity.class);
				Bundle b = new Bundle();
				b.putParcelable("objectModel", result);
				viewObject.putExtra("bundle", b);
				startActivity(viewObject);
			} else {
				Toast.makeText(context,
						getString(R.string.error_while_reading),
						Toast.LENGTH_LONG).show();
			}
		}

	}

	private class DeleteObject extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog pleaseWait;
		private ContentResolver cr;
		private Uri galleryItemUri;
		private Cursor newCursor;

		@Override
		protected void onPreExecute() {
			pleaseWait = ProgressDialog.show(context, "",
					getString(R.string.please_wait), true);
			cr = getContentResolver();
			galleryItemUri = DatabaseProvider.CONTENT_URI.buildUpon()
					.appendPath("gallery").build();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			String galleryId = params[0];
			String objectId = params[1];

			if (!ExternalDirectory.isMounted()) {
				return false;
			}

			Uri objectUri = DatabaseProvider.CONTENT_URI.buildUpon()
					.appendPath("object").build();
			Uri galleryUri = DatabaseProvider.CONTENT_URI.buildUpon()
					.appendPath("gallery").build();

			Cursor objectCursor = cr.query(objectUri, null,
					DatabaseAdapter.OBJECT_ID_KEY + "=?",
					new String[] { objectId }, null);
			objectCursor.moveToFirst();
			String filePath = objectCursor
					.getString(DatabaseAdapter.OBJECT_FILE_PATH_COLUMN);
			objectCursor.close();
			File objFile = new File(filePath);
			boolean objDeleted = objFile.delete();

			Cursor galleryCursor = cr.query(galleryUri, null,
					DatabaseAdapter.GALLERY_ID_KEY + "=?",
					new String[] { galleryId }, null);
			galleryCursor.moveToFirst();
			String imgPath = galleryCursor
					.getString(DatabaseAdapter.GALLERY_THUMBNAIL_PATH_COLUMN);
			galleryCursor.close();
			File thumbnail = new File(imgPath);
			boolean imgDeleted = thumbnail.delete();

			int result1 = cr.delete(galleryItemUri,
					DatabaseAdapter.GALLERY_ID_KEY + "=?",
					new String[] { galleryId });
			int result2 = cr.delete(objectUri, DatabaseAdapter.OBJECT_ID_KEY
					+ "=?", new String[] { objectId });

			newCursor = managedQuery(galleryUri, null, null, null, null);

			if ((result1 + result2) >= 2 && objDeleted && imgDeleted) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean successfullyDeleted) {
			pleaseWait.dismiss();
			adapter.changeCursor(newCursor);
			if (successfullyDeleted) {
				Toast.makeText(context,
						getString(R.string.deleted_successfully),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context,
						getString(R.string.error_while_deleting),
						Toast.LENGTH_SHORT).show();
			}

		}
	}
}
