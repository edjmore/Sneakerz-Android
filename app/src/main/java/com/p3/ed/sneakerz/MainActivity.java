package com.p3.ed.sneakerz;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.sql.SQLException;

/**
 * Created by Ed on 5/27/15.
 */
public class MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";


    private Context mContext;
    private Point mDisplaySize;
    private ShoeCursorAdapter mAdapter;
    private ListView mListView;
    private PopupWindow mPopupWindow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        init();

        // List of shoes
        mListView = (ListView) findViewById(R.id.main_shoe_list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int _id = view.getId();
                // Launch a shoe detail activity using the shoe's ID
                Intent intent = new Intent(MainActivity.this, ViewShoeActivity.class);
                intent.putExtra(ViewShoeActivity.KEY_SHOE_ID, _id);
                startActivity(intent);
            }
        });
        // Get data and populate the list
        refresh();

        // Button for adding new shoes to list
        findViewById(R.id.main_add_shoe_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show a popup window to create a new shoe
                showPopup();
            }
        });
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
                // Launch settings activity
                Intent prefs = new Intent(this, PrefActivity.class);
                startActivity(prefs);
                return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Get new data and refresh the shoe list
        refresh();
    }

    private void init() {
        mContext = this;
        // Need to set background to transparent
        FrameLayout root = (FrameLayout) findViewById(R.id.main_root);
        root.getForeground().setAlpha(0);
        // Screen size
        mDisplaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(mDisplaySize);
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

    private void showPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.new_shoe_popup, null);
        // Button to finish creating a new shoe
        Button doneButton = (Button) popupView.findViewById(R.id.new_shoe_popup_finish);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input
                EditText editName = (EditText) mPopupWindow.getContentView()
                        .findViewById(R.id.new_shoe_popup_name);
                String input = editName.getText().toString();
                // Dismiss the popup
                mPopupWindow.dismiss();
                // Restore backround
                FrameLayout root = (FrameLayout) findViewById(R.id.main_root);
                root.getForeground().setAlpha(0); // 0 is transparent

                boolean success = createNewShoe(input);
                // Let user know if new shoe was added
                if (success) {
                    Toast.makeText(mContext, "\'" + input + "\' added!", Toast.LENGTH_SHORT);
                    refresh();
                } else {
                    Toast.makeText(mContext, "Shoe not added.", Toast.LENGTH_SHORT);
                }
            }
        });

        // Use screen dimensions to calculate popup window size
        mPopupWindow = new PopupWindow(popupView, (int) (mDisplaySize.x * 0.85),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // Show ar center of shoe list view
        mPopupWindow.showAtLocation(findViewById(R.id.main_shoe_list),
                Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, (int) (mDisplaySize.y * 0.25));
        // Dim the background
        FrameLayout root = (FrameLayout) findViewById(R.id.main_root);
        root.getForeground().setAlpha(208); // 255 is invisible
    }

    private boolean createNewShoe(String input) {
        if (!input.isEmpty()) {
            DataSrc dataSrc = new DataSrc(mContext);
            try {
                dataSrc.open();
                // Write new shoe to database
                dataSrc.addShoe(input);
                // Shoe successfully added
                return true;
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            } finally {
                if (dataSrc.isOpen()) dataSrc.close();
            }
        }
        // There was an error and no new shoe was created
        return false;
    }
}
