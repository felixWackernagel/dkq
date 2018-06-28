package de.wackernagel.dkq.ui;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import de.wackernagel.dkq.R;

public class PreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource( R.xml.preferences );
    }

}