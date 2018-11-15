package de.wackernagel.dkq.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import de.wackernagel.dkq.DkqPreferences;

public class SplashActivity extends AbstractDkqActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent redirectTo;
        if( DkqPreferences.showLoginScreen( this ) )
        {
            redirectTo = LoginActivity.createLaunchIntent( this );
        }
        else
        {
            redirectTo = MainActivity.createLaunchIntent( this );
        }
        startActivity( redirectTo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) );
        finish();
    }
}
