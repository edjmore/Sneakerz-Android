package com.p3.ed.sneakerz;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.sql.SQLException;

/**
 * Created by Ed on 5/27/15.
 */
public class MainActivity extends Activity {

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

        mShoeList = (ListView) findViewById(R.id.main_shoe_list);
        mEditText = (EditText) findViewById(R.id.main_shoe_name_input);

        Button button = (Button) findViewById(R.id.main_add_shoe_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mEditText.getText().toString();
                if (!input.isEmpty()) {
                    try {
                        mDataSrc.open();
                        mDataSrc.addShoe(input);
                    } catch (SQLException sqle) {
                        sqle.printStackTrace();
                    }
                }

                // Refresh list view
                refresh();
            }
        });

        refresh();
    }

    private void refresh() {
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
    }

    @Override
    public void onDestroy() {
        // Need to make sure the database isn't leaked
        if (mDataSrc != null) mDataSrc.close();
        super.onDestroy();
    }
}
