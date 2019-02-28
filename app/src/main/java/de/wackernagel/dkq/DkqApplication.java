package de.wackernagel.dkq;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import org.acra.data.StringFormat;

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

@AcraCore(
    buildConfigClass = BuildConfig.class,
    reportFormat = StringFormat.KEY_VALUE_LIST )
@AcraMailSender(
    mailTo = DkqConstants.EMAIL )
@AcraDialog(
    resIcon = 0,
    resNegativeButtonText = R.string.acra_dialog_negativ,
    resPositiveButtonText = R.string.acra_dialog_positiv,
    resText = R.string.acra_dialog_text,
    resTitle = R.string.acra_dialog_title,
    resTheme = R.style.DkqDialogTheme )
public class DkqApplication extends Application implements HasActivityInjector, HasWorkerInjector, HasBroadcastReceiverInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    @Inject
    DispatchingAndroidInjector<Worker> workerInjector;

    @Inject
    DispatchingAndroidInjector<BroadcastReceiver> broadcastReceiverInjector;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ACRA.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DaggerApplicationComponent
                .builder()
                .roomModule( new RoomModule( this, new AppExecutors() ) )
                .retrofitModule( new RetrofitModule( this ) )
                .build()
                .inject( this );
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