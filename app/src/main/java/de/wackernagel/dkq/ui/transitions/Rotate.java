package de.wackernagel.dkq.ui.transitions;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;

/**
 * A [Transition] which animates the rotation of a [View].
 */
public class Rotate extends Transition {
    private static String PROP_ROTATION = "dkq:rotate:rotation";
    private static String[] TRANSITION_PROPERTIES = new String[]{ PROP_ROTATION };

    @Keep
    public Rotate() {
        super();
    }

    @Keep
    public Rotate( Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public String[] getTransitionProperties() {
        return TRANSITION_PROPERTIES;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues( TransitionValues transitionValues ) {
        final View view = transitionValues.view;
        if( view == null || view.getWidth() <= 0 || view.getHeight() <= 0 ) {
            return;
        }
        transitionValues.values.put(PROP_ROTATION, view.getRotation());
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null)
            return null;

        float startRotation = (float) startValues.values.get(PROP_ROTATION);
        float endRotation = (float) endValues.values.get(PROP_ROTATION);
        if (startRotation == endRotation)
            return null;

        final View view = endValues.view;
        // ensure the pivot is set
        view.setPivotX( view.getWidth() / 2f );
        view.setPivotY( view.getHeight() / 2f );
        return ObjectAnimator.ofFloat( endValues.view, View.ROTATION, startRotation, endRotation );
    }
}