package de.wackernagel.dkq.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.wackernagel.dkq.receiver.AppUpdatedReceiver;

@Module
public abstract class BroadcastReceiverModule {

    @ContributesAndroidInjector
    abstract AppUpdatedReceiver contributesAppUpdatedReceiverInject();

}
