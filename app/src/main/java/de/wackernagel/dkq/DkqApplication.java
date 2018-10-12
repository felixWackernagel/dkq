package de.wackernagel.dkq;

import android.app.Activity;
import android.app.Application;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ConfigurationBuilder;

import javax.inject.Inject;

import androidx.work.Worker;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import de.wackernagel.dkq.dagger.DaggerApplicationComponent;
import de.wackernagel.dkq.dagger.RetrofitModule;
import de.wackernagel.dkq.dagger.RoomModule;
import de.wackernagel.dkq.dagger.workerinjector.HasWorkerInjector;

public class DkqApplication extends Application implements HasActivityInjector, HasWorkerInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    @Inject DispatchingAndroidInjector<Worker> workerDispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();

        DaggerApplicationComponent
                .builder()
                .roomModule( new RoomModule( this ) )
                .retrofitModule( new RetrofitModule( this ) )
                .build()
                .inject( this );

        final ACRAConfiguration config = new ConfigurationBuilder( this )
                .setMailTo( "dkq@felixwackernagel.de" )
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
        return workerDispatchingAndroidInjector;
    }
}