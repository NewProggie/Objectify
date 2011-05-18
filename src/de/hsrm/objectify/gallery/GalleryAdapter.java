package de.hsrm.objectify.gallery;

import de.hsrm.objectify.R;
import de.hsrm.objectify.database.DatabaseAdapter;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GalleryAdapter extends CursorAdapter {
	
	private static final String TAG  = "GalleryAdapter";
	private Context context;
	private Cursor cursor;
	private LayoutInflater inflater;
	
	public GalleryAdapter(Context context, Cursor c) {
		super(context, c);
		this.context = context;
		this.cursor = c;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView image = (ImageView) view.findViewById(R.id.gallery_image);
		String imagePath = cursor.getString(DatabaseAdapter.GALLERY_IMAGE_PATH_COLUMN);
		image.setImageBitmap(BitmapFactory.decodeFile(imagePath));
		
		TextView tvSize = (TextView) view.findViewById(R.id.gallery_size_textview);
		tvSize.setText(cursor.getString(DatabaseAdapter.GALLERY_SIZE_COLUMN));
		
		TextView tvFaces = (TextView) view.findViewById(R.id.gallery_faces_textview);
		tvFaces.setText(cursor.getString(DatabaseAdapter.GALLERY_FACES_COLUMN));
		
		TextView tvVertices = (TextView) view.findViewById(R.id.gallery_vertices_textview);
		tvVertices.setText(cursor.getString(DatabaseAdapter.GALLERY_VERTICES_COLUMN));
		
		TextView tvDimension = (TextView) view.findViewById(R.id.gallery_dimension_textview);
		tvDimension.setText(cursor.getString(DatabaseAdapter.GALLERY_DIMENSIONS_COLUMN));
		
		TextView tvDate = (TextView) view.findViewById(R.id.gallery_date_textview);
		tvDate.setText(cursor.getString(DatabaseAdapter.GALLERY_DATE_COLUMN));
	}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View view = inflater.inflate(R.layout.gallery_item, null);
		return view;
	}

}
