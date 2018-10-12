package de.wackernagel.dkq.dagger.workerinjector;

import androidx.work.Worker;
import dagger.Binds;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;
import de.wackernagel.dkq.workers.ProfileWorker;
import de.wackernagel.dkq.workers.UpdateWorker;

@Module(subcomponents = {ProfileWorkerModule.class, UpdateWorkerModule.class})
public abstract class WorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(ProfileWorker.class)
    abstract AndroidInjector.Factory<? extends Worker> bindProfileWorkerFactory(ProfileWorkerModule.Builder profileWorker);

    @Binds
    @IntoMap
    @WorkerKey(UpdateWorker.class)
    abstract AndroidInjector.Factory<? extends Worker> bindUpdateWorkerFactory(UpdateWorkerModule.Builder updateWorker);
}