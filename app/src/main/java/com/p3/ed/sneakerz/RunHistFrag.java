package com.p3.ed.sneakerz;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // I don't like this, it's kinda hacky
        // Could replace with an interface, but that's more work...
        if (activity instanceof ViewShoeActivity) {
            ViewShoeActivity vsa = (ViewShoeActivity) activity;
            // Refresh data and views
            vsa.refresh();
        }
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

        runList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int runId = view.getId();
                Intent viewRun = new Intent(mContext, ViewRunActivity.class);
                viewRun.putExtra(ViewRunActivity.KEY_RUN_ID, runId);

                startActivity(viewRun);
            }
        });

        Button addRunButton = (Button) getView().findViewById(R.id.run_hist_add_run);
        addRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewRunFrag newRunFrag = new NewRunFrag();
                Bundle args = new Bundle();
                args.putInt(ViewShoeActivity.KEY_SHOE_ID, mShoeId);
                newRunFrag.setArguments(args);

                getFragmentManager().beginTransaction().replace(R.id.view_shoe_frag_container,
                        newRunFrag).commit();
            }
        });
    }
}
