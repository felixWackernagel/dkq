package de.wackernagel.dkq.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.wackernagel.dkq.utils.AppUtils;

public class AppUpdatedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if( Intent.ACTION_MY_PACKAGE_REPLACED.equals( intent.getAction() ) ) {
            updatedToVersion( AppUtils.getAppVersionCode( context ) );
        }
    }

    private void updatedToVersion( final int newVersionCode ) {
        Log.d("DKQ","App was updated to version code " + newVersionCode);
    }
}
