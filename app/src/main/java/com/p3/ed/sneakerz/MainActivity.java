package com.p3.ed.sneakerz;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.sql.SQLException;

/**
 * Created by Ed on 5/27/15.
 */
public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    private DataSrc mDataSrc;

    private ListView mShoeList;
    private Cursor mShoeCursor;
    private ShoeCursorAdapter mShoeAdapter;

    private EditText mEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Initialize database
        mDataSrc = new DataSrc(this);

        // Get handles for views
        mShoeList = (ListView) findViewById(R.id.main_shoe_list);
        mEditText = (EditText) findViewById(R.id.main_shoe_name_input);

        Button button = (Button) findViewById(R.id.main_add_shoe_button);
        // Add a new row to the database when button is clicked
        button.setOnClickListener(buttonClickListener);

        // Register reciever
        IntentFilter filter = new IntentFilter(EditShoeActivity.DB_UPDATED);
        registerReceiver(broadcastReceiver, filter);

        // Open edit shoe activity when list item is clicked
        mShoeList.setOnItemClickListener(lvClickListener);

        // Load data into list view
        refreshViews();
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshViews();
    }

    private final View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String input = mEditText.getText().toString();
            // Only add a new shoe if input is not empty
            if (!input.isEmpty()) {
                try {
                    mDataSrc.open();
                    mDataSrc.addShoe(input);
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            // Refresh list view
            refreshViews();
        }
    };

    private final AdapterView.OnItemClickListener lvClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Get the shoe's ID from view
            int _id = (int) view.getTag();

            // Start edit shoe activity and pass the ID along
            Intent intent = new Intent(MainActivity.this, EditShoeActivity.class);
            intent.putExtra(DbHelper.SHOES_ID, _id);
            startActivity(intent);
        }
    };

    private void refreshViews() {
        // Get all shoe data
        try {
            mDataSrc.open();
            mShoeCursor = mDataSrc.getAllShoes();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        // Bind to the list view (if not null or empty)
        if (mShoeCursor != null && mShoeCursor.getCount() > 0) {
            mShoeCursor.moveToNext();
            mShoeAdapter = new ShoeCursorAdapter(this, mShoeCursor, 0);
            mShoeList.setAdapter(mShoeAdapter);
        }

        // Clear text
        mEditText.setText("");
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Refresh views if data has been updated in another activity
            if (intent.getAction() == EditShoeActivity.DB_UPDATED) {
                Log.d(TAG, "UPDATED");
                refreshViews();
            }
        }
    };

    @Override
    public void onDestroy() {
        // Need to make sure nothing is leaked
        if (mDataSrc != null) mDataSrc.close();
        if (broadcastReceiver != null) unregisterReceiver(broadcastReceiver);

        super.onDestroy();
    }
}
