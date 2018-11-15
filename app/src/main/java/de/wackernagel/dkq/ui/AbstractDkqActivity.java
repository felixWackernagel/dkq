package de.wackernagel.dkq.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static de.wackernagel.dkq.receiver.NotificationReceiver.NOTIFICATION_CONTENT;
import static de.wackernagel.dkq.receiver.NotificationReceiver.NOTIFICATION_TITLE;

public abstract class AbstractDkqActivity extends AppCompatActivity {

    public static BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra( NOTIFICATION_CONTENT );
            if( TextUtils.isEmpty( message ) ) {
                message = intent.getStringExtra( NOTIFICATION_TITLE );
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

            if( isOrderedBroadcast() ) {
                abortBroadcast();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Placeholder UI
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver( receiver, new IntentFilter("dkq.notification") );
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver( receiver );
    }
}
