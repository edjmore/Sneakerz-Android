package com.p3.ed.sneakerz;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ed on 5/27/15.
 */
public class DataSrc {
    private static final String TAG = "DataSrc";

    private DbHelper mDbhelper;
    private SQLiteDatabase mDb;
    private boolean mOpen;

    public static final String[] ALL_SHOE_COLUMNS = {
            DbHelper.SHOES_ID, DbHelper.SHOES_NAME, DbHelper.SHOES_MILES,
            DbHelper.SHOES_IMAGE_URI
    };

    public static final String[] ALL_RUN_COLUMNS = {
            DbHelper.RUNS_ID, DbHelper.RUNS_SHOE_ID, DbHelper.RUNS_MILES,
            DbHelper.RUNS_DATE
    };

    public DataSrc(Context context) {
        mDbhelper = new DbHelper(context);
    }

    public void open() throws SQLException {
        mDb = mDbhelper.getWritableDatabase();
        mOpen = true;
    }

    public boolean isOpen() {
        return mOpen;
    }

    public void close() {
        mDb.close();
        mOpen = false;
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

    public void setImageUri(Uri imgUri, int _id) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.SHOES_IMAGE_URI, imgUri.getPath());

        // Update the row with the provided ID
        String whereClause = DbHelper.SHOES_ID + " = ?";
        mDb.update(DbHelper.TABLE_SHOES, values, whereClause, new String[]{String.valueOf(_id)});
    }

    public void deleteShoe(int _id) {
        String whereClause = DbHelper.SHOES_ID + " = ?";
        mDb.delete(DbHelper.TABLE_SHOES, whereClause, new String[]{String.valueOf(_id)});
    }

    public void updateShoe(String newName, double newMiles, int _id) {
        // Only update name and miles (use 'setImageUri' to update image)
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

    public Run addRun(int shoeId, double miles) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.RUNS_SHOE_ID, shoeId);
        values.put(DbHelper.RUNS_MILES, miles);
        long date = System.currentTimeMillis();
        values.put(DbHelper.RUNS_DATE, date);

        long _id = mDb.insert(DbHelper.TABLE_RUNS, null, values);
        // Retrieve the inserted run from table
        String selectClause = DbHelper.RUNS_ID + " = ?";
        Cursor cursor = mDb.query(DbHelper.TABLE_RUNS, ALL_RUN_COLUMNS, selectClause,
                new String[]{String.valueOf(_id)}, null, null, null);
        cursor.moveToNext();

        int[] indices = DbHelper.getColIndices(cursor, DbHelper.TABLE_RUNS);

        return Run.cursorToRun(cursor, indices);
    }

    public Run getRun(int runId) {
        String selectClause = DbHelper.RUNS_ID + " = ?";
        Cursor cursor = mDb.query(DbHelper.TABLE_RUNS, ALL_RUN_COLUMNS, selectClause,
                new String[]{String.valueOf(runId)}, null, null, null);
        cursor.moveToNext();
        int[] indices = DbHelper.getColIndices(cursor, DbHelper.TABLE_RUNS);

        return Run.cursorToRun(cursor, indices);
    }

    public void deleteRun(int _id) {
        String whereClause = DbHelper.RUNS_ID + " = ?";
        mDb.delete(DbHelper.TABLE_RUNS, whereClause, new String[]{String.valueOf(_id)});
    }

    public Cursor getAllRunsForShoe(int shoeId) {
        String selectClause = DbHelper.RUNS_SHOE_ID + " = ?";
        // Sort by date (most recent first)
        String orderBy = DbHelper.RUNS_DATE + " DESC";

        return mDb.query(DbHelper.TABLE_RUNS, ALL_RUN_COLUMNS, selectClause,
                new String[]{String.valueOf(shoeId)}, null, null, orderBy);
    }
}
