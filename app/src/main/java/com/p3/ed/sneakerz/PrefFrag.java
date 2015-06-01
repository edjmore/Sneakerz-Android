package com.p3.ed.sneakerz;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Ed on 6/1/15.
 */
public class PrefFrag extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_screen);
    }
}
