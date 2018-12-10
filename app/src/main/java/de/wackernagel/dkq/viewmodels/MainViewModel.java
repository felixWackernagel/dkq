package de.wackernagel.dkq.viewmodels;

import android.app.Application;

import java.util.concurrent.TimeUnit;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import de.wackernagel.dkq.AppExecutors;
import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.utils.AppUtils;
import de.wackernagel.dkq.webservice.Resource;
import de.wackernagel.dkq.workers.UpdateWorker;

public class MainViewModel extends AndroidViewModel {
    private final AppExecutors executors;
    private final DkqRepository repository;
    private final WorkManager workManager;
    private final MutableLiveData<Boolean> newAppVersion;

    MainViewModel(final Application application, final AppExecutors executors, final DkqRepository repository) {
        super(application);
        this.executors = executors;
        this.repository = repository;
        workManager = WorkManager.getInstance();
        newAppVersion = new MutableLiveData<>();
        newAppVersion.setValue( Boolean.FALSE );
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

    public LiveData<Boolean> isNewAppVersion() {
        executors.diskIO().execute( new Runnable() {
            @Override
            public void run() {
                final int actualVersion = AppUtils.getAppVersionCode( getApplication() );
                final int previousVersion = DkqPreferences.getLastVersionCode( getApplication() );
                final boolean isNewVersion = actualVersion > previousVersion;
                executors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        newAppVersion.setValue( isNewVersion );
                    }
                });
            }
        } );
        return newAppVersion;
    }

    public LiveData<Resource<Quiz>> loadNextQuiz() {
        return repository.loadNextQuiz();
    }
}
