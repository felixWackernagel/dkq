package de.wackernagel.dkq.dagger.workerinjector;

import java.util.Map;

import androidx.work.Worker;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.multibindings.Multibinds;

@Module
public abstract class AndroidWorkerInjectionModule {

    @Multibinds
    abstract Map<Class<? extends Worker>, AndroidInjector.Factory<? extends Worker>>
    workerInjectorFactories();
}
