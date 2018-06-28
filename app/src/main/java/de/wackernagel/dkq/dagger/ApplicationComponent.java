package de.wackernagel.dkq.dagger;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;
import de.wackernagel.dkq.DkqApplication;

@Singleton
@Component(modules = {AndroidInjectionModule.class, RoomModule.class, UIModule.class, RetrofitModule.class})
public interface ApplicationComponent extends AndroidInjector<DkqApplication> {
}
