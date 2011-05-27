package de.hsrm.objectify.gallery;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView;
import de.hsrm.objectify.R;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.utils.ImageHelper;

public class GalleryAdapter extends CursorAdapter {
	
	public Cursor cursor;
	private int galleryItemBackground;
	
	public GalleryAdapter(Context context, Cursor c) {
		super(context, c);
		this.cursor = c;
		TypedArray a = context.obtainStyledAttributes(R.styleable.galleryStyle);
		galleryItemBackground = a.getResourceId(R.styleable.galleryStyle_android_galleryItemBackground, 0);
		a.recycle();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView image = (ImageView) view;
		byte[] imageData = cursor.getBlob(DatabaseAdapter.GALLERY_IMAGE_COLUMN);
		image.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {		
		ImageView imageView = new ImageView(context);
		int size = ImageHelper.dipToPx(100, context);
		imageView.setLayoutParams(new Gallery.LayoutParams(size, size));
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		imageView.setBackgroundResource(galleryItemBackground);
		
		return imageView;
	}

}
