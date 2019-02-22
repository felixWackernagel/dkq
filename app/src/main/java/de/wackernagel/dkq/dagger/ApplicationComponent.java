package de.wackernagel.dkq.dagger;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;
import de.wackernagel.dkq.DkqApplication;
import de.wackernagel.dkq.dagger.workerinjector.AndroidWorkerInjectionModule;
import de.wackernagel.dkq.dagger.workerinjector.WorkerModule;

@Singleton
@Component(modules = {AndroidInjectionModule.class, RoomModule.class, UIModule.class, RetrofitModule.class, AndroidWorkerInjectionModule.class, WorkerModule.class, BroadcastReceiverModule.class})
public interface ApplicationComponent extends AndroidInjector<DkqApplication> {
}
