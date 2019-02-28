package de.wackernagel.dkq;

import android.util.Log;

public class DkqLog {
    public static void i( String tag, String msg ) {
        if( BuildConfig.DEBUG ) {
            Log.i("DKQ", tag + ": " + msg);
        }
    }

    public static void d( String tag, String msg ) {
        if( BuildConfig.DEBUG ) {
            Log.d("DKQ", tag + ": " + msg);
        }
    }

    public static void e( String tag, String msg, Throwable e ) {
        if( BuildConfig.DEBUG ) {
            Log.e("DKQ", tag + ": " + msg, e);
        }
    }
}
