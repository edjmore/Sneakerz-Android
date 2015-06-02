package com.p3.ed.sneakerz;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ed on 5/27/15.
 */
public class DbHelper extends SQLiteOpenHelper {
    public static final String TAG = "DbHelper";

    // Database
    private static final String DB_SNEAKERZ = "sneakerz.db";
    private static final int DB_VERSION = 17;

    // Tables
    public static final String TABLE_SHOES = "shoes";
    public static final String TABLE_RUNS = "runs";

    // 'Shoes' columns
    public static final String SHOES_ID = "_id";
    public static final String SHOES_NAME = "name";
    public static final String SHOES_MILES = "miles";
    public static final String SHOES_IMAGE_URI = "small_img_file_path";
    // 'Runs' columns
    public static final String RUNS_ID = "_id";
    public static final String RUNS_SHOE_ID = "shoe_id";
    public static final String RUNS_MILES = "miles";
    public static final String RUNS_DATE = "date";

    private static final String DB_CREATE_SHOES = "create table " + TABLE_SHOES + "(" + SHOES_ID +
            " integer primary key autoincrement, " + SHOES_NAME + " text not null, " +
            SHOES_MILES + " double, " + SHOES_IMAGE_URI + " text);";

    private static final String DB_CREATE_RUNS = "create table " + TABLE_RUNS + "(" + RUNS_ID +
            " integer primary key autoincrement, " + RUNS_SHOE_ID + " integer, " + RUNS_MILES +
            " double, " + RUNS_DATE + " text not null);";

    public DbHelper(Context context) {
        super(context, DB_SNEAKERZ, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE_SHOES);
        db.execSQL(DB_CREATE_RUNS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RUNS);

        onCreate(db);
    }

    public static int[] getColIndices(Cursor cursor, String table) {
        if (table.equals(TABLE_SHOES)) {
            int _idCol = cursor.getColumnIndex(SHOES_ID);
            int nameCol = cursor.getColumnIndex(SHOES_NAME);
            int milesCol = cursor.getColumnIndex(SHOES_MILES);
            int imgUri = cursor.getColumnIndex(SHOES_IMAGE_URI);

            return new int[]{
                    _idCol, nameCol, milesCol, imgUri
            };

        } else { // assume client wants 'Runs' table
            int _idCol = cursor.getColumnIndex(RUNS_ID);
            int shoeIdCol = cursor.getColumnIndex(RUNS_SHOE_ID);
            int milesCol = cursor.getColumnIndex(RUNS_MILES);
            int dateCol = cursor.getColumnIndex(RUNS_DATE);

            return new int[]{
                    _idCol, shoeIdCol, milesCol, dateCol
            };
        }
    }
}
