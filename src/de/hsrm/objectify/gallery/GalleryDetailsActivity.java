package de.hsrm.objectify.gallery;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.hsrm.objectify.R;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.database.DatabaseProvider;
import de.hsrm.objectify.rendering.ObjectModel;
import de.hsrm.objectify.rendering.ObjectViewerActivity;
import de.hsrm.objectify.ui.BaseActivity;

/**
 * This activity shows some details to a previously chosen gallery image and
 * provides some functionality as deleting or displaying the underlying 3D model
 * object.
 * 
 * @author kwolf001
 * 
 */
public class GalleryDetailsActivity extends BaseActivity {

	private final String TAG = "GalleryDetailsActivity";
	private ImageView picture;
	private TextView date, numberOfPics;
	private String objectId;
	private ContentResolver cr;
	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.gallery_details);
		setupActionBar(getString(R.string.details), 0);
		
		picture = (ImageView) findViewById(R.id.galleryDetailImage);
		date = (TextView) findViewById(R.id.dateTextview);
		numberOfPics = (TextView) findViewById(R.id.numberOfPicsTextview);
		
		String id = String.valueOf(getIntent().getLongExtra("id", 1));
		cr = getContentResolver();
		Uri galleryItemUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath("gallery").build();
		Cursor c = cr.query(galleryItemUri, null, DatabaseAdapter.GALLERY_ID_KEY+"=?", new String[] {id}, null);
		c.moveToFirst();
		
		String thumbnailPath = c.getString(DatabaseAdapter.GALLERY_THUMBNAIL_PATH_COLUMN);
		String amountPics = c.getString(DatabaseAdapter.GALLERY_NUMBER_OF_PICTURES_COLUMN);
		String strDate = c.getString(DatabaseAdapter.GALLERY_DATE_COLUMN);
		
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTimeInMillis(Long.valueOf(strDate));
		
		objectId = c.getString(DatabaseAdapter.GALLERY_OBJECT_ID_COLUMN);
		Bitmap preview = BitmapFactory.decodeFile(thumbnailPath);
		if (preview == null) {
			Toast.makeText(this, getString(R.string.no_object_found), Toast.LENGTH_LONG).show();
		} else {
			picture.setImageBitmap(preview);
		}
		date.setText(cal.getTime().toLocaleString());
		numberOfPics.setText(amountPics);
		
		addNewActionButton(R.drawable.ic_title_show, R.string.show, onShowclicked());
		addNewActionButton(R.drawable.ic_title_delete, R.string.delete, onDeleteclicked());
	}
	
	private OnClickListener onDeleteclicked() {
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		};
		return listener;
	}
	
	private OnClickListener onShowclicked() {
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				Uri objectUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath("object").build();
				new LoadingObject().execute(objectUri);
			}
		};
		return listener;
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
		@Override
		protected void onPreExecute() {
			pleaseWait = ProgressDialog.show(context, "", getString(R.string.please_wait), true);
		}

		@Override
		protected ObjectModel doInBackground(Uri... params) {
			Uri objectUri = params[0];
			ObjectModel objectModel = null;
			Cursor c = cr.query(objectUri, null, DatabaseAdapter.OBJECT_ID_KEY+"=?", new String[] { objectId }, null);
			c.moveToFirst();
			try {
				FileInputStream inputStream = new FileInputStream(c.getString(DatabaseAdapter.OBJECT_FILE_PATH_COLUMN));
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
			if (result != null) {
				Intent viewObject = new Intent(context, ObjectViewerActivity.class);
				Bundle b = new Bundle();
				b.putParcelable("objectModel", result);
				viewObject.putExtra("bundle", b);
				
				pleaseWait.dismiss();
				startActivity(viewObject);
				((Activity) context).finish();
			} else {
				Toast.makeText(context, getString(R.string.error_while_reading), Toast.LENGTH_LONG).show();
			}
		}
		
	}
}
