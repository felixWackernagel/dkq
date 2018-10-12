package de.wackernagel.dkq.dagger.workerinjector;

import androidx.work.Worker;
import dagger.android.AndroidInjector;

public interface HasWorkerInjector {
    AndroidInjector<Worker> workerInjector();
}