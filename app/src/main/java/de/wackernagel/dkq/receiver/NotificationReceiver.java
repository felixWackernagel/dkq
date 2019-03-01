package de.wackernagel.dkq.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.ui.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_TITLE = "notification:title";
    public static final String NOTIFICATION_CONTENT = "notification:content";
    public static final String NOTIFICATION_TYPE = "notification:type";

    public static final int NOTIFICATION_TYPE_NEXT_QUIZ = 1;
    public static final int NOTIFICATION_TYPE_NEW_MESSAGES = 2;

    private static final String CHANNEL_ID = "dkq";

    public static void forNextQuiz( final Context context, final int futureQuizzesCount ) {
        final Intent notification = new Intent( "dkq.notification" );
        notification.putExtra( NOTIFICATION_TITLE, context.getString(R.string.next_quiz_title) );
        notification.putExtra( NOTIFICATION_CONTENT, context.getResources().getQuantityString(R.plurals.next_quiz_description, futureQuizzesCount, futureQuizzesCount) );
        notification.putExtra( NOTIFICATION_TYPE, NOTIFICATION_TYPE_NEXT_QUIZ );
        context.sendOrderedBroadcast( notification, null ); // null means no permissions required by the receiver
    }

    public static void forNewMessages( final Context context, final int newMessagesCount ) {
        final Intent notification = new Intent( "dkq.notification" );
        notification.putExtra( NOTIFICATION_TITLE, context.getString(R.string.new_messages_title) );
        notification.putExtra( NOTIFICATION_CONTENT, context.getResources().getQuantityString(R.plurals.new_messages_description, newMessagesCount, newMessagesCount) );
        notification.putExtra( NOTIFICATION_TYPE, NOTIFICATION_TYPE_NEW_MESSAGES );
        context.sendOrderedBroadcast( notification, null ); // null means no permissions required by the receiver
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if( DkqPreferences.notificationsEnabled( context ) ) {

            // Make a channel if necessary
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                final String name = "DKQ Benachrichtigungen";
                final String description = "Zeigt Benachrichtigungen vom DKQ.";
                final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription( description );

                // Add the channel
                final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }

            final String title = intent.getStringExtra( NOTIFICATION_TITLE );
            final String content =  intent.getStringExtra(NOTIFICATION_CONTENT);
            final int notificationId = intent.getIntExtra(NOTIFICATION_TYPE, 0);
            final Intent activityOnClick =new Intent(context, getActivityForNotification( notificationId ) );
            activityOnClick.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder( context, CHANNEL_ID )
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSmallIcon(R.drawable.ic_notification_dkq)
                    .setContentIntent(PendingIntent.getActivity(context, 0, activityOnClick, PendingIntent.FLAG_ONE_SHOT))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);

            if( DkqPreferences.soundsEnabled( context ) ) {
                builder.setSound( RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION ) );
            } else {
                builder.setSound( null );
            }

            NotificationManagerCompat.from(context).notify( notificationId, builder.build() );
        }
    }

    private Class<?> getActivityForNotification(int notificationId) {
        switch ( notificationId ) {
            case NOTIFICATION_TYPE_NEXT_QUIZ:
            case NOTIFICATION_TYPE_NEW_MESSAGES:
                return MainActivity.class;

            default:
                return MainActivity.class;
        }
    }
}
