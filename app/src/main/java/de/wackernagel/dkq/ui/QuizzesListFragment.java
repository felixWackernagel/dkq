package de.wackernagel.dkq.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.room.entities.QuizListItem;
import de.wackernagel.dkq.utils.DateUtils;
import de.wackernagel.dkq.utils.DeviceUtils;
import de.wackernagel.dkq.utils.GridGutterDecoration;
import de.wackernagel.dkq.utils.SlideUpAlphaAnimator;
import de.wackernagel.dkq.viewmodels.QuizzesViewModel;
import de.wackernagel.dkq.webservice.Resource;

public class QuizzesListFragment extends Fragment {
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private RecyclerView recyclerView;
    private TextView emptyView;
    private ProgressBar progressBar;
    private QuizAdapter adapter;

    static QuizzesListFragment newInstance() {
        final QuizzesListFragment fragment = new QuizzesListFragment();
        fragment.setArguments( new Bundle() );
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quizzes, container, false);
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

            adapter = new QuizAdapter( new QuizItemCallback() );
            recyclerView.setLayoutManager( new GridLayoutManager( getContext(), 1 ) );
            recyclerView.setHasFixedSize( true );
            recyclerView.setItemAnimator( animator );
            recyclerView.setAdapter( adapter );
            recyclerView.addItemDecoration( new GridGutterDecoration(
                    DeviceUtils.dpToPx(16, getContext()),
                    1,
                    true,
                    true ) );

            final QuizzesViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(QuizzesViewModel.class);
            viewModel.loadQuizzes().observe(this, new Observer<Resource<List<QuizListItem>>>() {
                @Override
                public void onChanged(@Nullable Resource<List<QuizListItem>> quizzes) {
                    if( quizzes != null ) {
                        Log.i("dkq", "Status=" + quizzes.status + ", Items=" + (quizzes.data != null ? quizzes.data.size() : "null" ));
                        switch (quizzes.status) {
                            case LOADING:
                                progressBar.setVisibility( View.VISIBLE );
                                emptyView.setVisibility( View.GONE );
                                break;
                            case ERROR:
                            case SUCCESS:
                                progressBar.setVisibility( View.GONE );
                                emptyView.setVisibility( quizzes.data != null && quizzes.data.size() > 0 ? View.GONE : View.VISIBLE );
                                break;
                        }

                        adapter.submitList( quizzes.data );
                    }
                }
            });
        }
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        final QuizAdapter adapter;

        TextView name;
        TextView questionsHint;

        QuizViewHolder(final QuizAdapter adapter, View itemView) {
            super(itemView);
            this.adapter = adapter;
            name = itemView.findViewById( R.id.name );
            questionsHint = itemView.findViewById( R.id.questionsHint);
        }
    }

    static class QuizItemCallback extends DiffUtil.ItemCallback<QuizListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull QuizListItem oldItem, @NonNull QuizListItem newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull QuizListItem oldItem, @NonNull QuizListItem newItem) {
            return oldItem.number == newItem.number && oldItem.questionCount == newItem.questionCount;
        }
    }

    static class QuizAdapter extends ListAdapter<QuizListItem, QuizViewHolder> {
        QuizAdapter(@NonNull DiffUtil.ItemCallback<QuizListItem> diffCallback) {
            super(diffCallback);
        }

        @Override
        public QuizViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new QuizViewHolder( this, LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_quiz, parent, false ) );
        }

        @Override
        public void onBindViewHolder(QuizViewHolder holder, int position) {
            if( position != RecyclerView.NO_POSITION ) {
                final QuizListItem quiz = getItem( position );

                holder.name.setText( holder.itemView.getContext().getString( R.string.quiz_number, quiz.number ) );
                holder.questionsHint.setText( holder.itemView.getContext().getString( R.string.questions_hint, quiz.questionCount) );
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Context context = view.getContext();
                        context.startActivity( QuizActivity.createLaunchIntent( context, quiz.id, quiz.number ) );
                    }
                });
            }
        }
    }
}
