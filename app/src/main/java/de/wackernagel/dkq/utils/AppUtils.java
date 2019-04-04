package de.wackernagel.dkq.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class AppUtils {

    public static int getVersionCode(final Context context ) {
        try {
            return context.getPackageManager().getPackageInfo( context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    public static String getVersionName(final Context context ) {
        try {
            return context.getPackageManager().getPackageInfo( context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "?";
        }
    }

}
