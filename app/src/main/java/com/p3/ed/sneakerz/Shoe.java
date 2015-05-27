package com.p3.ed.sneakerz;

import android.database.Cursor;
import android.net.Uri;
import android.util.Pair;

import java.io.File;

/**
 * Created by Ed on 5/27/15.
 */
public class Shoe {

    // Auto assigned by SQLite database
    private int _id;

    String name; // not null
    double miles;

    private Uri smallImgUri; // possibly null
    private Uri largeImgUri; // possibly null

    public Shoe() {
    }

    public int get_id() {
        return this._id;
    }

    public Pair<Uri, Uri> getImageUris() {
        return new Pair(smallImgUri, largeImgUri);
    }

    public static Shoe cursorToShoe(Cursor cursor, int[] indices) {
        Shoe shoe = new Shoe();
        shoe._id = cursor.getInt(indices[0]);
        shoe.name = cursor.getString(indices[1]);
        shoe.miles = cursor.getDouble(indices[2]);
        // Create files from paths and then build URI's (if paths are not null)
        String path;
        if ((path = cursor.getString(indices[3])) != null) {
            File smallImgFile = new File(path);
            shoe.smallImgUri = Uri.fromFile(smallImgFile);
        }
        if ((path = cursor.getString(indices[4])) != null) {
            File largeImgFile = new File(path);
            shoe.largeImgUri = Uri.fromFile(largeImgFile);
        }

        return shoe;
    }
}
