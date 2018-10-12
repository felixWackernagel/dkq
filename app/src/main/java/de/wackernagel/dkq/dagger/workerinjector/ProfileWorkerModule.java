package de.wackernagel.dkq.dagger.workerinjector;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;
import de.wackernagel.dkq.workers.ProfileWorker;

@Subcomponent
public interface ProfileWorkerModule extends AndroidInjector<ProfileWorker> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ProfileWorker>{}
}