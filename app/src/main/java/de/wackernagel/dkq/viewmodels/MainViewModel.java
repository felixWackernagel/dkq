package de.wackernagel.dkq.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.workers.UpdateWorker;

public class MainViewModel extends AndroidViewModel {
    @NonNull
    private final DkqRepository repository;
    private final WorkManager workManager;

    @Inject
    MainViewModel(final @NonNull Application application, @NonNull final DkqRepository repository) {
        super( application );
        this.repository = repository;
        workManager = WorkManager.getInstance( application );
    }

    public void installUpdateChecker() {
        if( !DkqPreferences.isDailyUpdateScheduled( getApplication() ) ) {
            workManager.enqueueUniquePeriodicWork(
                    "dkqUpdateChecker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    new PeriodicWorkRequest.Builder(UpdateWorker.class, 1, TimeUnit.DAYS).setConstraints(
                            new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                    ).build()
            );
            DkqPreferences.setDailyUpdateScheduled( getApplication() );
        }
    }

    public LiveData<Quiz> loadNextQuiz() {
        return repository.loadNextQuiz();
    }

    public LiveData<Integer> getNewMessagesCount() {
        return repository.getNewMessagesCount();
    }
}
