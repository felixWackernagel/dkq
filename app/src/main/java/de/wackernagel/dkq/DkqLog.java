package de.wackernagel.dkq;

import android.util.Log;

public class DkqLog {
    public static void i( String tag, String msg ) {
        if( BuildConfig.DEBUG ) {
            Log.i("DKQ", String.format("%s: %s", tag, msg ) );
        }
    }

    public static void d( String tag, String msg ) {
        if( BuildConfig.DEBUG ) {
            Log.d("DKQ", String.format("%s: %s", tag, msg ));
        }
    }

    public static void e( String tag, String msg ) {
        e( tag, msg, null );
    }

    public static void e( String tag, String msg, Throwable e ) {
        if( BuildConfig.DEBUG ) {
            if( e == null ) {
                Log.e("DKQ", String.format("%s: %s", tag, msg ) );
            } else {
                Log.e("DKQ", String.format("%s: %s", tag, msg ), e);
            }
        }
    }
}
