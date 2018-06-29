package de.wackernagel.dkq.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.utils.DeviceUtils;
import de.wackernagel.dkq.utils.GridGutterDecoration;
import de.wackernagel.dkq.viewmodels.QuestionsViewModel;
import de.wackernagel.dkq.webservice.Resource;

public class QuestionsListFragment extends Fragment {
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private RecyclerView recyclerView;
    private QuestionAdapter adapter;

    public static QuestionsListFragment newInstance( long quizId, int quizNumber ) {
        final QuestionsListFragment fragment = new QuestionsListFragment();
        final Bundle arguments = new Bundle();
        arguments.putLong( "quizId", quizId );
        arguments.putInt( "quizNumber", quizNumber );
        fragment.setArguments( arguments );
        return fragment;
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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onActivityCreated(savedInstanceState);

        if( recyclerView != null ) {
            adapter = new QuestionAdapter();
            recyclerView.setLayoutManager( new GridLayoutManager( getContext(), 1 ) );
            recyclerView.setHasFixedSize( true );
            recyclerView.setAdapter( adapter );
            recyclerView.addItemDecoration( new GridGutterDecoration(
                    DeviceUtils.dpToPx(16, getContext()),
                    1,
                    true,
                    true ) );

            final QuestionsViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(QuestionsViewModel.class);
            viewModel.loadQuestions( getArguments().getLong("quizId", 0 ), getArguments().getInt("quizNumber", 0 ) ).observe(this, new Observer<Resource<List<Question>>>() {
                @Override
                public void onChanged(@Nullable Resource<List<Question>> questions) {
                if( questions != null ) {
                    switch (questions.status) {
                        case ERROR:
                            //Toast.makeText(QuestionsListFragment.this.getContext(), "Error " + questions.message, Toast.LENGTH_SHORT).show();
                            break;

                        case LOADING:
                            break;

                        case SUCCESS:
                            break;
                    }
                    if( questions.data == null ) {
                        adapter.setItems( new Question[0]);
                    } else {
                        adapter.setItems(questions.data.toArray(new Question[questions.data.size()]));
                    }
                }
                }
            });
        }
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        final QuestionAdapter adapter;
        TextView name;
        TextView question;

        QuestionViewHolder(final QuestionAdapter adapter, View itemView) {
            super(itemView);
            this.adapter = adapter;
            name = itemView.findViewById( R.id.name );
            question = itemView.findViewById( R.id.question );

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMessageOKCancel( view.getContext(), adapter.items[getAdapterPosition()].answer );
                }
            });
        }

        private void showMessageOKCancel(final Context context, final String message) {
            new AlertDialog.Builder( context )
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .create()
                    .show();
        }
    }

    static class QuestionAdapter extends RecyclerView.Adapter<QuestionViewHolder> {
        private Question[] items;

        @Override
        public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new QuestionViewHolder(this, LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_question, parent, false ) );
        }

        @Override
        public void onBindViewHolder(QuestionViewHolder holder, int position) {
            final Question question = items[position];
            holder.name.setText( holder.itemView.getContext().getString( R.string.question_number, question.number ) );
            holder.question.setText( question.question );
        }

        @Override
        public int getItemCount() {
            return items != null ? items.length : 0;
        }

        void setItems(Question[] items) {
            this.items = items;
            notifyDataSetChanged();
        }
    }
}
