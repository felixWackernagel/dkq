package de.wackernagel.dkq.workers;

import android.content.Context;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import de.wackernagel.dkq.dagger.workerinjector.AndroidWorkerInjection;
import de.wackernagel.dkq.repository.DkqRepository;

/**
 * Placeholder worker for future task in background. Just to simplify dagger usage with two workers.
 */
public class ProfileWorker extends Worker {

    @Inject
    DkqRepository repository;

    public ProfileWorker(Context appContext, WorkerParameters parameters ) {
        super(appContext, parameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        AndroidWorkerInjection.inject(this);
        return Result.success();
    }
}
