package de.wackernagel.dkq;

import android.app.Activity;
import android.app.Application;
import android.provider.SyncStateContract;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ConfigurationBuilder;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import de.wackernagel.dkq.dagger.DaggerApplicationComponent;
import de.wackernagel.dkq.dagger.RetrofitModule;
import de.wackernagel.dkq.dagger.RoomModule;

public class DkqApplication extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

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
}