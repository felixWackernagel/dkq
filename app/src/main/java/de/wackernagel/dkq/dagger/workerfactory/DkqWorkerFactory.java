package de.wackernagel.dkq.dagger.workerfactory;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerFactory;
import androidx.work.WorkerParameters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

public class DkqWorkerFactory extends WorkerFactory {

    private final Map<Class<? extends ListenableWorker>, Provider<ChildWorkerFactory>> workerFactories;

    @Inject
    public DkqWorkerFactory( final Map<Class<? extends ListenableWorker>, Provider<ChildWorkerFactory>> workerFactories ) {
        this.workerFactories = workerFactories;
    }

    @Nullable
    @Override
    public ListenableWorker createWorker(@NonNull Context appContext, @NonNull String workerClassName, @NonNull WorkerParameters workerParameters) {
        for( Map.Entry<Class<? extends ListenableWorker>, Provider<ChildWorkerFactory>> factory : workerFactories.entrySet() ) {
            try {
                if( Class.forName( workerClassName ).isAssignableFrom( factory.getKey() ) ) {
                    try {
                        return  factory.getValue().get().create( appContext, workerParameters );
                    } catch ( Exception e ) {
                        throw new RuntimeException( "Error during creation of worker '" + workerClassName + "' by factory!", e );
                    }
                }
            } catch ( ClassNotFoundException e ) {
                throw new RuntimeException( "Unknown worker class '" + workerClassName + "'!", e );
            }
        }

        Log.d("DkqWorkerFactory", "Create worker '" + workerClassName + "' without dependencies.");
        try {
            final Class<? extends  Worker> workerClass = Class.forName(workerClassName).asSubclass( Worker.class );
            final Constructor<? extends Worker> workerConstructor = workerClass.getDeclaredConstructor( Context.class, WorkerParameters.class );
            return workerConstructor.newInstance( appContext, workerParameters );
         } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e ) {
           throw new RuntimeException( "Error during creation of worker '" + workerClassName + "' by reflection!", e );
        }
    }
}