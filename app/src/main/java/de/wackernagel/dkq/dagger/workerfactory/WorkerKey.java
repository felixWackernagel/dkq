package de.wackernagel.dkq.dagger.workerfactory;

import androidx.work.ListenableWorker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dagger.MapKey;

@MapKey
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@interface WorkerKey {
    Class<? extends ListenableWorker> value();
}