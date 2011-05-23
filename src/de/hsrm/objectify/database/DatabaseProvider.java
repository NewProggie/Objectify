package de.hsrm.objectify.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import de.hsrm.objectify.gallery.GalleryActivity;

/**
 * Provider that stores gallery data. Data is usually inserted by
 * {@link CameraActivity} and queried by {@link GalleryActivity}.
 * 
 * @author kwolf001
 * 
 */
public class DatabaseProvider extends ContentProvider {

	private static final String AUTHORITY = "de.hsrm.objectify.databaseprovider.content";
	private static final String TAG = "DatabaseProvider";
	private static final String CONTENT_URI_STRING = "content://" + AUTHORITY;
	public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
	
	private static final int GALLERY = 1;
	
	private static final UriMatcher uriMatcher;
	
	private DatabaseAdapter dbadapter;
	private SQLiteDatabase db;
	
	private static final String VND_GALLERY_DIR = "vnd.android.cursor.dir/vnd.de.android.gallery";
	private static final String VND_GALLERY_ITEM = "vnd.android.cursor.item/vnd.de.android.gallery";
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "gallery", GALLERY);
	}
	
	@Override
	public boolean onCreate() {
		dbadapter = new DatabaseAdapter(getContext());
		dbadapter.open();
		db = dbadapter.getDatabase();
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case GALLERY:
			return VND_GALLERY_DIR;
		default:
			throw new IllegalArgumentException("Unsuppoerted Uri: " + uri);
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId;
		Uri erg;
		int code = uriMatcher.match(uri);
		switch (code) {
		case GALLERY:
			rowId = db.insert(DatabaseAdapter.DATABASE_TABLE_GALLERY, null, values);
			erg = (rowId > 0) ? CONTENT_URI.buildUpon().appendPath("gallery").build() : null;
			return erg;
		}
		return null;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		int code = uriMatcher.match(uri);
		switch (code) {
		case GALLERY:
			return db.query(DatabaseAdapter.DATABASE_TABLE_GALLERY, projection, selection, selectionArgs, null, null, null);
		default:
			Log.e(TAG, "uriMatcher.match(uri) error");
			return null;
		}
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch (uriMatcher.match(uri)) {
		case GALLERY:
			return db.update(DatabaseAdapter.DATABASE_TABLE_GALLERY, values, selection, selectionArgs);
		default:
			Log.e(TAG, "uriMatcher.match(uri) error");
			return 0;
		}
	}
	
	/** {@inheritDoc} **/
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (uriMatcher.match(uri)) {
		case GALLERY:
			Cursor c = db.query(DatabaseAdapter.DATABASE_TABLE_GALLERY, null, selection, selectionArgs, null, null, null);
			c.moveToFirst();
			String table = c.getString(DatabaseAdapter.GALLERY_ID_COLUMN);
			c.close();
			return db.delete(table, selection, selectionArgs);
		}
		return 0;
	}
}
