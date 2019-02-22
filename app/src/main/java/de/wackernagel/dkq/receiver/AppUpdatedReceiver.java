package de.wackernagel.dkq.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
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
            final int newVersionCode = AppUtils.getAppVersionCode( context );
            Log.d("DKQ","App was updated to version code " + newVersionCode);
            updatedToVersion( newVersionCode );
            DkqPreferences.setLastVersionCode( context, newVersionCode );
        }
    }

    private void updatedToVersion( final int newVersionCode ) {
        if( newVersionCode == 4 ) {
            repository.saveUpdateLogMessage( R.string.update_log_version_code_4_title, R.string.update_log_version_code_4_content );
        }
    }
}
