package com.p3.ed.sneakerz;

import android.database.Cursor;
import android.net.Uri;
import android.util.Pair;

import java.io.File;

/**
 * Created by Ed on 5/27/15.
 */
public class Shoe {
    private static final String TAG = "Shoe";
    
    public String name; // not null
    public double miles;

    private int _id; // auto-assigned
    private Uri imgUri; // possibly null

    public Shoe() {
    }

    public int getId() {
        return this._id;
    }

    public Uri getImageUri() {
        return imgUri;
    }

    public static Shoe cursorToShoe(Cursor cursor, int[] indices) {
        Shoe shoe = new Shoe();

        shoe._id = cursor.getInt(indices[0]);
        shoe.name = cursor.getString(indices[1]);
        shoe.miles = cursor.getDouble(indices[2]);

        String path;
        if ((path = cursor.getString(indices[3])) != null) {
            File imgFile = new File(path);
            shoe.imgUri = Uri.fromFile(imgFile);
        }

        return shoe;
    }

    @Override
    public String toString() {
        return "Name: " + this.name + '\n' + "Miles: " + this.miles + '\n' + "Image URI: " +
                this.imgUri + '\n' + "ID: " + this._id;
    }
}
