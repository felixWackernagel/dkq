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

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.ui.MainActivity;
import de.wackernagel.dkq.ui.MessageDetailsActivity;
import de.wackernagel.dkq.ui.QuizDetailsActivity;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String ACTION_DKQ_NOTIFICATION = "dkq.notification";

    public static final String NOTIFICATION_TITLE = "notification:title";
    public static final String NOTIFICATION_CONTENT = "notification:content";
    public static final String NOTIFICATION_TYPE = "notification:type";
    public static final String NOTIFICATION_ID = "notification:id";
    public static final String NOTIFICATION_NUMBER = "notification:number";

    private static final int NOTIFICATION_TYPE_ONE_FUTURE_QUIZ = 1;
    private static final int NOTIFICATION_TYPE_MANY_FUTURE_QUIZZES = 2;
    private static final int NOTIFICATION_TYPE_ONE_NEW_MESSAGE = 3;
    private static final int NOTIFICATION_TYPE_MANY_NEW_MESSAGES = 4;
    private static final int NOTIFICATION_TYPE_DAILY_UPDATE = 5;

    private static final String CHANNEL_ID = "dkq";

    public static void forOneFutureQuiz( @NonNull final Context context, final long quizId, final int quizNumber ) {
        final Intent notification = new Intent( ACTION_DKQ_NOTIFICATION );
        notification.putExtra( NOTIFICATION_TITLE, context.getString(R.string.next_quiz_title) );
        notification.putExtra( NOTIFICATION_CONTENT, context.getResources().getString(R.string.next_quiz_concrete_description, quizNumber ) );
        notification.putExtra( NOTIFICATION_TYPE, NOTIFICATION_TYPE_ONE_FUTURE_QUIZ );
        notification.putExtra( NOTIFICATION_ID, quizId );
        notification.putExtra( NOTIFICATION_NUMBER, quizNumber );
        context.sendOrderedBroadcast( notification, null ); // null means no permissions required by the receiver
    }

    public static void forManyFutureQuizzes( @NonNull final Context context, final int futureQuizzesCount ) {
        final Intent notification = new Intent( ACTION_DKQ_NOTIFICATION );
        notification.putExtra( NOTIFICATION_TITLE, context.getString(R.string.next_quiz_title) );
        notification.putExtra( NOTIFICATION_CONTENT, context.getResources().getQuantityString(R.plurals.next_quiz_description, futureQuizzesCount, futureQuizzesCount) );
        notification.putExtra( NOTIFICATION_TYPE, NOTIFICATION_TYPE_MANY_FUTURE_QUIZZES );
        context.sendOrderedBroadcast( notification, null ); // null means no permissions required by the receiver
    }

    public static void forOneNewMessage( @NonNull final Context context, @NonNull final String message, final long messageId, final int messageNumber ) {
        final Intent notification = new Intent( ACTION_DKQ_NOTIFICATION );
        notification.putExtra( NOTIFICATION_TITLE, context.getString(R.string.new_messages_title) );
        notification.putExtra( NOTIFICATION_CONTENT, message );
        notification.putExtra( NOTIFICATION_TYPE, NOTIFICATION_TYPE_ONE_NEW_MESSAGE);
        notification.putExtra( NOTIFICATION_ID, messageId );
        notification.putExtra( NOTIFICATION_NUMBER, messageNumber );
        context.sendOrderedBroadcast( notification, null ); // null means no permissions required by the receiver
    }

    public static void forManyNewMessages( @NonNull final Context context, final int newMessagesCount ) {
        final Intent notification = new Intent( ACTION_DKQ_NOTIFICATION );
        notification.putExtra( NOTIFICATION_TITLE, context.getString(R.string.new_messages_title) );
        notification.putExtra( NOTIFICATION_CONTENT, context.getResources().getQuantityString(R.plurals.new_messages_description, newMessagesCount, newMessagesCount) );
        notification.putExtra( NOTIFICATION_TYPE, NOTIFICATION_TYPE_MANY_NEW_MESSAGES);
        context.sendOrderedBroadcast( notification, null ); // null means no permissions required by the receiver
    }

    public static void forDailyUpdate( @NonNull final Context context, final String messages, final String quizzes ) {
        final Intent notification = new Intent( ACTION_DKQ_NOTIFICATION );
        notification.putExtra( NOTIFICATION_TITLE, context.getString( R.string.daily_update_title ) );
        notification.putExtra( NOTIFICATION_CONTENT, context.getString( R.string.daily_update_description, messages, quizzes ) );
        notification.putExtra( NOTIFICATION_TYPE, NOTIFICATION_TYPE_DAILY_UPDATE );
        context.sendOrderedBroadcast( notification, null ); // null means no permissions required by the receiver
    }

    @Override
    public void onReceive( final Context context, final Intent intent ) {
        if( DkqPreferences.notificationsEnabled( context ) ) {
            // Make a channel if necessary
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                final String name = context.getString( R.string.notification_channel_name );
                final String description = context.getString( R.string.notification_channel_description );
                final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription( description );

                // Add the channel
                final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }

            final String title = intent.getStringExtra( NOTIFICATION_TITLE );
            final String content =  intent.getStringExtra( NOTIFICATION_CONTENT );
            final int notificationType = intent.getIntExtra( NOTIFICATION_TYPE, 0 );
            final long itemId = intent.getLongExtra( NOTIFICATION_ID, 0 );
            final int itemNumber = intent.getIntExtra( NOTIFICATION_NUMBER, 0 );
            final Intent activityOnClick = createRedirectIntent( context, notificationType, itemId, itemNumber );
            activityOnClick.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );

            final NotificationCompat.Builder builder = new NotificationCompat.Builder( context, CHANNEL_ID )
                    .setContentTitle( title )
                    .setContentText( content )
                    .setAutoCancel(true )
                    .setPriority( NotificationCompat.PRIORITY_DEFAULT )
                    .setSmallIcon( R.drawable.ic_notification_dkq )
                    .setContentIntent( PendingIntent.getActivity( context, 0, activityOnClick, PendingIntent.FLAG_ONE_SHOT ) )
                    .setStyle( new NotificationCompat.BigTextStyle().bigText( content ) )
                    .setDefaults( Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS );

            if( DkqPreferences.soundsEnabled( context ) ) {
                builder.setSound( RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION ) );
            } else {
                builder.setSound( null );
            }

            NotificationManagerCompat.from( context ).notify( notificationType, builder.build() );
        }
    }

    private Intent createRedirectIntent(Context context, int notificationType, long itemId, int itemNumber ) {
        switch ( notificationType ) {
            case NOTIFICATION_TYPE_ONE_FUTURE_QUIZ:
                return QuizDetailsActivity.createLaunchIntent( context, itemId, itemNumber );

            case NOTIFICATION_TYPE_ONE_NEW_MESSAGE:
                return MessageDetailsActivity.createLaunchIntent( context, itemId, itemNumber );

            case NOTIFICATION_TYPE_MANY_NEW_MESSAGES:
                return MainActivity.createLaunchIntent( context, MainActivity.FRAGMENT_MESSAGES );

            case NOTIFICATION_TYPE_MANY_FUTURE_QUIZZES:
            case NOTIFICATION_TYPE_DAILY_UPDATE:
            default:
                return MainActivity.createLaunchIntent( context );
        }
    }
}
