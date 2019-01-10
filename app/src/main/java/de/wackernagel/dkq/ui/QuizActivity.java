package de.wackernagel.dkq.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

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

        final QuestionsViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(QuestionsViewModel.class);
        viewModel.loadQuiz( getQuizId(), getQuizNumber() ).observe(this, new Observer<Resource<Quiz>>() {
            @Override
            public void onChanged(@Nullable Resource<Quiz> resource) {
                if( resource != null && resource.data != null ) {
                    setQuizDetails( resource.data );

                    if( resource.data.winnerId == null ) {
                        setWinner( null );
                    } else {
                        viewModel.loadQuizzer( resource.data.winnerId ).observe(QuizActivity.this, new Observer<Resource<Quizzer>>() {
                            @Override
                            public void onChanged(Resource<Quizzer> quizzerResource) {
                                if( quizzerResource != null )
                                    setWinner( quizzerResource.data );
                                else
                                    setWinner( null );
                            }
                        });
                    }

                    if( resource.data.quizMasterId == null ) {
                        setQuizMaster( null );
                    } else {
                        viewModel.loadQuizzer( resource.data.quizMasterId ).observe(QuizActivity.this, new Observer<Resource<Quizzer>>() {
                            @Override
                            public void onChanged(Resource<Quizzer> quizzerResource) {
                                if( quizzerResource != null )
                                    setQuizMaster( quizzerResource.data );
                                else
                                    setQuizMaster( null );
                            }
                        });
                    }
                }
            }
        });

        if( savedInstanceState == null ) {
            getSupportFragmentManager().beginTransaction()
                .replace( R.id.container, QuestionsListFragment.newInstance( getQuizId(), getQuizNumber() ), "questions" )
                .commit();
        }
    }

    private void setWinner( @Nullable final Quizzer quizzer ) {
        if( quizzer != null ) {
            ImageView image = findViewById(R.id.winnerImage);
            TextView name = findViewById(R.id.winner);
            GlideUtils.loadCircleImage( image, quizzer.image, true );
            SpannableString ss = new SpannableString( "GEWINNER\n" + quizzer.name );
            ss.setSpan( new AbsoluteSizeSpan(10, true), 0, "gewinner".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
            name.setText( ss, TextView.BufferType.SPANNABLE );
        }
    }

    private void setQuizMaster( @Nullable final Quizzer quizzer ) {
        if( quizzer != null ) {
            ImageView image = findViewById(R.id.quizMasterImage);
            TextView name = findViewById(R.id.quizMaster);
            GlideUtils.loadCircleImage( image, quizzer.image, true );
            SpannableString ss = new SpannableString( "QUIZ-MASTER\n" + quizzer.name );
            ss.setSpan( new AbsoluteSizeSpan(10, true), 0, "quiz-master".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
            name.setText( ss, TextView.BufferType.SPANNABLE );
        }
    }

    private void setQuizDetails( @NonNull final Quiz quiz ) {
        final TextView date = findViewById(R.id.quiz_date);
        String value = getString(R.string.unknown_word);
        if( quiz.quizDate != null && !quiz.quizDate.equals("0000-00-00 00:00:00") ) {
            final Date quizDate = DateUtils.joomlaDateToJavaDate( quiz.quizDate );
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( quizDate );
            value = twoDigits(calendar.get(Calendar.DAY_OF_MONTH)) + "." +
                        twoDigits(calendar.get(Calendar.MONTH) + 1) + "." +
                        twoDigits(calendar.get(Calendar.YEAR));
        }
        date.setText( value );

        final TextView time = findViewById(R.id.quiz_time);
        value = getString(R.string.unknown_word);
        if( quiz.quizDate != null && !quiz.quizDate.equals("0000-00-00 00:00:00") ) {
            final Date quizTime = DateUtils.joomlaDateToJavaDate( quiz.quizDate );
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( quizTime );
            value = getString( R.string.time_format, twoDigits(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + twoDigits(calendar.get(Calendar.MINUTE)) );
        }
        time.setText( value );

        final TextView location = findViewById(R.id.quiz_location);
        value = getString(R.string.unknown_word);
        if( quiz.location != null && quiz.address != null ) {
            value = quiz.location + "\n" + quiz.address.replaceAll(", ", "\n");
        } else if( quiz.location != null ) {
            value = quiz.location;
        } else if( quiz.address != null ) {
            value = quiz.address.replaceAll(",", "\n");
        }
        location.setText( value );
    }

    private String twoDigits( int digit ) {
        if( digit < 10 )
            return "0".concat( String.valueOf( digit ) );
        else
            return String.valueOf( digit );
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }
}
