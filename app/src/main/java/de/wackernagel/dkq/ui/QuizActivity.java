package de.wackernagel.dkq.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.room.entities.Quizzer;
import de.wackernagel.dkq.utils.DateUtils;
import de.wackernagel.dkq.utils.GlideUtils;
import de.wackernagel.dkq.viewmodels.QuestionsViewModel;
import de.wackernagel.dkq.webservice.Resource;

import static de.wackernagel.dkq.DkqConstants.Web.JOOMLA_UNKNOWN_DATE;
import static de.wackernagel.dkq.utils.DateUtils.twoDigits;

public class QuizActivity extends AbstractDkqActivity implements HasSupportFragmentInjector {

    private static final String ARG_QUIZ_ID = "quizId";
    private static final String ARG_QUIZ_NUMBER = "quizNumber";

    static Intent createLaunchIntent(final Context context, final long quizId, final int quizNumber ) {
        final Intent intent = new Intent( context, QuizActivity.class );
        intent.putExtra( ARG_QUIZ_ID, quizId );
        intent.putExtra( ARG_QUIZ_NUMBER, quizNumber );
        return intent;
    }

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private long getQuizId() {
        final Intent intent = getIntent();
        if( intent != null ) {
            return intent.getLongExtra( ARG_QUIZ_ID, 0 );
        }
        return 0;
    }

    private int getQuizNumber() {
        final Intent intent = getIntent();
        if( intent != null ) {
            return intent.getIntExtra( ARG_QUIZ_NUMBER, 0 );
        }
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar( toolbar );

        final ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle( getString(R.string.quiz_number, getQuizNumber()) );
        }

        setFabBehavior( null );
        final QuestionsViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(QuestionsViewModel.class);
        viewModel.loadQuiz( getQuizId(), getQuizNumber() ).observe(this, new Observer<Resource<Quiz>>() {
            @Override
            public void onChanged(@Nullable Resource<Quiz> resource) {
                if( resource != null && resource.data != null ) {
                    setQuizDate( resource.data );
                    setQuizTime( resource.data );
                    setQuizLocation( resource.data );
                    setFabBehavior( resource.data );
                }
            }
        });

        setWinner( null );
        viewModel.loadWinner( getQuizId() ).observe( this, new Observer<Resource<Quizzer>>() {
            @Override
            public void onChanged(@Nullable Resource<Quizzer> resource) {
                if( resource != null && resource.data != null ) {
                    setWinner( resource.data );
                }
            }
        });

        setQuizMaster( null );
        viewModel.loadQuizmaster( getQuizId() ).observe( this, new Observer<Resource<Quizzer>>() {
            @Override
            public void onChanged(@Nullable Resource<Quizzer> resource) {
                if( resource != null && resource.data != null ) {
                    setQuizMaster( resource.data );
                }
            }
        });

        if( savedInstanceState == null ) {
            getSupportFragmentManager().beginTransaction()
                .replace( R.id.container, QuestionsListFragment.newInstance( getQuizId(), getQuizNumber() ), "questions" )
                .commit();
        }
    }

    private void setFabBehavior(@Nullable final Quiz quiz ) {
        final FloatingActionButton fab = findViewById(R.id.fab);
        if( quiz == null ) {
            fab.hide();
        } else {
            fab.show();
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final BottomSheetDialog modalBottomSheet = new BottomSheetDialog( QuizActivity.this );
                    modalBottomSheet.setContentView( R.layout.bottom_sheet_quiz );
                    modalBottomSheet.show();
                }
            });
        }
    }

    private void setWinner( @Nullable final Quizzer quizzer ) {
        setQuizzer( quizzer, R.id.winnerImage, R.id.winner, getString( R.string.quizzers_tab_winners ) );
    }

    private void setQuizMaster( @Nullable final Quizzer quizzer ) {
        setQuizzer( quizzer, R.id.quizMasterImage, R.id.quizMaster, getString( R.string.quizzers_tab_quiz_masters ) );
    }

    private void setQuizzer(@Nullable final Quizzer quizzer, @IdRes final int imageId, @IdRes final int textId, @NonNull final String quizzerRole ) {
        final ImageView image = findViewById( imageId );
        final TextView name = findViewById( textId );
        final String nameData = quizzer != null ? quizzer.name : getString( R.string.unknown_word );
        final String imageData = quizzer != null ? quizzer.image : null;

        GlideUtils.loadCircleImage( image, imageData, true );
        final SpannableString ss = new SpannableString( quizzerRole + "\n" + nameData );
        ss.setSpan( new AbsoluteSizeSpan(10, true), 0, quizzerRole.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
        name.setText( ss, TextView.BufferType.SPANNABLE );
    }

    private void setQuizDate( @NonNull final Quiz quiz ) {
        String value = getString(R.string.unknown_word);
        final TextView date = findViewById(R.id.quiz_date);
        if( quiz.quizDate != null && !quiz.quizDate.equals(JOOMLA_UNKNOWN_DATE) ) {
            final Date quizDate = DateUtils.joomlaDateToJavaDate( quiz.quizDate );
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime( quizDate );
            value = twoDigits(calendar.get(Calendar.DAY_OF_MONTH)) + "." +
                    twoDigits(calendar.get(Calendar.MONTH) + 1) + "." +
                    twoDigits(calendar.get(Calendar.YEAR));
        }
        date.setText( value );
    }

    private void setQuizTime( @NonNull final Quiz quiz ) {
        String value = getString(R.string.unknown_word);
        final TextView time = findViewById(R.id.quiz_time);
        if( quiz.quizDate != null && !quiz.quizDate.equals(JOOMLA_UNKNOWN_DATE) ) {
            final Date quizTime = DateUtils.joomlaDateToJavaDate( quiz.quizDate );
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime( quizTime );
            value = getString( R.string.time_format, twoDigits(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + twoDigits(calendar.get(Calendar.MINUTE)) );
        }
        time.setText( value );
    }

    private void setQuizLocation( @NonNull final Quiz quiz ) {
        String value = getString(R.string.unknown_word);
        final TextView location = findViewById(R.id.quiz_location);
        if( quiz.location != null && !TextUtils.isEmpty( quiz.address ) ) {
            value = quiz.location + "\n" + quiz.address.replaceAll(", ", "\n");
        } else if( quiz.location != null ) {
            value = quiz.location;
        } else if( quiz.address != null ) {
            value = quiz.address.replaceAll(",", "\n");
        }
        location.setText( value );
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }
}
