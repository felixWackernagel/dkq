package de.wackernagel.dkq.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;

import de.wackernagel.dkq.R;

import static de.wackernagel.dkq.utils.AnimUtils.getFastOutSlowInInterpolator;

public class GlideUtils {
    public static void loadImage( final ImageView view, @Nullable final String url, final int version ) {
        loadImage( view, url,  version, 0, 0 );
    }

    public static void loadImage( final ImageView view, @Nullable final String url, final int version, final int width, final int height ) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .signature( new ObjectKey( version + "-" + width + "-" + height ) )
                .fallback( R.drawable.image_placeholder );

        if( width > 0 && height > 0 ) {
            options = options.override( width, height );
        }

        Glide.with( view )
            .load( url )
            .listener( new RequestListener<Drawable>() {
                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    view.setHasTransientState(true);
                    final ObservableColorMatrix cm = new ObservableColorMatrix();
                    final ObjectAnimator saturation = ObjectAnimator.ofFloat(cm, ObservableColorMatrix.SATURATION, 0f, 1f);
                    saturation.addUpdateListener(animation -> view.setColorFilter(new ColorMatrixColorFilter(cm)));
                    saturation.setDuration(2000L);
                    saturation.setInterpolator(getFastOutSlowInInterpolator(view.getContext()));
                    saturation.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.clearColorFilter();
                            view.setHasTransientState(false);
                        }
                    });
                    saturation.start();
                    return false;
                }

                @Override
                public boolean onLoadFailed(@Nullable final GlideException e, final Object model, final Target<Drawable> target, final boolean isFirstResource) {
                    return false;
                }
            } )
            .apply(options)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into( view );
    }

    public static void loadCircleThumbnailImage( final ImageView view, @Nullable final String url, final boolean white, final int version ) {
        final int fallbackResId = white ? R.drawable.ic_account_circle_white_40dp : R.drawable.ic_account_circle_38_black_40dp;
        final int size = view.getResources().getDimensionPixelSize( R.dimen.thumbnail_image_size);
        final String key = "thumb-" + version;

        final RequestOptions options = new RequestOptions()
                .diskCacheStrategy( DiskCacheStrategy.RESOURCE )
                .signature( new ObjectKey( key ) )
                .fallback( fallbackResId )
                .circleCrop()
                .override( size );

        Glide.with( view )
            .load( url )
            .apply( options )
            .transition( DrawableTransitionOptions.withCrossFade() )
            .into( view );
    }

    public static int fourThreeHeightOf( final int width ) {
        return width * 3 / 4;
    }
}
