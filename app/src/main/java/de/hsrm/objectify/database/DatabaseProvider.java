package de.hsrm.objectify.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class DatabaseProvider extends ContentProvider {

    private static final String AUTHORITY = "de.hsrm.objectify.database.databaseprovider.content";
    private static final String CONTENT_URI_STRING = "content://" + AUTHORITY;
    public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
    private static final String TAG = "DatabaseProvider";
    private static final int GALLERY = 1;
    private static final int OBJECT = 2;

    private static final UriMatcher uriMatcher;
    private static final String VND_GALLERY_DIR = "vnd.android.cursor.dir/vnd.de.android.gallery";
    private static final String VND_GALLERY_ITEM = "vnd.android.cursor.item/vnd.de.android.gallery";
    private static final String VND_OBJECT_DIR = "vnd.android.cursor.dir/vnd.de.android.object";
    private static final String VND_OBJECT_ITEM = "vnd.android.cursor.item/vnd.de.android.object";
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "gallery", GALLERY);
        uriMatcher.addURI(AUTHORITY, "object", OBJECT);
    }
    private DatabaseAdapter dbadapter;
    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        dbadapter = new DatabaseAdapter(getContext());
        dbadapter.open();
        db = dbadapter.getDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        int code = uriMatcher.match(uri);
        switch (code) {
            case GALLERY:
                return db.query(DatabaseAdapter.DATABASE_TABLE_GALLERY, projection,
                        selection, selectionArgs, null, null, null);
            case OBJECT:
                return db.query(DatabaseAdapter.DATABASE_TABLE_OBJECT, projection,
                        selection, selectionArgs, null, null, null);
            default:
                Log.e(TAG, "uriMatcher.match(uri) error");
                return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case GALLERY:
                return VND_GALLERY_DIR;
            case OBJECT:
                return VND_OBJECT_DIR;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long rowId;
        Uri erg;
        int code = uriMatcher.match(uri);
        switch (code) {
            case GALLERY:
                rowId = db.insert(DatabaseAdapter.DATABASE_TABLE_GALLERY, null, contentValues);
                erg = (rowId > 0) ? CONTENT_URI.buildUpon().appendPath("gallery")
                        .appendPath("" + rowId).build() : null;
                return erg;
            case OBJECT:
                rowId = db.insert(DatabaseAdapter.DATABASE_TABLE_OBJECT, null, contentValues);
                erg = (rowId > 0) ? CONTENT_URI.buildUpon().appendPath("object")
                        .appendPath("" + rowId).build() : null;
                return erg;
        }

        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case GALLERY:
                return db.update(DatabaseAdapter.DATABASE_TABLE_GALLERY, values,
                        selection, selectionArgs);
            case OBJECT:
                return db.update(DatabaseAdapter.DATABASE_TABLE_OBJECT, values,
                        selection, selectionArgs);
            default:
                Log.e(TAG, "uriMatcher.match(uri) error");
                return 0;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int erg;
        switch (uriMatcher.match(uri)) {
            case GALLERY:
                erg = db.delete(DatabaseAdapter.DATABASE_TABLE_GALLERY, selection,
                        selectionArgs);
                return erg;
            case OBJECT:
                erg = db.delete(DatabaseAdapter.DATABASE_TABLE_OBJECT, selection,
                        selectionArgs);
                return erg;
        }
        return 0;
    }
}
