package com.p3.ed.sneakerz;

import android.app.Fragment;
import android.content.Context;
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
    private static final String TAG = "RunHistFrag";

    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.run_hist_frag, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();

        Button addRunButton = (Button) getView().findViewById(R.id.run_hist_add_run);
        addRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addRun = new Intent();
                addRun.setAction(ViewShoeActivity.ACTION_ADD_RUN);
                mContext.sendBroadcast(addRun);
            }
        });
    }
}
