package de.wackernagel.dkq.viewmodels;

import android.app.Application;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import androidx.lifecycle.AndroidViewModel;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.workers.UpdateWorker;

public class MainViewModel extends AndroidViewModel {
    private final WorkManager workManager;

    MainViewModel(final Application application) {
        super(application);
        this.workManager = WorkManager.getInstance();
    }

    public void installUpdateChecker() {
        Log.i("dkq", "Install update checker");
        workManager.enqueueUniquePeriodicWork(
                "dkqUpdateChecker",
                ExistingPeriodicWorkPolicy.REPLACE,
                new PeriodicWorkRequest.Builder(UpdateWorker.class, 1, TimeUnit.DAYS).setConstraints(
                        new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).build()
        );
    }

    public boolean isNewAppVersion() {
        int currentVersionCode;
        try {
            currentVersionCode = getApplication().getPackageManager().getPackageInfo( getApplication().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            currentVersionCode = 0;
        }
        final int lastVersionCode = DkqPreferences.getLastVersionCode( getApplication() );
        return currentVersionCode > lastVersionCode;
    }
}
