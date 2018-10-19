package de.wackernagel.dkq.utils;

import android.app.Application;
import android.content.pm.PackageManager;

public class AppUtils {

    public static int getAppVersion( final Application application ) {
        try {
            return application.getPackageManager().getPackageInfo( application.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

}
