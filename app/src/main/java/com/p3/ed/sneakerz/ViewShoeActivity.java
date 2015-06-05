package com.p3.ed.sneakerz;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.SQLException;

/**
 * Created by Ed on 6/1/15.
 */
public class ViewShoeActivity extends ActionBarActivity {

    public static final String KEY_SHOE_ID = "shoe_id";
    private Bundle mArgs;

    private int mShoeId;
    private Context mContext;
    private BitmapManager mBmpManager;

    // GUI components
    private ImageView mImageView;
    private ActionBar mActionBar;
    private TextView mDistText;
    private TextView mDistDesc;

    public static final String ACTION_ADD_RUN = "com.p3.ed.action.ADD_RUN",
            ACTION_VIEW_HIST = "com.p3.ed.action.VIEW_HIST";
    public static final String ACTION_DB_UPDATED = "com.p3.ed.action.DP_UPDATED";

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FragmentManager fm = getFragmentManager();

            switch (intent.getAction()) {
                case ACTION_ADD_RUN:
                    NewRunFrag newRunFrag = new NewRunFrag();
                    newRunFrag.setArguments(mArgs);
                    fm.beginTransaction().replace(R.id.view_shoe_frag_container, newRunFrag)
                            .commit();
                    break;

                case ACTION_VIEW_HIST:
                    RunHistFrag runHistFrag = new RunHistFrag();
                    runHistFrag.setArguments(mArgs);
                    fm.beginTransaction().replace(R.id.view_shoe_frag_container, runHistFrag)
                            .commit();

                    refresh();
                    break;

                case ACTION_DB_UPDATED:
                    refresh();
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_shoe_activity);

        // Which shoe did the user tap on?
        mShoeId = getIntent().getIntExtra(KEY_SHOE_ID, 1);

        init();
        refresh();

        FragmentManager fm = getFragmentManager();
        RunHistFrag runHistFrag = new RunHistFrag();
        mArgs = new Bundle();
        mArgs.putInt(ViewShoeActivity.KEY_SHOE_ID, mShoeId);
        runHistFrag.setArguments(mArgs);
        fm.beginTransaction().add(R.id.view_shoe_frag_container, runHistFrag).commit();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ADD_RUN);
        filter.addAction(ACTION_VIEW_HIST);
        filter.addAction(ACTION_DB_UPDATED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_shoe_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();
        Bundle args = new Bundle();
        args.putInt(KEY_SHOE_ID, mShoeId);

        switch (item.getItemId()) {
            case R.id.action_edit:
                EditShoeFrag editShoeFrag = new EditShoeFrag();
                editShoeFrag.setArguments(args);
                fm.beginTransaction().replace(R.id.view_shoe_frag_container, editShoeFrag).commit();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void init() {
        mContext = this;
        mBmpManager = new BitmapManager(mContext);
        mImageView = (ImageView) findViewById(R.id.view_shoe_image);
        mActionBar = getSupportActionBar();
        mDistText = (TextView) findViewById(R.id.view_shoe_dist);
        mDistDesc = (TextView) findViewById(R.id.view_shoe_dist_desc);
    }

    private Shoe fetchShoe() {
        DataSrc dataSrc = new DataSrc(mContext);
        try {
            dataSrc.open();
            return dataSrc.getShoe(mShoeId);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return null;
        } finally {
            if (dataSrc.isOpen()) dataSrc.close();
        }
    }

    private void refresh() {
        // Most recent shoe data
        Shoe shoe = fetchShoe();

        // Image will take the longest so start it first
        final Uri uri = shoe.getImageUri();
        // If URI is null, just stick with the default image
        if (uri != null) {
            // Load and post image on background thread
            mBmpManager.fetchBitmapAsync(uri, mImageView);
        }

        // Need user prefs for the distance units
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String units = prefs.getString(getString(R.string.pref_units), "miles");
        // Set distance text and description with appropriate units
        mDistText.setText(shoe.getDist(units));
        mDistDesc.setText(units);

        // Name of shoe
        mActionBar.setTitle(shoe.name);
    }
}
