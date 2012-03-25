package de.hsrm.objectify.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Adapter class for creating the database and tables
 * 
 * @author kwolf001
 * 
 */
public class DatabaseAdapter {

	private static final String DATABASE_NAME = "objectify.db";
	private static final int DATABASE_VERSION = 1;

	static final String DATABASE_TABLE_OBJECT = "object";
	public static final String OBJECT_ID_KEY = "_id";
	public static final int OBJECT_ID_COLUMN = 0;
	public static final String OBJECT_FILE_PATH_KEY = "file_path";
	public static final int OBJECT_FILE_PATH_COLUMN = 1;

	static final String DATABASE_TABLE_GALLERY = "gallery";
	public static final String GALLERY_ID_KEY = "_id";
	public static final int GALLERY_ID_COLUMN = 0;
	public static final String GALLERY_THUMBNAIL_PATH_KEY = "thumbnail_path";
	public static final int GALLERY_THUMBNAIL_PATH_COLUMN = 1;
	public static final String GALLERY_NUMBER_OF_PICTURES_KEY = "number_of_pictures";
	public static final int GALLERY_NUMBER_OF_PICTURES_COLUMN = 2;
	public static final String GALLERY_DATE_KEY = "date";
	public static final int GALLERY_DATE_COLUMN = 3;
	public static final String GALLERY_DIMENSION_KEY = "dimension";
	public static final int GALLERY_DIMENSION_COLUMN = 4;
	public static final String GALLERY_FACES_KEY = "faces";
	public static final int GALLERY_FACES_COLUMN = 5;
	public static final String GALLERY_VERTICES_KEY = "vertices";
	public static final int GALLERY_VERTICES_COLUMN = 6;
	public static final String GALLERY_OBJECT_ID_KEY = "object_id";
	public static final int GALLERY_OBJECT_ID_COLUMN = 7;

	private static final String DATABASE_TABLE_OBJECT_CREATE = "CREATE TABLE "
			+ DATABASE_TABLE_OBJECT + " (" + OBJECT_ID_KEY
			+ " INTEGER PRIMARY KEY AUTOINCREMENT" + ", "
			+ OBJECT_FILE_PATH_KEY + " TEXT NOT NULL" + ")";

	private static final String DATABASE_TABLE_GALLERY_CREATE = "CREATE TABLE "
			+ DATABASE_TABLE_GALLERY + " (" + GALLERY_ID_KEY
			+ " INTEGER PRIMARY KEY AUTOINCREMENT" + ", "
			+ GALLERY_THUMBNAIL_PATH_KEY + " TEXT NOT NULL" + ", "
			+ GALLERY_NUMBER_OF_PICTURES_KEY + " TEXT NOT NULL" + ", "
			+ GALLERY_DATE_KEY + " TEXT NOT NULL" + "," + GALLERY_DIMENSION_KEY
			+ " TEXT NOT NULL" + ", " + GALLERY_FACES_KEY + " TEXT NOT NULL"
			+ ", " + GALLERY_VERTICES_KEY + " TEXt NOT NULL" + ", "
			+ GALLERY_OBJECT_ID_KEY + " INTEGER REFERENCES"
			+ DATABASE_TABLE_OBJECT + ")";

	private static SQLiteDatabase db;
	private DatabaseHelper databaseHelper;

	public DatabaseAdapter(Context context) {
		databaseHelper = new DatabaseHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
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

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_TABLE_OBJECT_CREATE);
			db.execSQL(DATABASE_TABLE_GALLERY_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS" + DATABASE_TABLE_GALLERY);
			db.execSQL("DROP TABLE IF EXISTS" + DATABASE_TABLE_OBJECT);
		}
	}
}
