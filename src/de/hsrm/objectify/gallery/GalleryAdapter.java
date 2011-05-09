package de.hsrm.objectify.gallery;

import java.io.File;
import java.util.ArrayList;

import de.hsrm.objectify.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class GalleryAdapter extends BaseAdapter {
	
	private static final String TAG  = "GalleryAdapter";
	private Context context;
	private final float SCALE;
	private File[] images;
	private int galleryItemBackground;
	
	public GalleryAdapter(Context context, File[] images) {
		this.context = context;
		this.images = images;
		SCALE = context.getResources().getDisplayMetrics().density;
		TypedArray a = context.obtainStyledAttributes(R.styleable.galleryStyle);
		galleryItemBackground = a.getResourceId(R.styleable.galleryStyle_android_galleryItemBackground, 0);
		a.recycle();
	}

	@Override
	public int getCount() {
		return images.length;
	}

	@Override
	public Object getItem(int position) {
		return images[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		File file = images[position];
		Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
		ImageView img = new ImageView(context);
		int width = (int) (250 * SCALE + 0.5f);
		int height = (int) (200 * SCALE + 0.5f);
		img.setLayoutParams(new Gallery.LayoutParams(width, height));
		img.setScaleType(ImageView.ScaleType.FIT_XY);
		img.setBackgroundResource(galleryItemBackground);
		img.setImageBitmap(bmp);
		return img;
	}

}
