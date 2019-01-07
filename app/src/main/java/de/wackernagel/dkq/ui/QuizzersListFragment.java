package de.wackernagel.dkq.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import dagger.android.support.AndroidSupportInjection;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.room.entities.QuizzerListItem;
import de.wackernagel.dkq.utils.DeviceUtils;
import de.wackernagel.dkq.utils.GlideUtils;
import de.wackernagel.dkq.utils.GridGutterDecoration;
import de.wackernagel.dkq.utils.SlideUpAlphaAnimator;
import de.wackernagel.dkq.viewmodels.QuizzersViewModel;
import de.wackernagel.dkq.webservice.Resource;

public class QuizzersListFragment extends Fragment {
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private RecyclerView recyclerView;
    private TextView emptyView;
    private ProgressBar progressBar;
    private QuizzerAdapter adapter;

    static QuizzersListFragment newInstance() {
        final QuizzersListFragment fragment = new QuizzersListFragment();
        fragment.setArguments( new Bundle(0) );
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quizzers, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById( R.id.recyclerView );
        emptyView = view.findViewById( R.id.emptyView );
        progressBar = view.findViewById(R.id.progressBar);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onActivityCreated(savedInstanceState);

        if( recyclerView != null ) {
            final SlideUpAlphaAnimator animator = new SlideUpAlphaAnimator().withInterpolator( new FastOutSlowInInterpolator() );
            animator.setAddDuration( 400 );
            animator.setChangeDuration( 400 );
            animator.setMoveDuration( 400 );
            animator.setRemoveDuration( 400 );

            adapter = new QuizzersListFragment.QuizzerAdapter( new QuizzerItemCallback() );
            recyclerView.setLayoutManager( new GridLayoutManager( getContext(), 1 ) );
            recyclerView.setHasFixedSize( true );
            recyclerView.setItemAnimator( animator );
            recyclerView.setAdapter( adapter );
            recyclerView.addItemDecoration( new GridGutterDecoration(
                    DeviceUtils.dpToPx(16, getContext()),
                    1,
                    true,
                    true ) );

            final QuizzersViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(QuizzersViewModel.class);
            viewModel.loadQuizzers().observe(this, new Observer<Resource<List<QuizzerListItem>>>() {
                @Override
                public void onChanged(@Nullable Resource<List<QuizzerListItem>> quizzers) {
                    if( quizzers != null ) {
                        switch (quizzers.status) {
                            case LOADING:
                                progressBar.setVisibility( View.VISIBLE );
                                emptyView.setVisibility( View.GONE );
                                break;
                            case ERROR:
                            case SUCCESS:
                                progressBar.setVisibility( View.GONE );
                                emptyView.setVisibility( quizzers.data != null && quizzers.data.size() > 0 ? View.GONE : View.VISIBLE );
                                break;
                        }
                        adapter.submitList( quizzers.data );
                    }
                }
            });
        }
    }

    static class QuizzerItemCallback extends DiffUtil.ItemCallback<QuizzerListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull QuizzerListItem oldItem, @NonNull QuizzerListItem newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull QuizzerListItem oldItem, @NonNull QuizzerListItem newItem) {
            return TextUtils.equals( oldItem.image, newItem.image )
                && TextUtils.equals( oldItem.name, newItem.name )
                && oldItem.ranking == newItem.ranking;
        }
    }

    static class QuizzerViewHolder extends RecyclerView.ViewHolder {
        final QuizzersListFragment.QuizzerAdapter adapter;

        ImageView image;
        TextView name;
        TextView ranking;

        QuizzerViewHolder(final QuizzersListFragment.QuizzerAdapter adapter, View itemView) {
            super(itemView);
            this.adapter = adapter;
            image = itemView.findViewById( R.id.image );
            name = itemView.findViewById( R.id.name );
            ranking = itemView.findViewById( R.id.ranking );
        }
    }

    static class QuizzerAdapter extends ListAdapter<QuizzerListItem, QuizzerViewHolder> {

        QuizzerAdapter(@NonNull DiffUtil.ItemCallback<QuizzerListItem> diffCallback) {
            super(diffCallback);
        }

        @Override
        public QuizzersListFragment.QuizzerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new QuizzersListFragment.QuizzerViewHolder( this, LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_quizzer, parent, false ) );
        }

        @Override
        public void onBindViewHolder(final QuizzersListFragment.QuizzerViewHolder holder, final int position) {
            if( position != RecyclerView.NO_POSITION ) {
                final QuizzerListItem quizzer = getItem( position);

                GlideUtils.loadCircleImage( holder.image, quizzer.image );
                holder.name.setText( quizzer.name );
                holder.ranking.setText( "" + quizzer.ranking );
            }
        }
    }
}
