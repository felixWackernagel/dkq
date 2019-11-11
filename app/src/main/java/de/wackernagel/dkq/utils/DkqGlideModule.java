package de.wackernagel.dkq.utils;

import android.app.ActivityManager;
import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

import static android.content.Context.ACTIVITY_SERVICE;
import static com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888;
import static com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565;

/**
 * Read more on:
 * https://bumptech.github.io/glide/doc/configuration.html
 */
@GlideModule
public class DkqGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull final Context context, final GlideBuilder builder) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        builder.setDefaultRequestOptions( new RequestOptions()
                .format( activityManager.isLowRamDevice() ? PREFER_RGB_565 : PREFER_ARGB_8888 )
                .disallowHardwareConfig() );

        final int diskCacheSizeBytes = 1024 * 1024 * 50; // 50 MB
        builder.setDiskCache( new InternalCacheDiskCacheFactory( context, diskCacheSizeBytes ) );
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

}
