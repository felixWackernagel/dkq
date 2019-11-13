package de.wackernagel.dkq.webservice;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;

public class LiveDataCallAdapterFactory extends CallAdapter.Factory {

    @Override
    public CallAdapter<?, ?> get( @NonNull final Type returnType, @NonNull final Annotation[] annotations, @NonNull final Retrofit retrofit ) {
        if( getRawType( returnType ) != LiveData.class ) {
            return null;
        }

        final Type observableType = getParameterUpperBound(0, (ParameterizedType) returnType );
        final Class<?> rawObservableType = getRawType( observableType );
        if( rawObservableType != ApiResponse.class ) {
            throw new IllegalArgumentException( "type must be a resource" );
        }
        if( !( observableType instanceof ParameterizedType ) ) {
            throw new IllegalArgumentException( "resource must be parameterized" );
        }

        final Type bodyType = getParameterUpperBound(0, (ParameterizedType) observableType );
        return new LiveDataCallAdapter<>( bodyType );
    }
}
