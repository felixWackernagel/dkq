package de.wackernagel.dkq.dagger;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;
import de.wackernagel.dkq.DkqApplication;
import de.wackernagel.dkq.dagger.workerfactory.AssistedInjectModule;
import de.wackernagel.dkq.dagger.workerfactory.WorkerBindingModule;

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        RoomModule.class,
        UIModule.class,
        BroadcastReceiverModule.class,
        ViewModelModule.class,
        AssistedInjectModule.class,
        WorkerBindingModule.class })
public interface ApplicationComponent extends AndroidInjector<DkqApplication> {

    @Component.Builder
    interface Builder {

        ApplicationComponent build();

        @BindsInstance
        Builder application(Application application);
    }
}
