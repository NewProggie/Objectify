package de.hsrm.objectify.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * Adapter class for creating the database and tables
 * 
 * @author kwolf001
 * 
 */
public class DatabaseAdapter {

	private static final String DATABASE_NAME = "objectify.db";
	private static final int DATABASE_VERSION = 1;
	
	static final String DATABASE_TABLE_GALLERY = "gallery";
	public static final String GALLERY_ID_KEY = "_id";
	public static final int GALLERY_ID_COLUMN = 0;
	public static final String GALLERY_IMAGE_PATH_KEY = "image_path";
	public static final int GALLERY_IMAGE_PATH_COLUMN = 1;
	public static final String GALLERY_SIZE_KEY = "size";
	public static final int GALLERY_SIZE_COLUMN = 2;
	public static final String GALLERY_FACES_KEY = "faces";
	public static final int GALLERY_FACES_COLUMN = 3;
	public static final String GALLERY_VERTICES_KEY = "vertices";
	public static final int GALLERY_VERTICES_COLUMN = 4;
	public static final String GALLERY_DIMENSIONS_KEY = "dimensions";
	public static final int GALLERY_DIMENSIONS_COLUMN = 5;
	public static final String GALLERY_DATE_KEY = "date";
	public static final int GALLERY_DATE_COLUMN = 6;
	public static final String GALLERY_SUFFIX_KEY = "suffix";
	public static final int GALLERY_SUFFIX_COLUMN = 7;
	
	private static final String DATABASE_TABLE_GALLERY_CREATE = "CREATE TABLE " + DATABASE_TABLE_GALLERY + " (" +
		GALLERY_ID_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", " + GALLERY_IMAGE_PATH_KEY + " TEXT NOT NULL" + ", " +
		GALLERY_SIZE_KEY + " TEXT NOT NULL" + ", " + GALLERY_FACES_KEY + " TEXT NOT NULL" + ", " +
		GALLERY_VERTICES_KEY + " TEXT NOT NULL" + ", " + GALLERY_DIMENSIONS_KEY + " TEXT NOT NULL" + ", " + 
		GALLERY_DATE_KEY + " TEXT NOT NULL" + "," + GALLERY_SUFFIX_KEY + " TEXT NOT NULL" + ")";
	
	private static SQLiteDatabase db;
	private DatabaseHelper databaseHelper;
	
	public DatabaseAdapter(Context context) {
		databaseHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public void open() throws SQLException {
		try {
			db = databaseHelper.getWritableDatabase();
		} catch (SQLException e) {
			db = databaseHelper.getReadableDatabase();
		}
	}
	
	public void close() {
		db.close();
	}
	
	public SQLiteDatabase getDatabase() {
		return db;
	}
	
	/**
	 * Helper for managing {@link SQLiteDatabase} that stores data for
	 * {@link DatabaseProvider}.
	 * 
	 * @author kwolf001
	 * 
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_TABLE_GALLERY_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS" + DATABASE_TABLE_GALLERY);
		}
	}
}
