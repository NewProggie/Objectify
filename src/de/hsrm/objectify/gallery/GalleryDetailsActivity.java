package de.hsrm.objectify.gallery;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import de.hsrm.objectify.R;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.database.DatabaseProvider;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_details);
		setupActionBar(getString(R.string.details), 0);
		
		picture = (ImageView) findViewById(R.id.galleryDetailImage);
		dateTextview = (TextView) findViewById(R.id.dateTextview);
		numberofpicsTextview = (TextView) findViewById(R.id.numberOfPicsTextview);
		
		String id = String.valueOf(getIntent().getLongExtra("id", 1));
		ContentResolver cr = getContentResolver();
		Uri galleryItemUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath("gallery").build();
		Cursor c = cr.query(galleryItemUri, null, DatabaseAdapter.GALLERY_ID_KEY+"=?", new String[] {id}, null);
		c.moveToFirst();
		String thumbnailPath = c.getString(DatabaseAdapter.GALLERY_THUMBNAIL_PATH_COLUMN);
		String numberOfPics = c.getString(DatabaseAdapter.GALLERY_NUMBER_OF_PICTURES_COLUMN);
		String strDate = c.getString(DatabaseAdapter.GALLERY_DATE_COLUMN);
		String objectId = c.getString(DatabaseAdapter.GALLERY_OBJECT_ID_COLUMN);
		picture.setImageBitmap(BitmapFactory.decodeFile(thumbnailPath));
		dateTextview.setText(strDate);
		numberofpicsTextview.setText(numberOfPics);
		addNewActionButton(R.drawable.ic_title_show, R.string.show, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
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
