package de.wackernagel.dkq.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.wackernagel.dkq.R;

public class PreferencesActivity extends AppCompatActivity {

    static Intent createLaunchIntent(final Context context ) {
        return new Intent( context, PreferencesActivity.class );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar( toolbar );
        final ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

}
