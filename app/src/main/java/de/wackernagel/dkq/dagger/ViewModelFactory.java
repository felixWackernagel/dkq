package de.wackernagel.dkq.dagger;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

// https://github.com/Eli-Fox/LEGO-Catalog/blob/master/app/src/main/java/com/elifox/legocatalog/di/ViewModelFactory.kt
@Singleton
public class ViewModelFactory implements ViewModelProvider.Factory {

    private final Map<Class<? extends ViewModel>, Provider<ViewModel>> creators;

    @Inject
    ViewModelFactory( final Map<Class<? extends ViewModel>, Provider<ViewModel>> creators ) {
        this.creators = creators;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create( @NonNull final Class<T> modelClass ) {
        for( Map.Entry<Class<? extends ViewModel>, Provider<ViewModel>> creator : creators.entrySet() ) {
            if( modelClass.isAssignableFrom( creator.getKey() ) ) {
                try {
                    return  (T) creator.getValue().get();
                } catch ( Exception e ) {
                    throw new RuntimeException( "Error by creating model " + modelClass.getSimpleName(), e );
                }
            }
        }
        throw new IllegalArgumentException( "Unknown model class " + modelClass.getSimpleName() );
    }
}
