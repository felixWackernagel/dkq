package de.wackernagel.dkq.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.ui.widgets.CollapsibleCard;
import de.wackernagel.dkq.utils.DeviceUtils;
import de.wackernagel.dkq.utils.GridGutterDecoration;
import de.wackernagel.dkq.utils.SectionItemDecoration;
import de.wackernagel.dkq.utils.SlideUpAlphaAnimator;
import de.wackernagel.dkq.viewmodels.QuestionsViewModel;
import de.wackernagel.dkq.webservice.Resource;

public class QuestionsListFragment extends Fragment {

    private static final String ARG_QUIZ_ID = "quizId";
    private static final String ARG_QUIZ_NUMBER = "quizNumber";

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private RecyclerView recyclerView;
    private TextView emptyView;
    private ProgressBar progressBar;
    private QuestionAdapter adapter;

    static QuestionsListFragment newInstance( final long quizId, final int quizNumber ) {
        final QuestionsListFragment fragment = new QuestionsListFragment();
        final Bundle arguments = new Bundle();
        arguments.putLong( ARG_QUIZ_ID, quizId );
        arguments.putInt( ARG_QUIZ_NUMBER, quizNumber );
        fragment.setArguments( arguments );
        return fragment;
    }

    private long getQuizId() {
        final Bundle arguments = getArguments();
        if( arguments != null ) {
            return arguments.getLong( ARG_QUIZ_ID, 0 );
        }
        return 0;
    }

    private int getQuizNumber() {
        final Bundle arguments = getArguments();
        if( arguments != null ) {
            return arguments.getInt( ARG_QUIZ_NUMBER, 0 );
        }
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_questions, container, false);
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

            adapter = new QuestionAdapter( new QuestionItemCallback() );
            recyclerView.setLayoutManager( new GridLayoutManager( getContext(), 1 ) );
            recyclerView.setItemAnimator( animator );
            recyclerView.setAdapter( adapter );
            recyclerView.addItemDecoration( new GridGutterDecoration(
                    DeviceUtils.dpToPx(16, getContext()),
                    1,
                    false,
                    true,
                    true ) );
            recyclerView.addItemDecoration( new SectionItemDecoration( getContext(),false, new SectionItemDecoration.SectionCallback() {
                @Override
                public boolean isSection(int position) {
                    return position == 0 || position == 20 || position == 40 || position == 60;
                }

                @Override
                public CharSequence getSectionHeader(int position) {
                    int round = 1;
                    if( position >= 20 )
                        round = 2;
                    if( position >= 40 )
                        round = 3;
                    if( position >= 60 )
                        return getString( R.string.additions_section );
                    return getString( R.string.questions_section, round );
                }
            }) );

            final QuestionsViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(QuestionsViewModel.class);
            viewModel.loadQuestions( getQuizId(), getQuizNumber() ).observe(this, new Observer<Resource<List<Question>>>() {
                @Override
                public void onChanged(@Nullable Resource<List<Question>> questions) {
                if( questions != null ) {
                    Log.i("dkq", "Status=" + questions.status + ", Items=" + (questions.data != null ? questions.data.size() : "null" ));
                    switch (questions.status) {
                        case LOADING:
                            progressBar.setVisibility( View.VISIBLE );
                            emptyView.setVisibility( View.GONE );
                            break;
                        case ERROR:
                        case SUCCESS:
                            progressBar.setVisibility( View.GONE );
                            emptyView.setVisibility( questions.data != null && questions.data.size() > 0 ? View.GONE : View.VISIBLE );
                            break;
                    }
                    adapter.submitList( questions.data );
                }
                }
            });
        }
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView question;
        private CollapsibleCard answer;

        QuestionViewHolder(final View itemView) {
            super(itemView);
            name = itemView.findViewById( R.id.name );
            question = itemView.findViewById( R.id.question );
            answer = itemView.findViewById( R.id.answer );
        }
    }

    static class QuestionItemCallback extends DiffUtil.ItemCallback<Question> {
        @Override
        public boolean areItemsTheSame(@NonNull Question oldItem, @NonNull Question newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Question oldItem, @NonNull Question newItem) {
            return oldItem.number == newItem.number && TextUtils.equals(oldItem.question, newItem.question) && TextUtils.equals(oldItem.answer, newItem.answer);
        }
    }

    static class QuestionAdapter extends ListAdapter<Question, QuestionViewHolder> {
        QuestionAdapter(@NonNull DiffUtil.ItemCallback<Question> diffCallback) {
            super(diffCallback);
        }

        @Override
        public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new QuestionViewHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_question, parent, false ) );
        }

        @Override
        public void onBindViewHolder(QuestionViewHolder holder, int position) {
            final Question question = getItem(position);
            holder.name.setText( holder.itemView.getContext().getString( R.string.question_number, question.number ) );
            holder.question.setText( question.question );
            holder.answer.setCardDescription( question.answer );
        }
    }
}
