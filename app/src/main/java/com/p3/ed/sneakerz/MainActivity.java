package com.p3.ed.sneakerz;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
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

    private Cursor mShoeCursor;

    private final View.OnClickListener mNewShoeButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditText editText = (EditText) findViewById(R.id.main_shoe_name_input);
            String input = editText.getText().toString();
            if (!input.isEmpty()) {
                DataSrc dataSrc = new DataSrc(getApplicationContext());
                try {
                    dataSrc.open();
                    dataSrc.addShoe(input);

                    mShoeCursor = dataSrc.getAllShoes();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                } finally {
                    if (dataSrc.isOpen()) dataSrc.close();
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
    public void onResume() {
        super.onResume();

        loadShoeCursor();
        refreshViews();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void loadShoeCursor() {
        DataSrc dataSrc = new DataSrc(this);
        try {
            dataSrc.open();
            mShoeCursor = dataSrc.getAllShoes();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (dataSrc.isOpen()) dataSrc.close();
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
