package de.wackernagel.dkq;

import android.content.Context;

import de.wackernagel.dkq.utils.PreferenceUtils;

public class DkqPreferences {
    public static boolean showLoginScreen( final Context context ) {
        return PreferenceUtils.getBoolean( context, "show_login_screen", true );
    }

    public static boolean notificationsEnabled( final Context context ) {
        return PreferenceUtils.getBoolean( context, "notification_enabled", true );
    }

    public static boolean soundsEnabled( final Context context ) {
        return PreferenceUtils.getBoolean( context, "sounds_enabled", true );
    }

    public static int getLastVersionCode( final Context context ) {
        return PreferenceUtils.getInt( context, "last_version_code", 0 );
    }

    public static void setLastVersionCode( final Context context, final int versionCode ) {
        PreferenceUtils.setInt( context, "last_version_code", versionCode );
    }
}
