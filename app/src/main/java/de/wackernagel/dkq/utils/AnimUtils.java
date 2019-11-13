package de.wackernagel.dkq.utils;

import android.content.Context;
import android.os.Build;
import android.util.FloatProperty;
import android.util.Property;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

/**
 * Utility methods for working with animations.
 */
class AnimUtils {

    private AnimUtils() { }

    private static Interpolator fastOutSlowIn;

    static Interpolator getFastOutSlowInInterpolator(Context context) {
        if (fastOutSlowIn == null) {
            fastOutSlowIn = AnimationUtils.loadInterpolator(context,
                    android.R.interpolator.fast_out_slow_in);
        }
        return fastOutSlowIn;
    }

    /**
     * A delegate for creating a {@link Property} of <code>float</code> type.
     */
    public static abstract class FloatProp<T> {

        public final String name;

        FloatProp(String name) {
            this.name = name;
        }

        public abstract void set(T object, float value);
        public abstract float get(T object);
    }

    /**
     * The animation framework has an optimization for <code>Properties</code> of type
     * <code>float</code> but it was only made public in API24, so wrap the impl in our own type
     * and conditionally create the appropriate type, delegating the implementation.
     */
    static <T> Property<T, Float> createFloatProperty(final FloatProp<T> impl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return new FloatProperty<T>(impl.name) {
                @Override
                public Float get(T object) {
                    return impl.get(object);
                }

                @Override
                public void setValue(T object, float value) {
                    impl.set(object, value);
                }
            };
        } else {
            return new Property<T, Float>(Float.class, impl.name) {
                @Override
                public Float get(T object) {
                    return impl.get(object);
                }

                @Override
                public void set(T object, Float value) {
                    impl.set(object, value);
                }
            };
        }
    }

}