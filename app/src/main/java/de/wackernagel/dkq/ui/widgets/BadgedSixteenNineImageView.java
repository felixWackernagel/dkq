package de.wackernagel.dkq.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.wackernagel.dkq.R;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.SUBPIXEL_TEXT_FLAG;
import static android.graphics.PorterDuff.Mode.CLEAR;

public class BadgedSixteenNineImageView extends SixteenNineImageView {

    private Drawable badge;
    private boolean drawBadge = true;
    private boolean badgeBoundsSet = false;
    private int badgeGravity = Gravity.END | Gravity.BOTTOM;
    private int badgePadding = 0;
    private String badgeText = "";
    private int badgeColor = Color.WHITE;

    public BadgedSixteenNineImageView( final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init( context, attrs );
    }

    private void init( final Context context, final AttributeSet attrs) {
        if( attrs != null ) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BadgedSixteenNineImageView, 0, 0);
            badgeGravity = a.getInt(R.styleable.BadgedSixteenNineImageView_badgeGravity, badgeGravity );
            badgePadding = a.getDimensionPixelSize(R.styleable.BadgedSixteenNineImageView_badgePadding, badgePadding);
            final String text = a.getString(R.styleable.BadgedSixteenNineImageView_badgeText);
            badgeText = text != null ? text : badgeText;
            badgeColor = a.getColor(R.styleable.BadgedSixteenNineImageView_badgeColor, badgeColor);
            a.recycle();
        }
        badge = new TextBadge(context, badgeText);
        badge.setColorFilter(badgeColor, PorterDuff.Mode.SRC_IN);
    }

    public void drawBadge( final boolean drawBadge ) {
        this.drawBadge = drawBadge;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if( drawBadge ) {
            if( !badgeBoundsSet ) {
                layoutBadge();
            }
            badge.draw(canvas);
        }
    }

    public void onSizeChanged( int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        layoutBadge();
    }

    private void layoutBadge() {
        Rect badgeBounds = badge.getBounds();
        Gravity.apply(badgeGravity,
                badge.getIntrinsicWidth(),
                badge.getIntrinsicHeight(),
                new Rect(0, 0, getWidth(), getHeight()),
                badgePadding,
                badgePadding,
                badgeBounds);
        badge.setBounds( badgeBounds );
        badgeBoundsSet = true;
    }

    private static class TextBadge extends Drawable {

        private Context context;
        private Paint paint = new Paint();
        private Bitmap bitmap;
        private String text;

        TextBadge( final Context context, final String text ) {
            super();
            this.context = context;
            this.text = text;
            init();
        }

        private void init() {
            if( bitmap == null ) {
                DisplayMetrics dm = context.getResources().getDisplayMetrics();
                float density = dm.density;
                float scaledDensity = dm.scaledDensity;
                TextPaint textPaint = new TextPaint(ANTI_ALIAS_FLAG | SUBPIXEL_TEXT_FLAG);
                textPaint.setTypeface( Typeface.create("sans-serif-black", Typeface.NORMAL) );
                textPaint.setTextSize( 12 * scaledDensity ); //sp

                float padding = 4 * density; // dp
                Rect textBounds = new Rect();
                textPaint.getTextBounds(text, 0, text.length(), textBounds);
                float height = padding + textBounds.height() + padding;
                float width = padding + textBounds.width() + padding;
                bitmap = createBitmap(Math.round(width), Math.round(height),Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                final Paint backgroundPaint = new Paint(ANTI_ALIAS_FLAG);
                backgroundPaint.setColor( Color.WHITE );
                float cornerRadius = 2 * density; // dp
                canvas.drawRoundRect(0f, 0f, width, height, cornerRadius, cornerRadius, backgroundPaint);
                // punch out the word '<text>', leaving transparency
                textPaint.setXfermode( new PorterDuffXfermode(CLEAR) );
                canvas.drawText(text, padding, height - padding, textPaint);
            }
        }

        @Override
        public int getIntrinsicWidth() {
            return bitmap != null ? bitmap.getWidth() : 0;
        }

        @Override
        public int getIntrinsicHeight() {
            return bitmap != null ? bitmap.getHeight() : 0;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawBitmap(bitmap, (float) getBounds().left, (float) getBounds().top, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
