package de.wackernagel.dkq;

import android.content.Context;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import org.acra.data.StringFormat;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import de.wackernagel.dkq.dagger.ApplicationComponent;
import de.wackernagel.dkq.dagger.DaggerApplicationComponent;
import de.wackernagel.dkq.dagger.workerfactory.DkqWorkerFactory;

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
public class DkqApplication extends DaggerApplication implements Configuration.Provider {

    @Inject
    DkqWorkerFactory workerFactory;

    @Override
    public void onCreate() {
        if( BuildConfig.DEBUG ) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
        super.onCreate();
    }

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

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory( workerFactory )
                .build();
    }
}