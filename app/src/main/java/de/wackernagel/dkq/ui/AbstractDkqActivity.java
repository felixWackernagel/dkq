package de.wackernagel.dkq.ui;

import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import dagger.android.AndroidInjection;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.utils.DkqNotificationBroadcastReceiver;

public abstract class AbstractDkqActivity extends AppCompatActivity implements DkqNotificationBroadcastReceiver.DkqNotificationCallback {
    private static DkqNotificationBroadcastReceiver receiver = new DkqNotificationBroadcastReceiver();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Placeholder UI
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver.setCallback( this );
        receiver.registerTo( this );
    }

    @Override
    protected void onPause() {
        super.onPause();
        receiver.clearCallback();
        receiver.unregisterFrom( this );
    }

    @Override
    public void onMessage(@NonNull String message) {
        Snackbar.make( findViewById( R.id.container ), message, Snackbar.LENGTH_LONG ).show();
    }
}
