package de.wackernagel.dkq.workers;

import android.content.Context;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import de.wackernagel.dkq.DkqLog;
import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.dagger.workerinjector.AndroidWorkerInjection;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.webservice.ApiResult;
import retrofit2.Response;

public class UpdateWorker extends Worker {
    private static final String TAG = "UpdateWorker";

    @Inject
    DkqRepository repository;

    public UpdateWorker( @NonNull final Context appContext, @NonNull final WorkerParameters workerParams ) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AndroidWorkerInjection.inject(this);
        updateLog();

        updateQuizzes();
        updateMessages();
        updateQuestions();

        return Worker.Result.success();
    }

    private void updateLog() {
        final String prevLogs = DkqPreferences.getLastUpdateWorkerExecutionTime( getApplicationContext() );
        final String currentTimestamp = new SimpleDateFormat( "dd.MM.yyyy - HH:mm", Locale.getDefault() ).format( new Date() );
        String newLogs;
        if( !prevLogs.contains("|") ) {
            newLogs = prevLogs + " | " + currentTimestamp;
        } else {
            final String prevLog = prevLogs.split("\\|")[1];
            newLogs = prevLog  + " | " + currentTimestamp;
        }
        DkqPreferences.setLastUpdateWorkerExecutionTime( getApplicationContext(), newLogs );
    }

    private void updateQuizzes() {
        try {
            handleApiResult( repository.requestQuizzesIfNotLimited(), repository::saveQuizzesWithNotification );
        } catch (IOException e) {
            DkqLog.e(TAG, "Quizzes Update-Request Error", e);
        }
    }

    private void updateMessages() {
        try {
            handleApiResult( repository.requestMessagesIfNotLimited(), repository::saveMessagesWithNotification );
        } catch (IOException e) {
            DkqLog.e(TAG, "Messages Update-Request Error", e);
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

    private <T> void handleApiResult( @Nullable final Response<ApiResult<T>> response, Consumer<? super T> successConsumer ){
        if( response != null && response.isSuccessful() && response.body() != null ) {
            final ApiResult<T> api = response.body();
            if( api.isOkStatus() ) {
                if( api.result != null ) {
                    successConsumer.accept( api.result );
                }
            } else {
                // No error handling required because list results return no 404.
                DkqLog.e( TAG, String.format( Locale.ENGLISH, "API Error %d: %s", api.code, api.message) );
            }
        }
    }
}
