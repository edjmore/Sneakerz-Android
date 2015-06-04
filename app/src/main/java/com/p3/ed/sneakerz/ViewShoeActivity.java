package com.p3.ed.sneakerz;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;

/**
 * Created by Ed on 6/1/15.
 */
public class ViewShoeActivity extends ActionBarActivity {

    public static final String KEY_SHOE_ID = "shoe_id";
    private Shoe mShoe;
    private Bundle mArgs;

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
                    break;

                case ACTION_DB_UPDATED:
                    loadShoeData();
                    refreshViews();
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_shoe_activity);

        Intent intent = getIntent();
        int _id = intent.getIntExtra(KEY_SHOE_ID, 1);
        DataSrc dataSrc = new DataSrc(this);
        try {
            dataSrc.open();
            mShoe = dataSrc.getShoe(_id);

            refreshViews();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (dataSrc.isOpen()) dataSrc.close();
        }

        FragmentManager fm = getFragmentManager();
        RunHistFrag runHistFrag = new RunHistFrag();
        mArgs = new Bundle();
        mArgs.putInt(ViewShoeActivity.KEY_SHOE_ID, mShoe.getId());
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
    public void onStart() {
        super.onStart();

        // Set title to name of this shoe
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mShoe.name);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();
        Bundle args = new Bundle();
        args.putInt(KEY_SHOE_ID, mShoe.getId());

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

    private void loadShoeData() {
        DataSrc dataSrc = new DataSrc(this);
        try {
            dataSrc.open();
            mShoe = dataSrc.getShoe(mShoe.getId());
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (dataSrc.isOpen()) dataSrc.close();
        }
    }

    private void refreshViews() {
        ImageView imageView = (ImageView) findViewById(R.id.view_shoe_image);
        imageView.setImageURI(mShoe.getImageUri());

        TextView distView = (TextView) findViewById(R.id.view_shoe_dist);
        TextView descView = (TextView) findViewById(R.id.view_shoe_dist_desc);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String units = prefs.getString("key_pref_dist", "miles");
        distView.setText(mShoe.getDist(units));
        descView.setText(units);
    }
}
