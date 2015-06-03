package com.p3.ed.sneakerz;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import java.sql.SQLException;

/**
 * Created by Ed on 5/29/15.
 */
public class RunHistFrag extends Fragment {
    private static final String TAG = "RunHistFrag";

    private Context mContext;
    private int mShoeId;
    private RunCursorAdapter mAdapter;

    @Override
    public void setArguments(Bundle args) {
        mShoeId = args.getInt(ViewShoeActivity.KEY_SHOE_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.run_hist_frag, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();

        ListView runList = (ListView) getView().findViewById(R.id.run_hist_list);
        // Get all run data for this shoe
        DataSrc dataSrc = new DataSrc(mContext);
        try {
            dataSrc.open();
            Cursor cursor = dataSrc.getAllRunsForShoe(mShoeId);
            cursor.moveToNext();

            mAdapter = new RunCursorAdapter(mContext, cursor, 0);
            runList.setAdapter(mAdapter);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (dataSrc.isOpen()) dataSrc.close();
        }

        ImageButton addRunButton = (ImageButton) getView().findViewById(R.id.run_hist_add_run);
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
