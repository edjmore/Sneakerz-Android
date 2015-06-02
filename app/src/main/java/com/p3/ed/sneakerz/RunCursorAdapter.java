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

import org.w3c.dom.Text;

/**
 * Created by Ed on 6/2/15.
 */
public class RunCursorAdapter extends CursorAdapter {
    private static final String TAG = "RunCursorAdapter";

    private LayoutInflater mInflater;
    private int[] mIndices;
    private String mUnits;

    public RunCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mIndices = DbHelper.getColIndices(cursor, DbHelper.TABLE_RUNS);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mUnits = prefs.getString("key_pref_dist", "miles");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.run_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Run run = Run.cursorToRun(cursor, mIndices);

        TextView distView = (TextView) view.findViewById(R.id.run_item_dist);
        distView.setText(run.getDist(mUnits));

        TextView distDescView = (TextView) view.findViewById(R.id.run_item_dist_desc);
        distDescView.setText(mUnits);

        TextView dateView = (TextView) view.findViewById(R.id.run_item_date);
        dateView.setText(run.getDate());
    }
}
