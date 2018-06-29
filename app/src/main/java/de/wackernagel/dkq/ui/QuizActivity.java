package de.wackernagel.dkq.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.viewmodels.QuestionsViewModel;
import de.wackernagel.dkq.webservice.Resource;

public class QuizActivity extends AbstractDkqActivity implements HasSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar( toolbar );

        final QuestionsViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(QuestionsViewModel.class);
        viewModel.loadQuiz( getIntent().getLongExtra("quizId", 0 ), getIntent().getIntExtra("quizNumber", 0 ) ).observe(this, new Observer<Resource<Quiz>>() {
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
                        details.setText( "Quiz: " + quiz.number +
                                "\nDatum: " + (quiz.quizDate == null || quiz.quizDate.equals("0000-00-00 00:00:00") ? "?" : quiz.quizDate) +
                                "\nQuiz-Master: " + (TextUtils.isEmpty( quiz.quizMaster ) ? "?" : quiz.quizMaster) +
                                "\nOrt: " + (TextUtils.isEmpty( quiz.location ) ? "?" : quiz.location) +
                                "\nAdresse: " + (TextUtils.isEmpty( quiz.address ) ? "?" : quiz.address) );
                    }
                }
            }
        });

        if( savedInstanceState == null ) {
            getSupportFragmentManager().beginTransaction()
                .replace( R.id.container, QuestionsListFragment.newInstance( getIntent().getLongExtra("quizId", 0), getIntent().getIntExtra("quizNumber", 0) ), "questions" )
                .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate( R.menu.main_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId() ) {
            case R.id.action_open_preferences:
                startActivity( new Intent( getApplicationContext(), PreferencesActivity.class ) );
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }
}
