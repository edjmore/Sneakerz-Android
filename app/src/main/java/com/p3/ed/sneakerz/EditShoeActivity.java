package com.p3.ed.sneakerz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.sql.SQLException;

/**
 * Created by Ed on 5/27/15.
 */
public class EditShoeActivity extends Activity {

    private Shoe mShoe;
    private DataSrc mData;

    // Child views
    private TextView mNameView, mMilesView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_shoe_activity);

        // Load data from intent and get shoe
        Intent intent = getIntent();
        int _id = intent.getIntExtra(DbHelper.SHOES_ID, 0);

        mData = new DataSrc(this);
        try {
            mData.open();
            mShoe = mData.getShoe(_id);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        // Get handles on child views
        mNameView = (TextView) findViewById(R.id.edit_shoe_name);
        mMilesView = (TextView) findViewById(R.id.edit_shoe_dist);

        // Populate views from shoe data
        if (mShoe != null) {
            refreshViews();
        }
    }

    private void refreshViews() {
        mNameView.setText(mShoe.name);
        mMilesView.setText(String.valueOf(mShoe.miles));
    }

    @Override
    public void onDestroy() {
        if (mData != null) mData.close();
        super.onDestroy();
    }
}
