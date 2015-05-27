package com.p3.ed.sneakerz;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;

/**
 * Created by Ed on 5/27/15.
 */
public class ShoeCursorAdapter extends CursorAdapter {
    public static final String TAG = "ShoeCursorAdapter";

    private LayoutInflater mInflater;

    private int[] mIndices;

    public ShoeCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);

        // Initialize view inflater
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Get column indices so we can easily construct shoes
        mIndices = DbHelper.getColIndices(cursor, DbHelper.TABLE_SHOES);
        String s = "";
        for (int i = 0; i < mIndices.length; i++) {
            s += mIndices[i];
        }
        Log.d(TAG, s);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.shoe_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Get references for all child views
        TextView nameView = (TextView) view.findViewById(R.id.shoe_item_name);
        TextView milesView = (TextView) view.findViewById(R.id.shoe_item_miles);
        ImageView smallImgView = (ImageView) view.findViewById(R.id.shoe_item_image);

        // Use a shoe object to populate the child views
        Shoe shoe = Shoe.cursorToShoe(cursor, mIndices);
        nameView.setText(shoe.name);
        // Format distance to one decimal place
        DecimalFormat df = new DecimalFormat("0.0");
        milesView.setText(df.format(shoe.miles));
        // Load the small image from its URI
        try {
            Pair<Uri, Uri> imgUris = shoe.getImageUris();
            // If URI is null, just keep the default image from XML
            if (imgUris.first != null) {
                InputStream in = context.getContentResolver().openInputStream(imgUris.first);
                Bitmap bmp = BitmapFactory.decodeStream(in);
                smallImgView.setImageBitmap(bmp);
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
    }
}
