package com.p3.ed.sneakerz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.sql.SQLException;

/**
 * Created by Ed on 5/27/15.
 */
public class MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";


    private Context mContext;
    private ShoeCursorAdapter mAdapter;
    private ListView mListView;

    private final View.OnClickListener mNewShoeButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditText editText = (EditText) findViewById(R.id.main_shoe_name_input);
            String input = editText.getText().toString();
            if (!input.isEmpty()) {
                DataSrc dataSrc = new DataSrc(mContext);
                try {
                    dataSrc.open();
                    // Add new shoe
                    dataSrc.addShoe(input);
                    // Refresh views while data source is open
                    Cursor cursor = dataSrc.getAllShoes();
                    // Adapter will not be null
                    mAdapter.swapCursor(cursor);
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                } finally {
                    if (dataSrc.isOpen()) dataSrc.close();
                }
            }
        }
    };

    private final AdapterView.OnItemClickListener mLvClkListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int _id = view.getId();

            Intent intent = new Intent(MainActivity.this, ViewShoeActivity.class);
            intent.putExtra(ViewShoeActivity.KEY_SHOE_ID, _id);
            startActivity(intent);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mContext = this;

        // Handle list view first
        mListView = (ListView) findViewById(R.id.main_shoe_list);
        refresh();
        mListView.setOnItemClickListener(mLvClkListener);

        // Button for adding new shoes to list
        Button newShoeButton = (Button) findViewById(R.id.main_add_shoe_button);
        newShoeButton.setOnClickListener(mNewShoeButtonListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent prefs = new Intent(this, PrefActivity.class);
                startActivity(prefs);
                return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    private void refresh() {
        DataSrc dataSrc = new DataSrc(mContext);
        try {
            dataSrc.open();
            // Get new shoe data and display in list
            Cursor cursor = dataSrc.getAllShoes();
            if (mAdapter == null) {
                // Initialize adapter if necessary
                mAdapter = new ShoeCursorAdapter(mContext, cursor, 0);
                mListView.setAdapter(mAdapter);
            }
            mAdapter.swapCursor(cursor);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (dataSrc.isOpen()) dataSrc.close();
        }
    }
}
