package de.wackernagel.dkq.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.squareup.inject.assisted.Assisted;
import com.squareup.inject.assisted.AssistedInject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.wackernagel.dkq.DkqLog;
import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.dagger.workerfactory.ChildWorkerFactory;
import de.wackernagel.dkq.receiver.NotificationReceiver;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.webservice.ApiResult;
import retrofit2.Response;

/* DON'T RENAME THIS CLASS */
/* OTHERWISE THE WORK MANAGER CAN'T FIND THE CLASS BY NAME FOR ALWAYS SCHEDULED TASKS */
public class UpdateWorker extends Worker {

    @AssistedInject.Factory
    public interface Factory extends ChildWorkerFactory {}

    private static final String TAG = "UpdateWorker";

    private final DkqRepository repository;

    @AssistedInject
    UpdateWorker(
            @Assisted @NonNull final Context appContext,
            @Assisted @NonNull final WorkerParameters workerParameters,
            final DkqRepository repository ) {
        super(appContext, workerParameters);
        this.repository = repository;
    }

    @NonNull
    @Override
    public Result doWork() {
        updateLog();

        final String logQuizzes = updateQuizzes();
        final String logMessage = updateMessages();
        updateQuestions();

        if( DkqPreferences.isDailyUpdateNotificationEnabled( getApplicationContext() ) ) {
            NotificationReceiver.forDailyUpdate( getApplicationContext(), logMessage, logQuizzes );
        }

        return Worker.Result.success();
    }

    private void updateLog() {
        final String prevLogs = DkqPreferences.getLastUpdateWorkerExecutionTime( getApplicationContext() );
        final String currentTimestamp = new SimpleDateFormat( "dd.MM.yyyy - HH:mm", Locale.getDefault() ).format( new Date() );
        String newLogs;
        if( !prevLogs.contains("|") ) {
            newLogs = prevLogs + " | " + currentTimestamp;
        } else {
            final String prevLog = prevLogs.split(" \\| ")[1];
            newLogs = prevLog  + " | " + currentTimestamp;
        }
        DkqPreferences.setLastUpdateWorkerExecutionTime( getApplicationContext(), newLogs );
    }

    private String updateQuizzes() {
        try {
            return handleApiResult( repository.requestQuizzesIfNotLimited(), repository::saveQuizzesWithNotification );
        } catch (IOException e) {
            DkqLog.e(TAG, "Quizzes Update-Request Error", e);
            return "Exception happened";
        }
    }

    private String updateMessages() {
        try {
            return handleApiResult( repository.requestMessagesIfNotLimited(), repository::saveMessagesWithNotification );
        } catch (IOException e) {
            DkqLog.e(TAG, "Messages Update-Request Error", e);
            return "Exception happened";
        }
    }

    private void updateQuestions() {
        final List<Quiz> localQuizzes = repository.queryQuizzes();
        if( localQuizzes == null || localQuizzes.isEmpty() )
            return;

        for( Quiz quiz : localQuizzes ) {
            try {
                handleApiResult( repository.requestQuestionsIfNotLimited( quiz.number ), questions -> repository.saveQuestions( questions, quiz.id ) );
            } catch (IOException e) {
                DkqLog.e(TAG, String.format( Locale.ENGLISH,"Question Update-Request Error (id=%d, number=%d)", quiz.id, quiz.number ), e);
            }
        }
    }

    private <T> String handleApiResult( @Nullable final Response<ApiResult<T>> response, @NonNull final Consumer<? super T> successConsumer ){
        if( response != null && response.isSuccessful() && response.body() != null ) {
            final ApiResult<T> api = response.body();
            if( api.isStatusOk() ) {
                if( api.result != null ) {
                    successConsumer.accept( api.result );
                    return "data processed (ok)";
                } else {
                    return "No API result (fail)";
                }
            } else {
                // No error handling required because list results return no 404.
                DkqLog.e( TAG, String.format( Locale.ENGLISH, "API Error %d: %s", api.code, api.message) );
                return "API error " + api.code + " (fail)";
            }
        }
        return response == null ? "Request count was limited (fail)" : "Response error (fail)";
    }
}
