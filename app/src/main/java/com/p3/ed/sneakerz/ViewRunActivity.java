package com.p3.ed.sneakerz;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

/**
 * Created by Ed on 6/7/15.
 */
public class ViewRunActivity extends ActionBarActivity {
    private static final String TAG = "ViewRunActivity";

    public static final String KEY_RUN_ID = "key_run_id";

    private int mRunId;
    private TextView mDistView, mDistDescView, mDateView;
    private SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_run_activity);
        init();

        Run run = fetchRunData();
        if (run != null) {
            refreshTextViews(run);
        }

        ActionBar actionBar = getSupportActionBar();

    }

    private void init() {
        // Run ID
        mRunId = getIntent().getIntExtra(KEY_RUN_ID, 1);
        // View references
        mDistView = (TextView) findViewById(R.id.view_run_distance);
        mDistDescView = (TextView) findViewById(R.id.view_run_distance_description);
        mDateView = (TextView) findViewById(R.id.view_run_date);
        // Preferences
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private Run fetchRunData() {
        DataSrc dataSrc = new DataSrc(this);
        try {
            dataSrc.open();
            return dataSrc.getRun(mRunId);

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            dataSrc.close();
        }

        return null; // error
    }

    private void refreshTextViews(Run run) {
        String units = mPrefs.getString("key_pref_dist", "miles");
        mDistView.setText(run.getDist(units));
        mDistDescView.setText(units);
        mDateView.setText(run.getDate());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.view_run_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_edit:
                // TODO: switch to edit mode
                Toast.makeText(this, "Switching to edit mode...", Toast.LENGTH_SHORT).show();
                return true;
        }

        return false;
    }
}
