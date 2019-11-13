package de.wackernagel.dkq.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import static de.wackernagel.dkq.receiver.NotificationReceiver.NOTIFICATION_CONTENT;

public class DkqNotificationBroadcastReceiver extends BroadcastReceiver {

    public interface DkqNotificationCallback {
        void onMessage( @NonNull String message );
    }

    private final IntentFilter intentFilter;

    private DkqNotificationCallback callback;

    public DkqNotificationBroadcastReceiver() {
        this.intentFilter = new IntentFilter("dkq.notification");
    }

    public void registerTo( final Context context ) {
        context.registerReceiver( this, intentFilter );
    }

    public void unregisterFrom( final Context context ) {
        context.unregisterReceiver( this );
    }

    @Override
    public void onReceive( Context context, Intent intent ) {
        String message = intent.getStringExtra( NOTIFICATION_CONTENT );
        if( TextUtils.isEmpty( message ) ) {
            message = intent.getStringExtra( NOTIFICATION_CONTENT );
        }

        if( !TextUtils.isEmpty( message ) && callback != null ) {
            callback.onMessage( message );
        }

        if( isOrderedBroadcast() ) {
            abortBroadcast();
        }
    }

    public void setCallback( @NonNull final DkqNotificationCallback callback ) {
        this.callback = callback;
    }

    public void clearCallback() {
        this.callback = null;
    }
}