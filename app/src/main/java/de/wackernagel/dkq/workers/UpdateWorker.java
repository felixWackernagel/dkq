package de.wackernagel.dkq.workers;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import de.wackernagel.dkq.dagger.workerinjector.AndroidWorkerInjection;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Message;
import de.wackernagel.dkq.room.entities.Quiz;
import retrofit2.Response;

public class UpdateWorker extends Worker {
    private static final String TAG = UpdateWorker.class.getSimpleName();

    @Inject
    DkqRepository repository;

    public UpdateWorker( @NonNull final Context appContext, @NonNull final WorkerParameters workerParams ) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AndroidWorkerInjection.inject(this);
        if( repository != null ) {
            updateQuizzes();
            updateMessages();
        }
        return Worker.Result.SUCCESS;
    }

    private void updateQuizzes() {
        try {
            final Response<List<Quiz>> response = repository.getWebService().getQuizzesList().execute();
            if( response != null && response.isSuccessful() && response.body() != null ) {
                repository.saveQuizzesWithNotification( response.body() );
            }
        } catch (IOException e) {
            Log.e(TAG, "request quizzes error", e);
        }
    }

    private void updateMessages() {
        try {
            final Response<List<Message>> response = repository.getWebService().getMessagesList().execute();
            if( response != null && response.isSuccessful() && response.body() != null ) {
                repository.saveMessagesWithNotification( response.body() );
            }
        } catch (IOException e) {
            Log.e(TAG, "request messages error", e);
        }
    }
}
