package de.wackernagel.dkq.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import static de.wackernagel.dkq.receiver.NotificationReceiver.NOTIFICATION_CONTENT;
import static de.wackernagel.dkq.receiver.NotificationReceiver.NOTIFICATION_TITLE;
import static de.wackernagel.dkq.receiver.NotificationReceiver.NOTIFICATION_TYPE;
import static de.wackernagel.dkq.receiver.NotificationReceiver.NOTIFICATION_TYPE_NEXT_QUIZ;

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
