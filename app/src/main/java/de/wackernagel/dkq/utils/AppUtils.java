package de.wackernagel.dkq.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class AppUtils {

    public static int getAppVersionCode(final Context context ) {
        try {
            return context.getPackageManager().getPackageInfo( context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

}
