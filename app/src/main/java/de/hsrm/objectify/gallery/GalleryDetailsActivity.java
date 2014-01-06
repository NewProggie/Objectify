package de.hsrm.objectify.gallery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.hsrm.objectify.R;
import de.hsrm.objectify.SettingsActivity;
import de.hsrm.objectify.actionbarcompat.ActionBarActivity;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.database.DatabaseProvider;
import de.hsrm.objectify.rendering.ObjectModel;
import de.hsrm.objectify.rendering.ObjectViewerActivity;
import de.hsrm.objectify.utils.ExternalDirectory;

/**
 * This activity shows some details to a previously chosen gallery image and
 * provides some functionality as deleting or displaying the underlying 3D model
 * object.
 * 
 * @author kwolf001
 * 
 */
public class GalleryDetailsActivity extends ActionBarActivity {

	private final String TAG = "GalleryDetailsActivity";
	private ImageView picture;
	private TextView date, numberOfPics, dimension, faces, vertices;
	private String objectId, galleryId;
	private ContentResolver cr;
	private Context context;
	private Uri galleryItemUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.gallery_details);
		setTitle(getString(R.string.details));

		picture = (ImageView) findViewById(R.id.galleryDetailImage);
		date = (TextView) findViewById(R.id.dateTextview);
		numberOfPics = (TextView) findViewById(R.id.numberOfPicsTextview);
		dimension = (TextView) findViewById(R.id.dimensionsTextView);
		faces = (TextView) findViewById(R.id.facesTextView);
		vertices = (TextView) findViewById(R.id.verticesTextView);

		galleryId = String.valueOf(getIntent().getLongExtra("id", 1));
		cr = getContentResolver();
		galleryItemUri = DatabaseProvider.CONTENT_URI.buildUpon()
				.appendPath("gallery").build();

		Cursor c = cr.query(galleryItemUri, null,
				DatabaseAdapter.GALLERY_ID_KEY + "=?",
				new String[] { galleryId }, null);
		c.moveToFirst();

		String thumbnailPath = c
				.getString(DatabaseAdapter.GALLERY_THUMBNAIL_PATH_COLUMN);
		String amountPics = c
				.getString(DatabaseAdapter.GALLERY_NUMBER_OF_PICTURES_COLUMN);
		String strDate = c.getString(DatabaseAdapter.GALLERY_DATE_COLUMN);
		String strDimens = c
				.getString(DatabaseAdapter.GALLERY_DIMENSION_COLUMN);
		String strFaces = c.getString(DatabaseAdapter.GALLERY_FACES_COLUMN);
		String strVertices = c
				.getString(DatabaseAdapter.GALLERY_VERTICES_COLUMN);
		objectId = c.getString(DatabaseAdapter.GALLERY_OBJECT_ID_COLUMN);
		c.close();

		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTimeInMillis(Long.valueOf(strDate));

		Bitmap preview = BitmapFactory.decodeFile(thumbnailPath);
		if (preview == null) {
			Toast.makeText(this, getString(R.string.no_object_found),
					Toast.LENGTH_LONG).show();
		} else {
			picture.setImageBitmap(preview);
		}
		date.setText(cal.getTime().toLocaleString());
		numberOfPics.setText(amountPics);
		dimension.setText(strDimens + " " + getString(R.string.pixel));
		faces.setText(strFaces);
		vertices.setText(strVertices);
		picture.setOnClickListener(onShowclicked());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gallerydetails, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_show:
			Uri objectUri = DatabaseProvider.CONTENT_URI.buildUpon()
					.appendPath("object").build();
			new LoadingObject().execute(objectUri);
			break;
		case R.id.opt_delete:
			new DeleteObject().execute(galleryId, objectId);
			break;
		case R.id.opt_settings:
			Intent toSettings = new Intent(this, SettingsActivity.class);
			startActivity(toSettings);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private OnClickListener onShowclicked() {
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Uri objectUri = DatabaseProvider.CONTENT_URI.buildUpon()
						.appendPath("object").build();
				new LoadingObject().execute(objectUri);
			}
		};
		return listener;
	}

	private class DeleteObject extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog pleaseWait;
		private ContentResolver cr;

		@Override
		protected void onPreExecute() {
			pleaseWait = ProgressDialog.show(context, "",
					getString(R.string.please_wait), true);
			cr = getContentResolver();
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

			if ((result1 + result2) >= 2 && objDeleted && imgDeleted) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean successfullyDeleted) {
			pleaseWait.dismiss();
			if (successfullyDeleted) {
				Toast.makeText(context,
						getString(R.string.deleted_successfully),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context,
						getString(R.string.error_while_deleting),
						Toast.LENGTH_SHORT).show();
			}
			((Activity) context).finish();
		}
	}

	/**
	 * This class inherits from {@link AsyncTask} and takes care of loading a
	 * 3D-object from sd card.
	 * 
	 * @author kwolf001
	 * 
	 */
	private class LoadingObject extends AsyncTask<Uri, Void, ObjectModel> {

		private ProgressDialog pleaseWait;
		private ContentResolver cr;

		@Override
		protected void onPreExecute() {
			pleaseWait = ProgressDialog.show(context, "",
					getString(R.string.please_wait), true);
			cr = getContentResolver();
		}

		@Override
		protected ObjectModel doInBackground(Uri... params) {
			Uri objectUri = params[0];
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
}
