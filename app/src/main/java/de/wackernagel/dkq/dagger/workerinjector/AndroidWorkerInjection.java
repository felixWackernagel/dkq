package de.wackernagel.dkq.dagger.workerinjector;

import androidx.work.Worker;
import dagger.android.AndroidInjector;

public class AndroidWorkerInjection {

    public static void inject(Worker worker) {
        if( worker == null ) {
            throw new NullPointerException("worker");
        }
        Object application = worker.getApplicationContext();
        if (!(application instanceof HasWorkerInjector)) {
            throw new RuntimeException(
                    String.format(
                            "%s does not implement %s",
                            application.getClass().getCanonicalName(),
                            HasWorkerInjector.class.getCanonicalName()));
        }

        AndroidInjector<Worker> workerInjector =
                ((HasWorkerInjector) application).workerInjector();
        if( workerInjector == null ) {
            throw new NullPointerException(application.getClass().toString() + ".workerInjector() returned null" );
        }
        workerInjector.inject(worker);
    }
}