package de.wackernagel.dkq.workers;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.dagger.workerinjector.AndroidWorkerInjection;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Message;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.webservice.Webservice;
import retrofit2.Response;

public class UpdateWorker extends Worker {
    private static final String TAG = UpdateWorker.class.getSimpleName();

    @Inject
    DkqRepository repository;

    @Inject
    Webservice webservice;

    public UpdateWorker( @NonNull final Context appContext, @NonNull final WorkerParameters workerParams ) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AndroidWorkerInjection.inject(this);
        DkqPreferences.setLastUpdateWorkerExecutionTime( getApplicationContext(), new SimpleDateFormat( "dd. MMMM yyyy - HH:mm", Locale.getDefault() ).format( new Date() ) );

        updateQuizzes();
        updateMessages();

        return Worker.Result.SUCCESS;
    }

    private void updateQuizzes() {
        try {
            final Response<List<Quiz>> response = webservice.getQuizzesList().execute();
            if( response != null && response.isSuccessful() && response.body() != null ) {
                repository.saveQuizzesWithNotification( response.body() );
            }
        } catch (IOException e) {
            Log.e(TAG, "request quizzes error", e);
        }
    }

    private void updateMessages() {
        try {
            final Response<List<Message>> response = webservice.getMessagesList().execute();
            if( response != null && response.isSuccessful() && response.body() != null ) {
                repository.saveMessagesWithNotification( response.body() );
            }
        } catch (IOException e) {
            Log.e(TAG, "request messages error", e);
        }
    }
}
