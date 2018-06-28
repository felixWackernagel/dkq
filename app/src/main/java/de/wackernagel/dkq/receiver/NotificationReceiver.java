package de.wackernagel.dkq.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.ui.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_TITLE = "notification:title";
    public static final String NOTIFICATION_CONTENT = "notification:content";
    public static final String NOTIFICATION_TYPE = "notification:type";

    public static final int NOTIFICATION_TYPE_NEXT_QUIZ = 1;

    public static void forNextQuiz( final Context context, final String title, final String content ) {
        final Intent notification = new Intent( "dkq.notification" );
        notification.putExtra( NOTIFICATION_TITLE, title );
        notification.putExtra( NOTIFICATION_CONTENT, content );
        notification.putExtra( NOTIFICATION_TYPE, NOTIFICATION_TYPE_NEXT_QUIZ );
        context.sendOrderedBroadcast( notification, null ); // null means no permissions required by the receiver
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if( DkqPreferences.notificationsEnabled( context ) ) {
            final String title = intent.getStringExtra( NOTIFICATION_TITLE );
            final String content =  intent.getStringExtra(NOTIFICATION_CONTENT);
            final int notificationId = intent.getIntExtra(NOTIFICATION_TYPE, 0);
            final Intent activityOnClick =new Intent(context, getActivityForNotification( notificationId ) );
            activityOnClick.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder( context, "dkq" )
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(PendingIntent.getActivity(context, 0, activityOnClick, PendingIntent.FLAG_ONE_SHOT))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);

            if( DkqPreferences.soundsEnabled( context ) ) {
                builder.setSound( Uri.parse( "android.resource://de.wackernagel.dkq/" + R.raw.stapling_paper ) );
            } else {
                builder.setSound( null );
            }

            final NotificationManager notificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );
            if( notificationManager != null ) {
                notificationManager.notify( notificationId, builder.build() );
            }
        }
    }

    private Class<?> getActivityForNotification(int notificationId) {
        switch ( notificationId ) {
            case NOTIFICATION_TYPE_NEXT_QUIZ:
                return MainActivity.class;

            default:
                return MainActivity.class;
        }
    }
}
