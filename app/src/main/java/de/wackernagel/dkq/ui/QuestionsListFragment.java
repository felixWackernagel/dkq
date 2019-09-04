package de.wackernagel.dkq.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import de.wackernagel.dkq.DkqLog;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.ui.widgets.CollapsibleCard;
import de.wackernagel.dkq.utils.DeviceUtils;
import de.wackernagel.dkq.utils.GlideUtils;
import de.wackernagel.dkq.utils.GridGutterDecoration;
import de.wackernagel.dkq.utils.SectionItemDecoration;
import de.wackernagel.dkq.utils.SlideUpAlphaAnimator;
import de.wackernagel.dkq.viewmodels.QuestionsViewModel;
import de.wackernagel.dkq.webservice.Status;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
            viewModel.loadQuestions( getQuizId(), getQuizNumber() ).observe(this, questions -> {
                if( questions != null ) {
                    DkqLog.i("QuestionsListFragment", "Status=" + questions.status + ", Items=" + (questions.data != null ? questions.data.size() : "null" ) + ", Message=" + questions.message );

                    progressBar.setVisibility( questions.status == Status.LOADING ? VISIBLE : GONE );
                    emptyView.setVisibility( questions.status != Status.LOADING && ( questions.data == null || questions.data.isEmpty() ) ? VISIBLE : GONE );

                    adapter.submitList( questions.data );
                }
            });
        }
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView question;
        private ImageView image;
        private CollapsibleCard answer;

        QuestionViewHolder(final View itemView) {
            super(itemView);
            name = itemView.findViewById( R.id.name );
            question = itemView.findViewById( R.id.question );
            answer = itemView.findViewById( R.id.answer );
            image = itemView.findViewById( R.id.image );
        }
    }

    static class QuestionItemCallback extends DiffUtil.ItemCallback<Question> {
        @Override
        public boolean areItemsTheSame(@NonNull Question oldItem, @NonNull Question newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Question oldItem, @NonNull Question newItem) {
            return oldItem.number == newItem.number &&
                    TextUtils.equals(oldItem.question, newItem.question) &&
                    TextUtils.equals(oldItem.answer, newItem.answer) &&
                    TextUtils.equals(oldItem.image, newItem.image);
        }
    }

    static class QuestionAdapter extends ListAdapter<Question, QuestionViewHolder> {
        private Set<Integer> openCards = new HashSet<>();

        QuestionAdapter(@NonNull DiffUtil.ItemCallback<Question> diffCallback) {
            super(diffCallback);
            setHasStableIds( true );
        }

        @Override
        public long getItemId(int position) {
            return getItem( position ).id;
        }

        @Override
        public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new QuestionViewHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_question, parent, false ) );
        }

        @Override
        public void onBindViewHolder( final QuestionViewHolder holder, final int position) {
            final Question question = getItem(position);
            holder.name.setText( holder.itemView.getContext().getString( R.string.question_number, question.number ) );
            holder.question.setText( question.question );
            holder.answer.setCardDescription( question.answer );
            holder.answer.setExpanded( openCards.contains( position ) );
            holder.answer.setOnToggleListener(isExpanded -> {
                if( isExpanded ) {
                    openCards.add( position );
                } else {
                    openCards.remove( position );
                }
            });

            if( !TextUtils.isEmpty( question.image ) ) {
                GlideUtils.loadImage( holder.image, question.image );
                holder.image.setVisibility( VISIBLE );
                final Intent viewImageIntent = new Intent(Intent.ACTION_VIEW);
                viewImageIntent.setDataAndType(Uri.parse(question.image), "image/*");
                if ( holder.itemView.getContext().getPackageManager().queryIntentActivities( viewImageIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0 ) {
                    holder.image.setOnClickListener(v -> v.getContext().startActivity(viewImageIntent));
                    holder.image.setOnLongClickListener(view -> {
                        // position toast below image and at the center of the device
                        int x = 0;
                        int y = view.getBottom() + DeviceUtils.dpToPx( 16f, view.getContext() );
                        Toast toast = Toast.makeText( view.getContext(), R.string.maximize_image, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, x, y);
                        toast.show();
                        return true;
                    });
                }
            }
            else {
                Glide.with( holder.image ).clear( holder.image );
                holder.image.setVisibility( GONE );
                holder.image.setOnClickListener( null );
                holder.image.setOnLongClickListener( null );
            }
        }
    }
}
