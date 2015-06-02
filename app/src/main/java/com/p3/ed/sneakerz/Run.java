package com.p3.ed.sneakerz;

import android.database.Cursor;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ed on 6/2/15.
 */
public class Run {
    private static final String TAG = "Run";

    public double miles;
    public long date; // milliseconds since epoch

    private int _id; // auto-assigned
    private int shoeId; // the shoe used for this run

    public int getId() {
        return this._id;
    }

    public int getShoeId() {
        return this.shoeId;
    }

    public String getDist(String units) {
        DecimalFormat df = new DecimalFormat("0.0");
        if (units.equals("miles")) {
            return df.format(miles);
        } else {
            double km = miles * 1.609344; // kilometers per mile
            return df.format(km);
        }
    }

    public String getDate() {
        Date date = new Date(this.date);
        DateFormat df = DateFormat.getDateInstance();

        return df.format(date);
    }

    public static Run cursorToRun(Cursor cursor, int[] indices) {
        Run run = new Run();

        run._id = cursor.getInt(indices[0]);
        run.shoeId = cursor.getInt(indices[1]);
        run.miles = cursor.getDouble(indices[2]);
        run.date = cursor.getLong(indices[3]);

        return run;
    }

    @Override
    public String toString() {
        return "Miles: " + miles + '\n' + "Date: " + this.getDate() + '\n' + "Shoe ID: " + shoeId +
                '\n' + "ID: " + _id;
    }
}
