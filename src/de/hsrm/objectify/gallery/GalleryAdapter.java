package de.hsrm.objectify.gallery;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import de.hsrm.objectify.R;
import de.hsrm.objectify.database.DatabaseAdapter;

public class GalleryAdapter extends CursorAdapter {
	
	public Cursor cursor;
	
	public GalleryAdapter(Context context, Cursor c) {
		super(context, c);
		this.cursor = c;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView image = (ImageView) view;
		String pathName = cursor.getString(DatabaseAdapter.GALLERY_THUMBNAIL_PATH_COLUMN);
		// TODO: Abfangen, falls bitmap null, weil z.B. SD nicht lesbar ist
		Bitmap bitmap = BitmapFactory.decodeFile(pathName);
		image.setImageBitmap(bitmap);
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {		
		ImageView imageView = new ImageView(context);
		imageView.setLayoutParams(new GridView.LayoutParams(110,110));
		imageView.setScaleType(ScaleType.FIT_XY);
		imageView.setPadding(2, 2, 2, 0);
		imageView.setBackgroundResource(R.drawable.polaroid_frame);
		return imageView;
	}

}
