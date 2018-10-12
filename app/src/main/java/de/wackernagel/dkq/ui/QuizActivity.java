package de.wackernagel.dkq.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import javax.inject.Inject;

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
                if( resource != null ) {
                    switch (resource.status) {
                        case ERROR:
                            break;

                        case LOADING:
                            break;

                        case SUCCESS:
                            break;
                    }
                    if( resource.data != null ) {
                        Quiz quiz = resource.data;
                        TextView details = findViewById(R.id.details);
                        details.setText(
                                "Datum: " + (quiz.quizDate == null || quiz.quizDate.equals("0000-00-00 00:00:00") ? "?" : quiz.quizDate) +
                                "\nQuiz-Master: " + (TextUtils.isEmpty( quiz.quizMaster ) ? "?" : quiz.quizMaster) +
                                "\nOrt: " + (TextUtils.isEmpty( quiz.location ) ? "?" : quiz.location) +
                                "\nAdresse: " + (TextUtils.isEmpty( quiz.address ) ? "?" : quiz.address) );
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

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }
}
