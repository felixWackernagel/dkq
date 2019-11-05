package de.wackernagel.dkq.dagger.workerfactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import de.wackernagel.dkq.workers.UpdateWorker;

@Module
public interface WorkerBindingModule {

    @Binds
    @IntoMap
    @WorkerKey(UpdateWorker.class)
    ChildWorkerFactory bindUpdateWorker( UpdateWorker.Factory factory );

}
