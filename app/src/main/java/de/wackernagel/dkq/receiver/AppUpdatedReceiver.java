package de.wackernagel.dkq.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import de.wackernagel.dkq.DkqLog;
import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.utils.AppUtils;

public class AppUpdatedReceiver extends BroadcastReceiver {
    @Inject
    DkqRepository repository;

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this,context);

        if( Intent.ACTION_MY_PACKAGE_REPLACED.equals( intent.getAction() ) ) {
            final int newVersionCode = AppUtils.getVersionCode( context );
            final int oldVersionCode = DkqPreferences.getLastVersionCode( context );
            DkqLog.i("AppUpdatedReceiver","App was updated from version code " + oldVersionCode + " to version code " + newVersionCode);
            if( newVersionCode != oldVersionCode ) {
                updatedToVersion( newVersionCode );
            }
            DkqPreferences.setLastVersionCode( context, newVersionCode );
        }
    }

    private void updatedToVersion( final int newVersionCode ) {
        if( newVersionCode == 4 ) {
            repository.saveUpdateLogMessage( R.string.update_log_version_code_4_title, R.string.update_log_version_code_4_content );
        }
    }
}
