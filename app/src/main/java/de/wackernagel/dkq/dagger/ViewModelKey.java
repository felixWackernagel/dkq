package de.wackernagel.dkq.dagger;

import androidx.lifecycle.ViewModel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dagger.MapKey;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target( ElementType.METHOD )
@Retention( RUNTIME )
@MapKey
public @interface ViewModelKey {
    Class<? extends ViewModel> value();
}
