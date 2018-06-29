package de.wackernagel.dkq.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.utils.DateUtils;
import de.wackernagel.dkq.utils.DeviceUtils;
import de.wackernagel.dkq.utils.GridGutterDecoration;
import de.wackernagel.dkq.viewmodels.QuizzesViewModel;
import de.wackernagel.dkq.webservice.Resource;

public class QuizzesListFragment extends Fragment {
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private RecyclerView recyclerView;
    private QuizAdapter adapter;

    public static QuizzesListFragment newInstance() {
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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onActivityCreated(savedInstanceState);

        if( recyclerView != null ) {
            adapter = new QuizAdapter();
            recyclerView.setLayoutManager( new GridLayoutManager( getContext(), 1 ) );
            recyclerView.setHasFixedSize( true );
            recyclerView.setAdapter( adapter );
            recyclerView.addItemDecoration( new GridGutterDecoration(
                    DeviceUtils.dpToPx(16, getContext()),
                    1,
                    true,
                    true ) );

            final QuizzesViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(QuizzesViewModel.class);
            viewModel.loadQuizzes().observe(this, new Observer<Resource<List<Quiz>>>() {
                @Override
                public void onChanged(@Nullable Resource<List<Quiz>> quizzes) {
                if( quizzes != null ) {
                    switch (quizzes.status) {
                        case ERROR:
                            //Toast.makeText(QuizzesListFragment.this.getContext(), "Error " + quizzes.message, Toast.LENGTH_SHORT).show();
                            break;

                        case LOADING:
                            break;

                        case SUCCESS:
                            break;
                    }
                    if( quizzes.data == null ) {
                        adapter.setItems( new Quiz[0]);
                    } else {
                        adapter.setItems(quizzes.data.toArray(new Quiz[quizzes.data.size()]));
                    }
                }
                }
            });
        }
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        final QuizAdapter adapter;

        TextView name;
        TextView date;

        QuizViewHolder(final QuizAdapter adapter, View itemView) {
            super(itemView);
            this.adapter = adapter;
            name = itemView.findViewById( R.id.name );
            date = itemView.findViewById( R.id.date );
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Quiz quiz = adapter.items[position];
                    view.getContext().startActivity(new Intent( view.getContext(), QuizActivity.class ).putExtra("quizId", quiz.id) );
                }
            });
        }
    }

    static class QuizAdapter extends RecyclerView.Adapter<QuizViewHolder> {
        private Quiz[] items;

        private SimpleDateFormat formatter = new SimpleDateFormat( "EEEE',' dd. MMMM yyyy - HH:mm", Locale.getDefault() );

        @Override
        public QuizViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new QuizViewHolder( this, LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_quiz, parent, false ) );
        }

        @Override
        public void onBindViewHolder(QuizViewHolder holder, int position) {
            if( position != RecyclerView.NO_POSITION ) {
                final Quiz quiz = items[position];

                holder.name.setText( holder.itemView.getContext().getString( R.string.quiz_number, quiz.number ) );

                final Date today = new Date();
                final Date quizDate = DateUtils.joomlaDateToJavaDate( quiz.quizDate, today );
                holder.date.setVisibility( quizDate.after( today ) ? View.VISIBLE : View.GONE );
                holder.date.setText( formatter.format( quizDate ) );
            }

        }

        @Override
        public int getItemCount() {
            return items != null ? items.length : 0;
        }

        void setItems(Quiz[] items) {
            this.items = items;
            notifyDataSetChanged();
        }
    }
}
