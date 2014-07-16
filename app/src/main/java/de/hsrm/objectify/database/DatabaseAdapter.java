package de.hsrm.objectify.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;

/** @brief Database adapter responsible for creation of db tables and updating db
 *
 * This class manages several database tables which look like following:
 * @code
 * +-----------------+  +--------------------------------------------------------------------+
 * |      object     |  |                           gallery                                  |
 * +-----+-----------+  +-----+------------+------+-----------+-------+----------+-----------+
 * | _id | file_path |  | _id | image_path | date | dimension | faces | vertices | object_id |
 * +-----+-----------+  +-----+------------+------+-----------+-------+----------+-----------+
 * @endcode
 */
public class DatabaseAdapter {

    private static final String DATABASE_NAME               = "objectify.db";
    private static final int DATABASE_VERSION               = 1;

    public static final String DATABASE_TABLE_OBJECT        = "object";
    public static final String OBJECT_ID_KEY                = "_id";
    public static final int OBJECT_ID_COLUMN                = 0;
    public static final String OBJECT_FILE_PATH_KEY         = "file_path";
    public static final int OBJECT_FILE_PATH_COLUMN         = 1;

    public static final String DATABASE_TABLE_GALLERY       = "gallery";
    public static final String GALLERY_ID_KEY               = "_id";
    public static final int GALLERY_ID_COLUMN               = 0;
    public static final String GALLERY_IMAGE_PATH_KEY       = "image_path";
    public static final int GALLERY_IMAGE_PATH_COLUMN       = 1;
    public static final String GALLERY_DATE_KEY             = "date";
    public static final int GALLERY_DATE_COLUMN             = 2;
    public static final String GALLERY_DIMENSION_KEY        = "dimension";
    public static final int GALLERY_DIMENSION_COLUMN        = 3;
    public static final String GALLERY_FACES_KEY            = "faces";
    public static final int GALLERY_FACES_COLUMN            = 4;
    public static final String GALLERY_VERTICES_KEY         = "vertices";
    public static final int GALLERY_VERTICES_COLUMN         = 5;
    public static final String GALLERY_OBJECT_ID_KEY        = "object_id";
    public static final int GALLERY_OBJECT_ID_COLUMN        = 6;

    private static final String DATABASE_TABLE_OBJECT_CREATE = "CREATE TABLE "
            + DATABASE_TABLE_OBJECT
            +                               " ("
            + OBJECT_ID_KEY +               " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + OBJECT_FILE_PATH_KEY +        " TEXT NOT NULL"
            +                               ")";

    private static final String DATABASE_TABLE_GALLERY_CREATE = "CREATE TABLE "
            + DATABASE_TABLE_GALLERY
            +                               " ("
            + GALLERY_ID_KEY +              " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GALLERY_IMAGE_PATH_KEY +      " TEXT NOT NULL, "
            + GALLERY_DATE_KEY +            " TEXT NOT NULL, "
            + GALLERY_DIMENSION_KEY +       " TEXT NOT NULL, "
            + GALLERY_FACES_KEY +           " TEXT NOT NULL, "
            + GALLERY_VERTICES_KEY +        " TEXT NOT NULL, "
            + GALLERY_OBJECT_ID_KEY +       " INTEGER REFERENCES " + DATABASE_TABLE_OBJECT
            +                               ")";

    private static SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public DatabaseAdapter(Context context) {
        dbHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = dbHelper.getReadableDatabase();
        }
    }

    public void close() {
        db.close();
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    /**
     * Helper class for managing {@link android.database.sqlite.SQLiteDatabase} that stores data for
     * {@link DatabaseProvider}
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                              int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_TABLE_OBJECT_CREATE);
            db.execSQL(DATABASE_TABLE_GALLERY_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_GALLERY);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_OBJECT);
        }
    }
}
