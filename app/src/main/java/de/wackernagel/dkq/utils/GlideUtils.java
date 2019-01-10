package de.wackernagel.dkq.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import androidx.annotation.Nullable;
import de.wackernagel.dkq.GlideApp;
import de.wackernagel.dkq.GlideRequest;
import de.wackernagel.dkq.R;

import static de.wackernagel.dkq.utils.AnimUtils.getFastOutSlowInInterpolator;

public class GlideUtils {
    public static void loadImage(final ImageView view, final String url) {
        GlideApp.with( view )
            .load( url )
            .listener( new RequestListener<Drawable>() {
                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    view.setHasTransientState(true);
                    final ObservableColorMatrix cm = new ObservableColorMatrix();
                    final ObjectAnimator saturation = ObjectAnimator.ofFloat(cm, ObservableColorMatrix.SATURATION, 0f, 1f);
                    saturation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            view.setColorFilter(new ColorMatrixColorFilter(cm));
                        }
                    } );
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
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder( R.drawable.image_placeholder )
            .into( view );
    }

    public static void loadCircleImage(final ImageView view, final String url, final boolean white) {
        GlideRequest<Drawable> builder;
        if(TextUtils.isEmpty( url )) {
            builder= GlideApp.with( view ).load( white ? R.drawable.ic_account_circle_white_40dp : R.drawable.ic_account_circle_38_black_40dp);
        } else {
            builder = GlideApp.with( view ).load(url);
        }
        builder.listener( new RequestListener<Drawable>() {
                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    view.setHasTransientState(true);
                    final ObservableColorMatrix cm = new ObservableColorMatrix();
                    final ObjectAnimator saturation = ObjectAnimator.ofFloat(cm, ObservableColorMatrix.SATURATION, 0f, 1f);
                    saturation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            view.setColorFilter(new ColorMatrixColorFilter(cm));
                        }
                    } );
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
            .circleCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into( view );
    }
}
