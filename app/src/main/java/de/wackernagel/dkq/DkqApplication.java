package de.wackernagel.dkq;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ConfigurationBuilder;

import javax.inject.Inject;

import androidx.work.Worker;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasBroadcastReceiverInjector;
import de.wackernagel.dkq.dagger.DaggerApplicationComponent;
import de.wackernagel.dkq.dagger.RetrofitModule;
import de.wackernagel.dkq.dagger.RoomModule;
import de.wackernagel.dkq.dagger.workerinjector.HasWorkerInjector;

public class DkqApplication extends Application implements HasActivityInjector, HasWorkerInjector, HasBroadcastReceiverInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    @Inject
    DispatchingAndroidInjector<Worker> workerInjector;

    @Inject
    DispatchingAndroidInjector<BroadcastReceiver> broadcastReceiverInjector;

    @Override
    public void onCreate() {
        super.onCreate();

        DaggerApplicationComponent
                .builder()
                .roomModule( new RoomModule( this, new AppExecutors() ) )
                .retrofitModule( new RetrofitModule( this ) )
                .build()
                .inject( this );

        final ACRAConfiguration config = new ConfigurationBuilder( this )
                .setMailTo( DkqConstants.EMAIL )
                .setResToastText( R.string.crash_toast_text )
                .build();
        ACRA.init(this, config, false);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public AndroidInjector<Worker> workerInjector() {
        return workerInjector;
    }

    @Override
    public AndroidInjector<BroadcastReceiver> broadcastReceiverInjector() {
        return broadcastReceiverInjector;
    }
}