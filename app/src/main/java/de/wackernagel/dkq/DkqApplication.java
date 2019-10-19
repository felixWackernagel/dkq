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
import dagger.android.DaggerApplication;
import dagger.android.DispatchingAndroidInjector;
import de.wackernagel.dkq.dagger.ApplicationComponent;
import de.wackernagel.dkq.dagger.DaggerApplicationComponent;
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
public class DkqApplication extends DaggerApplication implements HasWorkerInjector {

    @Inject
    DispatchingAndroidInjector<Worker> workerInjector;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ACRA.init(this);
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        final ApplicationComponent component = DaggerApplicationComponent
                .builder()
                .application( this )
                .build();
        component.inject( this );
        return component;
    }

    @Override
    public AndroidInjector<Worker> workerInjector() {
        return workerInjector;
    }

}