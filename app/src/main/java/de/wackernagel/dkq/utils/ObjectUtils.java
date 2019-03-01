package de.wackernagel.dkq.utils;

import androidx.annotation.Nullable;

public class ObjectUtils {

    public static boolean notEquals( @Nullable final Object object1, @Nullable final Object object2 ) {
        return !equals(object1, object2);
    }

    public static boolean equals( @Nullable final Object object1, @Nullable final Object object2 ) {
        if (object1 == object2) {
            return true;
        }
        if ((object1 == null) || (object2 == null)) {
            return false;
        }
        return object1.equals(object2);
    }
}
