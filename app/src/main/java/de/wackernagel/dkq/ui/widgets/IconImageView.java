package de.wackernagel.dkq.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.utils.DeviceUtils;

public class IconImageView extends FourThreeImageView {

    private Drawable icon;
    private int iconSpace;
    private int iconSize;

    public IconImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize( context );
    }

    private void initialize( final Context context) {
        iconSize = DeviceUtils.dpToPx(24f, context );
        iconSpace = DeviceUtils.dpToPx(16f, context );
        icon = ContextCompat.getDrawable( context, R.drawable.ic_crop_free_white_48dp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        icon.setBounds( getWidth() - iconSpace - iconSize, getHeight() - iconSpace - iconSize, getWidth() - iconSpace, getHeight() - iconSpace);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        icon.draw(canvas);
    }

}
