package de.hsrm.objectify.gallery;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
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
	private TextView dateTextview, numberofpicsTextview;
	private String objectId;
	private ContentResolver cr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Fixme, falls das Bild von der SD gelöscht wurde, oder SD nicht erreichbar ist.
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_details);
		setupActionBar(getString(R.string.details), 0);
		
		picture = (ImageView) findViewById(R.id.galleryDetailImage);
		dateTextview = (TextView) findViewById(R.id.dateTextview);
		numberofpicsTextview = (TextView) findViewById(R.id.numberOfPicsTextview);
		
		String id = String.valueOf(getIntent().getLongExtra("id", 1));
		cr = getContentResolver();
		Uri galleryItemUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath("gallery").build();
		Cursor c = cr.query(galleryItemUri, null, DatabaseAdapter.GALLERY_ID_KEY+"=?", new String[] {id}, null);
		c.moveToFirst();
		String thumbnailPath = c.getString(DatabaseAdapter.GALLERY_THUMBNAIL_PATH_COLUMN);
		String numberOfPics = c.getString(DatabaseAdapter.GALLERY_NUMBER_OF_PICTURES_COLUMN);
		String strDate = c.getString(DatabaseAdapter.GALLERY_DATE_COLUMN);
		objectId = c.getString(DatabaseAdapter.GALLERY_OBJECT_ID_COLUMN);
		picture.setImageBitmap(BitmapFactory.decodeFile(thumbnailPath));
		dateTextview.setText(strDate);
		numberofpicsTextview.setText(numberOfPics);
		addNewActionButton(R.drawable.ic_title_show, R.string.show, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO: Fixme, Ladebalken oder irgendwas zeigen, weil das Laden unter Umständen länger dauern kann.
				Uri objectUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath("object").build();
				Cursor c = cr.query(objectUri, null, DatabaseAdapter.OBJECT_ID_KEY+"=?", new String[] { objectId }, null);
				c.moveToFirst();
				try {
					FileInputStream inputStream = new FileInputStream(c.getString(DatabaseAdapter.OBJECT_FILE_PATH_COLUMN));
					ObjectInputStream objInput = new ObjectInputStream(inputStream);
					ObjectModel objectModel = (ObjectModel) objInput.readObject();
					objectModel.setup();
					Intent viewObject = new Intent(v.getContext(), ObjectViewerActivity.class);
					Bundle b = new Bundle();
					b.putParcelable("objectModel", objectModel);
					viewObject.putExtra("bundle", b);
					startActivity(viewObject);
					((Activity) v.getContext()).finish();
				} catch (Exception e) {
					// TODO: Fixme, genauere Exceptions und Toast schmeißen
					Log.e(TAG, e.getLocalizedMessage());
					e.printStackTrace();
				}

			}
		});
		addNewActionButton(R.drawable.ic_title_delete, R.string.delete, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
