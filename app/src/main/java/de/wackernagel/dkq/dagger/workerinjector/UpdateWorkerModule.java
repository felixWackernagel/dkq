package de.wackernagel.dkq.dagger.workerinjector;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;
import de.wackernagel.dkq.workers.UpdateWorker;

@Subcomponent
public interface UpdateWorkerModule extends AndroidInjector<UpdateWorker> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<UpdateWorker>{}
}