/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.wackernagel.dkq.ui.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.content.res.AppCompatResources;
import de.wackernagel.dkq.R;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

public class CollapsibleCard extends FrameLayout {

    public interface OnToggleListener {
        void onToggleClicked( boolean isExpanded );
    }

    private boolean expanded = false;
    private TextView cardTitleView;
    private TextView cardDescriptionView;
    private ImageView expandIcon;
    private Transition toggle;
    private View root;
    private String cardTitle;
    private OnToggleListener listener;

    public CollapsibleCard( final Context context ) {
        this( context, null );
    }

    public CollapsibleCard( final Context context, final AttributeSet attrs ) {
        this( context, attrs, 0 );
    }

    public CollapsibleCard( final Context context, final AttributeSet attrs, @AttrRes final int defStyleAttr ) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CollapsibleCard( final Context context, final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes ) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init( final Context context, final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        final TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.CollapsibleCard, defStyleAttr, defStyleRes);
        cardTitle = arr.getString(R.styleable.CollapsibleCard_cardTitle);
        String cardDescription = arr.getString(R.styleable.CollapsibleCard_cardDescription);
        arr.recycle();
        root = LayoutInflater.from(context).inflate(R.layout.collapsible_card_content, this, true);

        final View titleContainer = root.findViewById(R.id.title_container);
        cardTitleView = root.findViewById(R.id.card_title);
        cardTitleView.setText(cardTitle);
        setTitleContentDescription(cardTitle);
        cardDescriptionView = root.findViewById(R.id.card_description);
        cardDescriptionView.setText(cardDescription);
        expandIcon = root.findViewById(R.id.expand_icon);
        if (SDK_INT < M) {
            expandIcon.setImageTintList(AppCompatResources.getColorStateList(context, R.color.collapsing_section));
        }
        toggle = TransitionInflater.from(context).inflateTransition(R.transition.info_card_toggle);
        titleContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleExpanded();
            }
        });
    }

    public void setCardTitle( final String title ) {
        cardTitleView.setText(title);
    }

    public void setCardDescription( final String description ) {
        cardDescriptionView.setText(description);
    }

    public void setOnToggleListener( @Nullable final OnToggleListener listener ) {
        this.listener = listener;
    }

    private void setTitleContentDescription(String cardTitle) {
        final Resources res = getResources();
        String description = cardTitle + " ";
        if (expanded) {
            description += res.getString(R.string.expanded);
        } else {
            description += res.getString(R.string.collapsed);
        }
        cardTitleView.setContentDescription(description);
    }

    public void setExpanded( boolean isExpanded ) {
        expanded = isExpanded;
        cardDescriptionView.setVisibility(expanded ? View.VISIBLE : View.GONE);
        expandIcon.setRotation(expanded ? 180f : 0f);
        expandIcon.setActivated(expanded);
        cardTitleView.setActivated(expanded);
        setTitleContentDescription(cardTitle);
    }

    private void toggleExpanded() {
        expanded = !expanded;
        toggle.setDuration(expanded ? 300L : 200L);
        // scene root is the RecyclerView for a proper animation
        TransitionManager.beginDelayedTransition((ViewGroup) root.getParent().getParent().getParent(), toggle);
        cardDescriptionView.setVisibility(expanded ? View.VISIBLE : View.GONE);
        expandIcon.setRotation(expanded ? 180f : 0f);
        // activated used to tint controls when expanded
        expandIcon.setActivated(expanded);
        cardTitleView.setActivated(expanded);
        setTitleContentDescription(cardTitle);
        if( listener != null ) {
            listener.onToggleClicked( expanded );
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.expanded = expanded;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            super.onRestoreInstanceState(((SavedState) state).getSuperState());
            if (expanded != ((SavedState) state).expanded) {
                toggleExpanded();
            }
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    public static class SavedState extends BaseSavedState {
        public static Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        boolean expanded = false;

        SavedState(Parcel source) {
            super(source);
            expanded = source.readInt() != 0;
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(expanded ? 1 : 0);
        }
    }
}