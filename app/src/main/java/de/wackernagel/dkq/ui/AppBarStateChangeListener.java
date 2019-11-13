package de.wackernagel.dkq.ui;

import com.google.android.material.appbar.AppBarLayout;

public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

    public enum State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }
    
    private State currentState = State.IDLE;

    @Override
    public final void onOffsetChanged( AppBarLayout appBarLayout, int offset ) {
        if (offset == 0) {
            if (currentState != State.EXPANDED) {
                onStateChanged(appBarLayout, State.EXPANDED);
            }
            currentState = State.EXPANDED;
        } else if (Math.abs(offset) >= appBarLayout.getTotalScrollRange()) {
            if (currentState != State.COLLAPSED) {
                onStateChanged(appBarLayout, State.COLLAPSED);
            }
            currentState = State.COLLAPSED;
        } else {
            if (currentState != State.IDLE) {
                onStateChanged(appBarLayout, State.IDLE);
            }
            currentState = State.IDLE;
        }
    }

    protected abstract void onStateChanged(AppBarLayout appBarLayout, State state);
}
