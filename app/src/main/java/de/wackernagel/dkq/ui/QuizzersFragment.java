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
import de.wackernagel.dkq.viewmodels.QuizzerRole;
import de.wackernagel.dkq.viewmodels.QuizzersViewModel;
import de.wackernagel.dkq.webservice.Resource;

public class QuizzersFragment extends Fragment {
    private static final String QUIZZERS_CRITERIA = "criteria";

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private RecyclerView recyclerView;
    private TextView emptyView;
    private ProgressBar progressBar;
    private QuizzerAdapter adapter;

    static QuizzersFragment newInstance(final QuizzerRole criteria) {
        final QuizzersFragment fragment = new QuizzersFragment();
        final Bundle arguments = new Bundle(1);
        arguments.putInt(QUIZZERS_CRITERIA, criteria.ordinal());
        fragment.setArguments( arguments );
        return fragment;
    }

    private QuizzerRole getQuizzersSearchCriteria() {
        int ordinal = 0;
        final Bundle arguments = getArguments();
        if( arguments != null ) {
            ordinal = arguments.getInt(QUIZZERS_CRITERIA, 0);
        }
        return QuizzerRole.values()[ ordinal ];
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

            adapter = new QuizzersFragment.QuizzerAdapter( new QuizzerItemCallback(), getQuizzersSearchCriteria() );
            recyclerView.setLayoutManager( new GridLayoutManager( getContext(), 1 ) );
            recyclerView.setHasFixedSize( true );
            recyclerView.setItemAnimator( animator );
            recyclerView.setAdapter( adapter );
            recyclerView.addItemDecoration( new GridGutterDecoration(
                    DeviceUtils.dpToPx(16, getContext()),
                    1,
                    true,
                    true ) );
            recyclerView.addItemDecoration( new SectionItemDecoration(DeviceUtils.dpToPx(48f, getContext()), false, new SectionItemDecoration.SectionCallback() {
                @Override
                public boolean isSection(int position) {
                    return position == 0;
                }

                @Override
                public CharSequence getSectionHeader(int position) {
                    switch ( getQuizzersSearchCriteria() ) {
                        case WINNER:
                            return getString( R.string.winner_section, adapter.getItemCount() );
                        case QUIZMASTER:
                            return getString( R.string.quizmaster_section, adapter.getItemCount() );
                        default:
                            return "";
                    }
                }
            }) );

            final QuizzersViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(QuizzersViewModel.class);
            viewModel.loadQuizzers( getQuizzersSearchCriteria() ).observe(this, new Observer<Resource<List<QuizzerListItem>>>() {
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
        private ImageView image;
        private TextView name;
        private TextView ranking;

        QuizzerViewHolder(final View itemView) {
            super(itemView);
            image = itemView.findViewById( R.id.image );
            name = itemView.findViewById( R.id.name );
            ranking = itemView.findViewById( R.id.ranking );
        }
    }

    static class QuizzerAdapter extends ListAdapter<QuizzerListItem, QuizzerViewHolder> {
        private final QuizzerRole criteria;

        QuizzerAdapter( @NonNull final DiffUtil.ItemCallback<QuizzerListItem> diffCallback, final QuizzerRole criteria ) {
            super(diffCallback);
            this.criteria = criteria;
        }

        @Override
        public QuizzersFragment.QuizzerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new QuizzersFragment.QuizzerViewHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_quizzer, parent, false ) );
        }

        @Override
        public void onBindViewHolder(final QuizzersFragment.QuizzerViewHolder holder, final int position) {
            if( position != RecyclerView.NO_POSITION ) {
                final QuizzerListItem quizzer = getItem( position);

                GlideUtils.loadCircleImage( holder.image, quizzer.image, false );
                holder.name.setText( quizzer.name );
                final int textRes = criteria == QuizzerRole.WINNER ? R.plurals.quizzers_win_count : R.plurals.quizzers_quiz_master_count;
                holder.ranking.setText( holder.itemView.getResources().getQuantityString( textRes, quizzer.ranking, quizzer.ranking) );
            }
        }
    }
}
