package com.p3.ed.sneakerz;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;

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
            showPopup();
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

    private PopupWindow mPopupWindow;

    private void showPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.new_shoe_popup, null);
        Button done = (Button) view.findViewById(R.id.new_shoe_popup_finish);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parent = (View) v.getParent();
                EditText editText = (EditText) parent.findViewById(R.id.new_shoe_popup_name);
                String input = editText.getText().toString();
                mPopupWindow.dismiss();
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
        });
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int width = (int) (size.x * 0.8);
        int height = (int) (size.y * 0.4);
        boolean focusable = true;
        mPopupWindow = new PopupWindow(view, width, height, focusable);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // TODO: Do something?
            }
        });
        mPopupWindow.showAtLocation(findViewById(R.id.main_shoe_list), Gravity.CENTER, 0, 0);
    }
}
