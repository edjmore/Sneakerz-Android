package com.p3.ed.sneakerz;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Ed on 5/29/15.
 */
public class RunHistFrag extends Fragment {

    // TODO: Load run history for this shoe
    private int mShoe_id;

    @Override
    public void setArguments(Bundle args) {
        mShoe_id = args.getInt(EditShoeActivity.SHOE_ID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set button click behavior
        Button addRun = (Button) getView().findViewById(R.id.run_hist_add_run);
        addRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Want to replace this fragment with the new run fragment
                Intent addRun = new Intent();
                addRun.addCategory(EditShoeActivity.FRAG_ACTION);
                addRun.setAction(EditShoeActivity.ADD_RUN);
                getActivity().sendBroadcast(addRun);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.run_hist_frag, container, false);
    }
}
