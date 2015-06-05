package com.p3.ed.sneakerz;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.sql.SQLException;

/**
 * Created by Ed on 5/29/15.
 */
public class NewRunFrag extends Fragment {
    public static final String TAG = "NewRunFrag";

    private Context mContext;
    private int mShoeId;
    private NumberPicker mTensPicker, mOnesPicker, mTenthsPicker;
    private double mRunDist = 0;
    private String mUnits;

    private final Runnable writeBack = new Runnable() {
        @Override
        public void run() {
            // Don't care about zero distance runs
            if (mRunDist != 0) {
                DataSrc dataSrc = new DataSrc(mContext);
                try {
                    dataSrc.open();

                    double miles = mRunDist;
                    // If in kilometers mode, convert user input to miles
                    if (mUnits.equals("kilometers")) {
                        miles = mRunDist / 1.609344; // kilometers per mile
                    }
                    // Add a new run to the database
                    dataSrc.addRun(mShoeId, miles);

                    Shoe shoe = dataSrc.getShoe(mShoeId);
                    // Update data for this shoe
                    double newMiles = shoe.miles + miles;
                    dataSrc.updateShoe(shoe.name, newMiles, shoe.getId());
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                } finally {
                    if (dataSrc.isOpen()) dataSrc.close();
                }
            }
        }
    };

    @Override
    public void setArguments(Bundle args) {
        mShoeId = args.getInt(ViewShoeActivity.KEY_SHOE_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_run_frag, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();

        mTensPicker = (NumberPicker) getView().findViewById(R.id.new_run_tens);
        mOnesPicker = (NumberPicker) getView().findViewById(R.id.new_run_ones);
        mTenthsPicker = (NumberPicker) getView().findViewById(R.id.new_run_tenths);

        Button finButton = (Button) getView().findViewById(R.id.new_run_finish);
        finButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRunDist = 10.0 * mTensPicker.getValue() + mOnesPicker.getValue() + mTenthsPicker.getValue() / 10.0;

                Thread t = new Thread(writeBack);
                t.start();

                // Switch to run history fragment
                RunHistFrag runHistFrag = new RunHistFrag();
                Bundle args = new Bundle();
                args.putInt(ViewShoeActivity.KEY_SHOE_ID, mShoeId);
                runHistFrag.setArguments(args);

                getFragmentManager().beginTransaction().replace(R.id.view_shoe_frag_container,
                        runHistFrag).commit();
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mUnits = prefs.getString("key_pref_dist", "miles");
        TextView distDescView = (TextView) getView().findViewById(R.id.new_run_dist_desc);
        distDescView.setText(mUnits);
    }
}
