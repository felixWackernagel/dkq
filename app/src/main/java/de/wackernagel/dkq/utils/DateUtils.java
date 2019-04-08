package de.wackernagel.dkq.utils;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DateUtils {

    public static boolean notEquals( final String date, final String otherDate ) {
        return !equals( date, otherDate );
    }

    public static boolean equals( final String date, final String otherDate ) {
        return TextUtils.equals( date, otherDate );
    }

    public static boolean isJoomlaDateInFuture( final String dateTime ) {
        final Date now = new Date();
        final Date date = joomlaDateToJavaDate( dateTime, now );
        return date.after( now );
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
    @Nullable
    private static Date joomlaDateToJavaDate( final String dateTime, @Nullable final Date fallback ) {
        if( dateTime == null || "0000-00-00 00:00:00".equals( dateTime ) || dateTime.length() != 19 ) {
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

    private static String twoDigits( int digit ) {
        if( digit < 10 )
            return "0".concat( String.valueOf( digit ) );
        else
            return String.valueOf( digit );
    }

    public static String getDateFromJoomlaDate( @Nullable final String joomlaDate, @Nullable final String fallback ) {
        final Date javaDate = joomlaDateToJavaDate( joomlaDate );
        if( javaDate != null ) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime( javaDate );
            return twoDigits(calendar.get(Calendar.DAY_OF_MONTH)) + "." +
                   twoDigits(calendar.get(Calendar.MONTH) + 1) + "." +
                   twoDigits(calendar.get(Calendar.YEAR));
        } else {
            return fallback;
        }
    }

    public static String getTimeFromJoomlaDate( @Nullable final String joomlaDate, @Nullable final String fallback ) {
        final Date javaDate = joomlaDateToJavaDate( joomlaDate );
        if( javaDate != null ) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime( javaDate );
            return twoDigits( calendar.get(Calendar.HOUR_OF_DAY) ) + ":" +
                   twoDigits( calendar.get( Calendar.MINUTE ) );
        } else {
            return fallback;
        }
    }

    public static String javaDateToJoomlaDate( @NonNull final Date date ) {
        return new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss", Locale.getDefault() ).format( date );
    }
}
