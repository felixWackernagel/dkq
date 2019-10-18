package de.wackernagel.dkq.viewmodels;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.workers.UpdateWorker;

public class MainViewModel extends ViewModel {
    @NonNull
    private final DkqRepository repository;
    private final WorkManager workManager;

    MainViewModel( @NonNull final DkqRepository repository ) {
        this.repository = repository;
        workManager = WorkManager.getInstance();
    }

    public void installUpdateChecker( @NonNull final Context context ) {
        if( !DkqPreferences.isDailyUpdateScheduled( context ) ) {
            workManager.enqueueUniquePeriodicWork(
                    "dkqUpdateChecker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    new PeriodicWorkRequest.Builder(UpdateWorker.class, 1, TimeUnit.DAYS).setConstraints(
                            new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                    ).build()
            );
            DkqPreferences.setDailyUpdateScheduled( context );
        }
    }

    public LiveData<Quiz> loadNextQuiz() {
        return repository.loadNextQuiz();
    }

    public LiveData<Integer> getNewMessagesCount() {
        return repository.getNewMessagesCount();
    }
}
