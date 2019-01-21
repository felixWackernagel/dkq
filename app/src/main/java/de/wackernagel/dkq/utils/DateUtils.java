package de.wackernagel.dkq.utils;

import android.text.TextUtils;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import androidx.annotation.Nullable;

public class DateUtils {

    public static boolean notEquals( final String date, final String otherDate ) {
        return !equals( date, otherDate );
    }

    public static boolean equals( final String date, final String otherDate ) {
        return TextUtils.equals( date, otherDate );
    }

    /**
     * @param dateTime 'YYYY-MM-DD HH:MM:SS'
     * @return Converted date or null
     */
    @Nullable
    public static Date joomlaDateToJavaDate( final String dateTime) {
        return joomlaDateToJavaDate( dateTime, null);
    }

    /**
     * @param dateTime 'YYYY-MM-DD HH:MM:SS'
     * @param fallback on wrong format
     * @return Converted date or fallback
     */
    public static Date joomlaDateToJavaDate( final String dateTime, final Date fallback ) {
        if( dateTime == null || dateTime.length() != 19 ) {
            Log.d( "DKQ", "Wrong dateTime format." );
            return fallback;
        }

        final Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(
                toInt( dateTime.substring(0, 4) ),
                toInt( dateTime.substring(5, 7) ) - 1,
                toInt( dateTime.substring(8, 10) ),
                toInt( dateTime.substring(11, 13) ),
                toInt( dateTime.substring(14, 16) ),
                toInt( dateTime.substring(17, 19) )
        );

        return calendar.getTime();
    }

    private static int toInt( final String value ) {
        if( TextUtils.isEmpty( value ) ) {
            return 0;
        }
        for( int index = 0; index < value.length(); index++ ) {
            if( value.charAt( index ) != '0' ) {
                return Integer.parseInt( value.substring( index ) );
            }
        }
        return 0;
    }

    public static String twoDigits( int digit ) {
        if( digit < 10 )
            return "0".concat( String.valueOf( digit ) );
        else
            return String.valueOf( digit );
    }

}
