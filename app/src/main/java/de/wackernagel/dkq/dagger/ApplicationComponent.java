package de.wackernagel.dkq.dagger;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;
import de.wackernagel.dkq.DkqApplication;
import de.wackernagel.dkq.dagger.workerinjector.AndroidWorkerInjectionModule;
import de.wackernagel.dkq.dagger.workerinjector.WorkerModule;

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        RoomModule.class,
        UIModule.class,
        AndroidWorkerInjectionModule.class,
        WorkerModule.class,
        BroadcastReceiverModule.class})
public interface ApplicationComponent extends AndroidInjector<DkqApplication> {
    @Component.Builder
    interface Builder {

        ApplicationComponent build();

        @BindsInstance
        Builder application(Application application);
    }
}
