package de.wackernagel.dkq.dagger.workerinjector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.work.Worker;
import dagger.MapKey;

@MapKey
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@interface WorkerKey {
    Class<? extends Worker> value();
}