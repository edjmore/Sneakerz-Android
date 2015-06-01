package com.p3.ed.sneakerz;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * Created by Ed on 5/27/15.
 */
public class ShoeCursorAdapter extends CursorAdapter {
    private static final String TAG = "ShoeCursorAdapter";

    private LayoutInflater mInflater;
    private int[] mIndices;
    private String mUnits;

    public ShoeCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mIndices = DbHelper.getColIndices(cursor, DbHelper.TABLE_SHOES);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mUnits = prefs.getString("key_pref_dist", "miles");
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

        ImageView imgView = (ImageView) view.findViewById(R.id.shoe_item_image);
        imgView.setImageURI(shoe.getImageUri());
    }
}
