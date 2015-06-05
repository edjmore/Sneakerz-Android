package com.p3.ed.sneakerz;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by Ed on 5/27/15.
 */
public class ShoeCursorAdapter extends CursorAdapter {
    private static final String TAG = "ShoeCursorAdapter";

    private LayoutInflater mInflater;
    private int[] mIndices;
    private String mUnits;
    private Uri mDefUri;

    private BitmapManager bmpManager;

    public ShoeCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        mDefUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(R.mipmap.def_sneakers) + '/' +
                context.getResources().getResourceTypeName(R.mipmap.def_sneakers) + '/' +
                context.getResources().getResourceEntryName(R.mipmap.def_sneakers));

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mIndices = DbHelper.getColIndices(cursor, DbHelper.TABLE_SHOES);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mUnits = prefs.getString("key_pref_dist", "miles");

        bmpManager = new BitmapManager(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.shoe_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Shoe shoe = Shoe.cursorToShoe(cursor, mIndices);
        view.setId(shoe.getId());

        TextView nameView = (TextView) view.findViewById(R.id.shoe_item_name);
        nameView.setText(shoe.name);

        TextView milesView = (TextView) view.findViewById(R.id.shoe_item_dist);
        milesView.setText(shoe.getDist(mUnits));
        TextView descView = (TextView) view.findViewById(R.id.shoe_item_dist_desc);
        descView.setText(mUnits);

        final ImageView imageView = (ImageView) view.findViewById(R.id.shoe_item_image);
        // URI guaranteed to be valid
        final Uri uri = shoe.getImageUri() == null ? mDefUri : shoe.getImageUri();
        // Fetch and set image on background thread
        bmpManager.fetchBitmapAsync(uri, imageView);
    }
}
