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
}
