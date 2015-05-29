package com.p3.ed.sneakerz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.sql.SQLException;

/**
 * Created by Ed on 5/27/15.
 */
public class DataSrc {
    public static final String TAG = "DataSrc";

    private DbHelper mDbhelper;
    private SQLiteDatabase mDb;

    public static final String[] ALL_SHOE_COLUMNS = {
            DbHelper.SHOES_ID, DbHelper.SHOES_NAME, DbHelper.SHOES_MILES,
            DbHelper.SHOES_SMALL_IMG_FILE_PATH, DbHelper.SHOES_LARGE_IMG_FILE_PATH
    };

    public DataSrc(Context context) {
        mDbhelper = new DbHelper(context);
    }

    public void open() throws SQLException {
        mDb = mDbhelper.getWritableDatabase();
        open = true;
    }

    private boolean open;

    public void close() {
        if (open) mDb.close();
        open = false;
    }

    public Shoe addShoe(String name) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.SHOES_NAME, name);
        // Always start at zero miles
        values.put(DbHelper.SHOES_MILES, 0);

        // Insert new shoe into table
        long _id = mDb.insert(DbHelper.TABLE_SHOES, null, values);
        // Use the auto assigned ID to retrieve the added shoe
        String selectClause = DbHelper.SHOES_ID + " = ?";
        Cursor cursor = mDb.query(DbHelper.TABLE_SHOES, ALL_SHOE_COLUMNS, selectClause,
                new String[]{String.valueOf(_id)}, null, null, null);
        // Cursor starts at index -1
        cursor.moveToNext();

        // Get column indices
        int[] indices = DbHelper.getColIndices(cursor, DbHelper.TABLE_SHOES);

        // Return the shoe so we know its ID
        return Shoe.cursorToShoe(cursor, indices);
    }

    public void setImageUris(Uri smallImg, Uri largeImg, int _id) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.SHOES_SMALL_IMG_FILE_PATH, smallImg.getPath());
        values.put(DbHelper.SHOES_LARGE_IMG_FILE_PATH, largeImg.getPath());

        // Update the row with the provided ID
        String whereClause = DbHelper.SHOES_ID + " = ?";
        mDb.update(DbHelper.TABLE_SHOES, values, whereClause, new String[]{String.valueOf(_id)});
    }

    public void deleteShoe(int _id) {
        String whereClause = DbHelper.SHOES_ID + " = ?";
        mDb.delete(DbHelper.TABLE_SHOES, whereClause, new String[]{String.valueOf(_id)});
    }

    public void updateShoe(String newName, double newMiles, int _id) {
        // Only update name and miles (use 'setImageUris' to update images)
        ContentValues values = new ContentValues();
        values.put(DbHelper.SHOES_NAME, newName);
        values.put(DbHelper.SHOES_MILES, newMiles);

        String whereClause = DbHelper.SHOES_ID + " = ?";
        mDb.update(DbHelper.TABLE_SHOES, values, whereClause, new String[]{String.valueOf(_id)});
    }

    public Cursor getAllShoes() {
        return mDb.query(DbHelper.TABLE_SHOES, ALL_SHOE_COLUMNS, null, null, null, null,
                DbHelper.SHOES_MILES);
    }

    public Shoe getShoe(int _id) {
        String selectClause = DbHelper.SHOES_ID + " = ?";
        Cursor cursor = mDb.query(DbHelper.TABLE_SHOES, ALL_SHOE_COLUMNS, selectClause,
                new String[]{String.valueOf(_id)}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToNext();
            int[] indices = DbHelper.getColIndices(cursor, DbHelper.TABLE_SHOES);

            return Shoe.cursorToShoe(cursor, indices);
        }

        // Something went wrong
        return null;
    }
}
