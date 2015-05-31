package com.p3.ed.sneakerz;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.sql.SQLException;
import java.text.DecimalFormat;

/**
 * Created by Ed on 5/27/15.
 */
public class EditShoeActivity extends Activity {
    public static final String TAG = "EditShoeActivity";

    private Shoe mShoe;

    public static final String SHOE_ID = "shoe_id";
    public static final String IMG_URI = "img_uri";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_shoe_activity);

        // Load data from intent and get shoe
        Intent intent = getIntent();
        int _id = intent.getIntExtra(DbHelper.SHOES_ID, 0);

        DataSrc dataSrc = new DataSrc(this);
        try {
            dataSrc.open();
            mShoe = dataSrc.getShoe(_id);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            // Avoid leak
            if (dataSrc != null && dataSrc.isOpen()) dataSrc.close();
        }

        // The most confusing way to initialize a variable
        (mArgs = new Bundle()).putInt(SHOE_ID, mShoe.get_id());
        Uri lrgUri = mShoe.getImageUris().second;
        String uriString = lrgUri == null ? null : lrgUri.toString();
        mArgs.putString(IMG_URI, uriString);
        FragmentManager fm = getFragmentManager();

        // Load shoe image fragment
        ImageFrag imgFrag = new ImageFrag();
        imgFrag.setArguments(mArgs);
        fm.beginTransaction().add(R.id.edit_shoe_img_frame, imgFrag).commit();
        // TODO: Allow user to select edit option
        imgFrag.setEditable(true);

        // Get handles on child views
        mNameView = (TextView) findViewById(R.id.edit_shoe_name);
        mMilesView = (TextView) findViewById(R.id.edit_shoe_dist);

        // Reciever for fragment manipulations
        IntentFilter filter = new IntentFilter();
        filter.addAction(ADD_RUN);
        filter.addAction(VIEW_HIST);
        registerReceiver(br, filter);

        // Populate views from shoe data
        refreshViews();

        // Load run history fragment for this shoe
        RunHistFrag runHistFrag = new RunHistFrag();
        runHistFrag.setArguments(mArgs);
        fm.beginTransaction().add(R.id.edit_shoe_frame, runHistFrag).commit();
    }

    // Extra values passed to fragments
    Bundle mArgs;

    public static final String ADD_RUN = "com.p3.ed.sneakerz.add_run";
    public static final String VIEW_HIST = "com.p3.ed.sneakerz.view_hist";

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FragmentManager fm = getFragmentManager();

            switch (intent.getAction()) {
                case ADD_RUN:
                    NewRunFrag newRunFrag = new NewRunFrag();
                    newRunFrag.setArguments(mArgs);

                    fm.beginTransaction().replace(R.id.edit_shoe_frame, newRunFrag).commit();
                    break;

                case VIEW_HIST:
                    // There may be updated data available
                    refreshShoeData();
                    refreshViews();

                    RunHistFrag runHistFrag = new RunHistFrag();
                    runHistFrag.setArguments(mArgs);

                    fm.beginTransaction().replace(R.id.edit_shoe_frame, runHistFrag).commit();
                    break;
            }
        }
    };

    private void refreshShoeData() {
        DataSrc dataSrc = new DataSrc(this);
        try {
            dataSrc.open();
            mShoe = dataSrc.getShoe(mShoe.get_id());
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            // Avoid leak
            if (dataSrc != null && dataSrc.isOpen()) dataSrc.close();
        }
    }

    private TextView mNameView, mMilesView;

    private void refreshViews() {
        mNameView.setText(mShoe.name);
        // Format distance to one decimal place
        DecimalFormat df = new DecimalFormat("0.0");
        mMilesView.setText(df.format(mShoe.miles));
    }

    public void deleteShoe(View view) {
        DataSrc dataSrc = new DataSrc(this);
        try {
            dataSrc.open();
            dataSrc.deleteShoe(mShoe.get_id());

            // Go back to main activity
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        // Don't want to leak receiver
        if (br != null) unregisterReceiver(br);

        super.onDestroy();
    }
}
