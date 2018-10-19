package de.wackernagel.dkq.dagger;

import android.app.Application;
import android.util.Log;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.wackernagel.dkq.BuildConfig;
import de.wackernagel.dkq.utils.ConnectivityInterceptor;
import de.wackernagel.dkq.webservice.LiveDataCallAdapterFactory;
import de.wackernagel.dkq.webservice.Webservice;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class RetrofitModule {

    private final Application application;

    public RetrofitModule( final Application application ) {
        this.application = application;
    }

    @Provides
    @Singleton
    Webservice provideWebservice() {
        final OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor( new ConnectivityInterceptor( application ) );

        if (BuildConfig.DEBUG) {
            final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log( final String message ) {
                    Log.i("OkHttp", message);
                }
            });
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            client.addInterceptor(interceptor);
        }

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://felixwackernagel.de/index.php/dkq/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .client(client.build())
                .build();
        return retrofit.create(Webservice.class);
    }
}
