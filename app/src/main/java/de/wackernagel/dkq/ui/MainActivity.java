package de.wackernagel.dkq.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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
import de.wackernagel.dkq.utils.BottomNavigationUtils;
import de.wackernagel.dkq.utils.DateUtils;
import de.wackernagel.dkq.viewmodels.MainViewModel;

public class MainActivity extends AbstractDkqActivity implements HasSupportFragmentInjector, BottomNavigationView.OnNavigationItemSelectedListener {

    @NonNull
    static Intent createLaunchIntent( final Context context ) {
        return new Intent( context, MainActivity.class );
    }

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar( toolbar );

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener( this );

        final MainViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(MainViewModel.class);
        viewModel.installUpdateChecker();
        viewModel.loadNextQuiz().observe(this, new Observer<Quiz>() {
            @Override
            public void onChanged( final Quiz quiz ) {
                final TextView nextQuizTextView = findViewById(R.id.nextQuizTextView);
                if( quiz != null ) {
                    final SimpleDateFormat formatter = new SimpleDateFormat( "dd. MMMM yyyy - HH:mm", Locale.getDefault() );
                    final Date quizDate = DateUtils.joomlaDateToJavaDate( quiz.quizDate );
                    final String formattedDate = quizDate != null ? formatter.format( quizDate ) : "?";

                    nextQuizTextView.setText( getString( R.string.next_quiz, quiz.number, formattedDate) );

                    final CardView toolbarCard = findViewById(R.id.toolbarCard);
                    toolbarCard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final Context context = view.getContext();
                            context.startActivity( QuizDetailsActivity.createLaunchIntent( context, quiz.id, quiz.number ) );
                        }
                    });
                }
                else {
                    nextQuizTextView.setText( getString( R.string.next_quiz_unschedule ) );
                }
            }
        });

        viewModel.getNewMessagesCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if( integer != null && integer > 0 ) {
                    BottomNavigationUtils.showOrUpdateBadge( bottomNavigationView, R.id.action_news, integer.toString() );
                } else {
                    BottomNavigationUtils.removeBadge( bottomNavigationView, R.id.action_news);
                }
            }
        });

        if( savedInstanceState == null ) {
            getSupportFragmentManager().beginTransaction()
                .replace( R.id.container, QuizzesListFragment.newInstance(), "quizzes" )
                .commit();

            final View infoCard = findViewById(R.id.toolbarCard);
            infoCard.setAlpha( 0f );
            infoCard.animate().alpha( 1f ).setDuration( 400L ).setListener( null ).start();
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
            case R.id.menu_preferences:
                startActivity( PreferencesActivity.createLaunchIntent( getApplicationContext() ) );
                return true;

            case R.id.menu_item_development:
                startActivity( DevelopmentActivity.createLaunchIntent( getApplicationContext() ) );
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        Fragment replacement = null;
        String tag = null;

        final Fragment currentFragment = getSupportFragmentManager().findFragmentById( R.id.container );
        switch (item.getItemId()) {
            case R.id.action_news:
                if( !( currentFragment instanceof MessagesListFragment ) ) {
                    replacement = MessagesListFragment.newInstance();
                    tag = "messages";
                }
                break;

            case R.id.action_quizzes:
                if( !( currentFragment instanceof QuizzesListFragment ) ) {
                    replacement = QuizzesListFragment.newInstance();
                    tag = "quizzes";
                }
                break;

            case R.id.action_quizzers:
                if( !( currentFragment instanceof QuizzersViewPagerFragment) ) {
                    replacement = QuizzersViewPagerFragment.newInstance();
                    tag = "quizzers";
                }
                break;
        }

        if( replacement != null ) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, replacement, tag).commit();
        }

        return true;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }
}
