package com.p3.ed.sneakerz;

import android.app.Fragment;
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

    private int mShoe_id;

    @Override
    public void setArguments(Bundle args) {
        // mShoe_id = args.getInt(EditShoeActivity.SHOE_ID);
    }

    private NumberPicker tens, ones, tenths;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize all number pickers
        tens = (NumberPicker) getView().findViewById(R.id.new_run_tens);
        ones = (NumberPicker) getView().findViewById(R.id.new_run_ones);
        tenths = (NumberPicker) getView().findViewById(R.id.new_run_tenths);

        // Set button click behavior
        Button fin = (Button) getView().findViewById(R.id.new_run_finish);
        fin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input from number pickers
                mRunMiles = 10.0 * tens.getValue() + ones.getValue() + tenths.getValue() / 10.0;
                // Write values to database on background thread
                Thread t = new Thread(writeBack);
                t.start();

                // Replace this fragment with run history fragment
                Intent viewHist = new Intent();
                // viewHist.setAction(EditShoeActivity.VIEW_HIST);
                getActivity().sendBroadcast(viewHist);
            }
        });
    }

    private double mRunMiles = 0;

    private final Runnable writeBack = new Runnable() {
        @Override
        public void run() {
            // Activity is an extension of context
            DataSrc dataSrc = new DataSrc(getActivity());
            try {
                dataSrc.open();
                // TODO: Put run data in seperate table
                Shoe shoe = dataSrc.getShoe(mShoe_id);
                double newMiles = shoe.miles + mRunMiles;
                // Write updated distance
                dataSrc.updateShoe(shoe.name, newMiles, shoe.getId());

            } catch (SQLException sqle) {
                sqle.printStackTrace();
            } finally {
                // Avoid database leak
                if (dataSrc != null && dataSrc.isOpen()) {
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
