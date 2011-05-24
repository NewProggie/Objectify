package de.hsrm.objectify.gallery;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView;
import de.hsrm.objectify.R;
import de.hsrm.objectify.database.DatabaseAdapter;

public class GalleryAdapter extends CursorAdapter {
	
	private static final String TAG  = "GalleryAdapter";
	private Context context;
	private Cursor cursor;
	private int galleryItemBackground;
	
	public GalleryAdapter(Context context, Cursor c) {
		super(context, c);
		this.context = context;
		this.cursor = c;
		TypedArray a = context.obtainStyledAttributes(R.styleable.galleryStyle);
		galleryItemBackground = a.getResourceId(R.styleable.galleryStyle_android_galleryItemBackground, 0);
		a.recycle();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView image = (ImageView) view;
		String imagePath = cursor.getString(DatabaseAdapter.GALLERY_IMAGE_PATH_COLUMN);
		image.setImageBitmap(BitmapFactory.decodeFile(imagePath));
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		ImageView imageView = new ImageView(context);
		imageView.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		imageView.setBackgroundResource(galleryItemBackground);
		
		return imageView;
	}

}
