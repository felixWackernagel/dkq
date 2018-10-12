package de.wackernagel.dkq.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class EmptyAwareRecyclerView extends RecyclerView {
    private View emptyView;

    public EmptyAwareRecyclerView(@NonNull Context context) {
        super(context);
    }

    public EmptyAwareRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptyAwareRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void initEmptyView() {
        if( emptyView != null ) {
            final boolean isEmpty = getAdapter() == null || getAdapter().getItemCount() == 0;
            emptyView.setVisibility( isEmpty ? VISIBLE : GONE);
        }
    }

    final AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            initEmptyView();
        }
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            initEmptyView();
        }
        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            initEmptyView();
        }
    };
    @Override
    public void setAdapter( final Adapter adapter ) {
        final Adapter oldAdapter = getAdapter();
        super.setAdapter(adapter);
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
    }

    public void setEmptyView( @Nullable final View view ) {
        this.emptyView = view;
        initEmptyView();
    }
}
