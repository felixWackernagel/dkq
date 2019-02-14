package de.wackernagel.dkq.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import de.wackernagel.dkq.R;

public final class BottomNavigationUtils {

    private BottomNavigationUtils() {
        // no instance needed
    }

    public static void showOrUpdateBadge( @NonNull final BottomNavigationView bottomNavigationView, @IdRes final int itemId, final String value ) {
        final BottomNavigationItemView itemView = bottomNavigationView.findViewById( itemId );
        if( itemView != null ) {
            View badge = itemView.findViewById( R.id.badge_container );
            if( badge == null ) {
                badge = LayoutInflater.from(bottomNavigationView.getContext()).inflate(R.layout.bottom_navigation_badge, bottomNavigationView, false);
                itemView.addView( badge );
            }
            final TextView text = badge.findViewById( R.id.badge_text_view );
            text.setText( value );
        }
    }

    public static void removeBadge( @NonNull final BottomNavigationView bottomNavigationView, @IdRes final int itemId ) {
        final BottomNavigationItemView itemView = bottomNavigationView.findViewById( itemId );
        if( itemView != null ) {
            final View badge = itemView.findViewById( R.id.badge_container );
            if( badge != null ) {
                itemView.removeView( badge );
            }
        }
    }

}
