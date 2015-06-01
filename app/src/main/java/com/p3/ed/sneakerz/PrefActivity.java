package com.p3.ed.sneakerz;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * Created by Ed on 6/1/15.
 */
public class PrefActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_activity);

        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(R.id.pref_container, new PrefFrag()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pref_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
