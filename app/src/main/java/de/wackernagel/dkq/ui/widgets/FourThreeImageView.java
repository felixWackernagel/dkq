package de.wackernagel.dkq.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.getSize;
import static android.view.View.MeasureSpec.makeMeasureSpec;

public class FourThreeImageView extends ForegroundImageView {

    public FourThreeImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        final int fourThreeHeight = makeMeasureSpec(getSize(widthSpec) * 3 / 4, EXACTLY);
        super.onMeasure(widthSpec, fourThreeHeight);
    }
}
