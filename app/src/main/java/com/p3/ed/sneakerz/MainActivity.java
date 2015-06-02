package com.p3.ed.sneakerz;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
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

    private DataSrc mDataSrc;
    private Cursor mShoeCursor;

    private final View.OnClickListener mNewShoeButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditText editText = (EditText) findViewById(R.id.main_shoe_name_input);
            String input = editText.getText().toString();
            if (!input.isEmpty()) {
                if (mDataSrc == null) mDataSrc = new DataSrc(getApplicationContext());
                try {
                    if (!mDataSrc.isOpen()) mDataSrc.open();
                    mDataSrc.addShoe(input);

                    mShoeCursor = mDataSrc.getAllShoes();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            refreshViews();
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

        Button newShoeButton = (Button) findViewById(R.id.main_add_shoe_button);
        newShoeButton.setOnClickListener(mNewShoeButtonListener);

        ListView shoeList = (ListView) findViewById(R.id.main_shoe_list);
        shoeList.setOnItemClickListener(mLvClkListener);

        loadShoeCursor();
        refreshViews();
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
                startActivityForResult(prefs, 0);
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            loadShoeCursor();
            refreshViews();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        loadShoeCursor();
        refreshViews();
    }

    @Override
    public void onDestroy() {
        if (mDataSrc != null && mDataSrc.isOpen()) mDataSrc.close();
        super.onDestroy();
    }

    private void loadShoeCursor() {
        if (mDataSrc == null) mDataSrc = new DataSrc(this);
        try {
            if (!mDataSrc.isOpen()) mDataSrc.open();
            mShoeCursor = mDataSrc.getAllShoes();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    private void refreshViews() {
        if (mShoeCursor != null && mShoeCursor.getCount() > 0) {
            mShoeCursor.moveToNext();

            ListView shoeList = (ListView) findViewById(R.id.main_shoe_list);
            ShoeCursorAdapter shoeCursorAdapter = new ShoeCursorAdapter(this, mShoeCursor, 0);
            shoeList.setAdapter(shoeCursorAdapter);
        }

        EditText editText = (EditText) findViewById(R.id.main_shoe_name_input);
        editText.clearComposingText();
    }
}
