package com.p3.ed.sneakerz;

import android.app.Fragment;
import android.content.Context;
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

    public static final String CONTEXT = "context";
    private Context mContext;

    public static final String SHOE_ID = "shoe_id";
    private int mShoe_id;

    @Override
    public void setArguments(Bundle args) {
        mContext = (Context) args.get(CONTEXT);
        mShoe_id = args.getInt(SHOE_ID);
    }

    private NumberPicker tens, ones, tenths;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize all number pickers
        tens = (NumberPicker) getView().findViewById(R.id.new_run_tens);
        tens.setMaxValue(9);
        tens.setMinValue(0);
        ones = (NumberPicker) getView().findViewById(R.id.new_run_ones);
        ones.setMaxValue(9);
        ones.setMinValue(0);
        tenths = (NumberPicker) getView().findViewById(R.id.new_run_tenths);
        tenths.setMaxValue(9);
        tenths.setMinValue(0);

        // Set button click behavior
        Button fin = (Button) getView().findViewById(R.id.new_run_finish);
        fin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input from number pickers
                mRunMiles = 10 * tens.getValue() + ones.getValue() + tenths.getValue() / 10;
                // Write values to database on background thread
                Thread t = new Thread(writeBack);
                t.start();
            }
        });
    }

    private double mRunMiles = 0;

    private final Runnable writeBack = new Runnable() {
        @Override
        public void run() {
            // Get writable database
            DataSrc dataSrc = new DataSrc(mContext);
            try {
                dataSrc.open();
                // TODO: Put run data in seperate table
                Shoe shoe = dataSrc.getShoe(mShoe_id);
                double newMiles = shoe.miles + mRunMiles;
                // Write
                dataSrc.updateShoe(shoe.name, newMiles, shoe.get_id());

            } catch (SQLException sqle) {
                sqle.printStackTrace();
            } finally {
                // Avoid database leak
                if (dataSrc != null && !dataSrc.isOpen()) {
                    dataSrc.close();
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout and return
        return inflater.inflate(R.layout.new_run_frag, container, false);
    }
}