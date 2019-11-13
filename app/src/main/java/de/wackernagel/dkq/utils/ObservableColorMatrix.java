package de.wackernagel.dkq.utils;

import android.graphics.ColorMatrix;
import android.os.Build;
import android.util.FloatProperty;
import android.util.Property;

/**
 * An extension to {@link ColorMatrix} which caches the saturation value for animation purposes.
 */
public class ObservableColorMatrix extends ColorMatrix {

    private float saturation = 1f;

    ObservableColorMatrix() {
        super();
    }

    float getSaturation() {
        return saturation;
    }

    @Override
    public void setSaturation(float saturation) {
        this.saturation = saturation;
        super.setSaturation(saturation);
    }

    static final Property<ObservableColorMatrix, Float> SATURATION =
        createFloatProperty(
            new FloatProp<ObservableColorMatrix>("saturation") {
                @Override
                public float get(ObservableColorMatrix ocm) {
                    return ocm.getSaturation();
                }

                @Override
                public void set(ObservableColorMatrix ocm, float saturation) {
                    ocm.setSaturation(saturation);
                }
            });

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
    private static <T> Property<T, Float> createFloatProperty(final FloatProp<T> impl) {
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