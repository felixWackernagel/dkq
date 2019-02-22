package de.wackernagel.dkq.viewmodels;

import android.app.Application;

import java.util.concurrent.TimeUnit;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.workers.UpdateWorker;

public class MainViewModel extends AndroidViewModel {
    private final DkqRepository repository;
    private final WorkManager workManager;

    MainViewModel(final Application application, final DkqRepository repository) {
        super(application);
        this.repository = repository;
        workManager = WorkManager.getInstance();
    }

    public void installUpdateChecker() {
        workManager.enqueueUniquePeriodicWork(
                "dkqUpdateChecker",
                ExistingPeriodicWorkPolicy.KEEP,
                new PeriodicWorkRequest.Builder(UpdateWorker.class, 1, TimeUnit.DAYS).setConstraints(
                        new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).build()
        );
    }

    public LiveData<Quiz> loadNextQuiz() {
        return repository.loadNextQuiz();
    }

    public LiveData<Integer> getNewMessagesCount() {
        return repository.getNewMessagesCount();
    }
}
