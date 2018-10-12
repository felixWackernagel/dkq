package de.wackernagel.dkq.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import de.wackernagel.dkq.ui.widgets.EmptyAwareRecyclerView;
import de.wackernagel.dkq.utils.DateUtils;
import de.wackernagel.dkq.utils.DeviceUtils;
import de.wackernagel.dkq.utils.GridGutterDecoration;
import de.wackernagel.dkq.utils.SlideUpAlphaAnimator;
import de.wackernagel.dkq.viewmodels.QuizzesViewModel;
import de.wackernagel.dkq.webservice.Resource;

public class QuizzesListFragment extends Fragment {
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private EmptyAwareRecyclerView recyclerView;
    private TextView emptyView;
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
            recyclerView.setEmptyView( emptyView );
            recyclerView.setItemAnimator( animator );
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
                    adapter.submitList( quizzes.data );
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
        }
    }

    static class QuizItemCallback extends DiffUtil.ItemCallback<Quiz> {

        @Override
        public boolean areItemsTheSame(@NonNull Quiz oldItem, @NonNull Quiz newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Quiz oldItem, @NonNull Quiz newItem) {
            return oldItem.number == newItem.number && TextUtils.equals( oldItem.quizDate, newItem.quizDate);
        }
    }

    static class QuizAdapter extends ListAdapter<Quiz, QuizViewHolder> {
        private SimpleDateFormat formatter = new SimpleDateFormat( "EEEE',' dd. MMMM yyyy - HH:mm", Locale.getDefault() );

        QuizAdapter(@NonNull DiffUtil.ItemCallback<Quiz> diffCallback) {
            super(diffCallback);
        }

        @Override
        public QuizViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new QuizViewHolder( this, LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_quiz, parent, false ) );
        }

        @Override
        public void onBindViewHolder(QuizViewHolder holder, int position) {
            if( position != RecyclerView.NO_POSITION ) {
                final Quiz quiz = getItem( position );

                holder.name.setText( holder.itemView.getContext().getString( R.string.quiz_number, quiz.number ) );

                final Date today = new Date();
                final Date quizDate = DateUtils.joomlaDateToJavaDate( quiz.quizDate, today );
                holder.date.setVisibility( quizDate.after( today ) ? View.VISIBLE : View.GONE );
                holder.date.setText( formatter.format( quizDate ) );
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
