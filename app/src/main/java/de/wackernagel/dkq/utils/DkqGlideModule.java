package de.wackernagel.dkq.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Read more on:
 * https://bumptech.github.io/glide/doc/configuration.html
 */
@GlideModule
public class DkqGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull final Context context, final GlideBuilder builder) {
        final int diskCacheSizeBytes = 1024 * 1024 * 50; // 50 MB
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
    }

}
