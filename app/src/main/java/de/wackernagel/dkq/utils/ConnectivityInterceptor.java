package de.wackernagel.dkq.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

import de.wackernagel.dkq.R;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ConnectivityInterceptor implements Interceptor {

    private Context mContext;

    public ConnectivityInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept( final Chain chain ) throws IOException {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

        if (!(netInfo != null && netInfo.isConnected())) {
            throw new NoConnectivityException( mContext.getString(R.string.no_connection_error) );
        }

        Request.Builder builder = chain.request().newBuilder();
        return chain.proceed(builder.build());
    }

    public static class NoConnectivityException extends IOException {
        NoConnectivityException( final String localizedMessage ) {
            super( localizedMessage );
        }
    }
}