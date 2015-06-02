package com.p3.ed.sneakerz;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import java.sql.SQLException;

/**
 * Created by Ed on 5/29/15.
 */
public class NewRunFrag extends Fragment {
    public static final String TAG = "NewRunFrag";

    private Context mContext;
    private int mShoeId;
    private NumberPicker mTensPicker, mOnesPicker, mTenthsPicker;
    private double mRunMiles = 0;

    private final Runnable writeBack = new Runnable() {
        @Override
        public void run() {
            DataSrc dataSrc = new DataSrc(mContext);
            try {
                dataSrc.open();
                Shoe shoe = dataSrc.getShoe(mShoeId);

                double newMiles = shoe.miles + mRunMiles;
                dataSrc.updateShoe(shoe.name, newMiles, shoe.getId());
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            } finally {
                if (dataSrc.isOpen()) dataSrc.close();
            }

            Intent dbUpdated = new Intent();
            dbUpdated.setAction(ViewShoeActivity.ACTION_DB_UPDATED);
            mContext.sendBroadcast(dbUpdated);
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
                mRunMiles = 10.0 * mTensPicker.getValue() + mOnesPicker.getValue() + mTenthsPicker.getValue() / 10.0;

                Thread t = new Thread(writeBack);
                t.start();

                Intent viewHist = new Intent();
                viewHist.setAction(ViewShoeActivity.ACTION_VIEW_HIST);
                mContext.sendBroadcast(viewHist);
            }
        });
    }
}
