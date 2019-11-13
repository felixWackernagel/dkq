package de.wackernagel.dkq.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.utils.BottomNavigationUtils;
import de.wackernagel.dkq.utils.DateUtils;
import de.wackernagel.dkq.viewmodels.MainViewModel;

public class MainActivity extends AbstractDkqActivity implements BottomNavigationView.OnNavigationItemSelectedListener, HasAndroidInjector {

    public static String FRAGMENT_QUIZZES = "quizzes";
    public static String FRAGMENT_MESSAGES = "messages";
    public static String FRAGMENT_QUIZZERS = "quizzers";

    private static String ARG_START_FRAGMENT = "mainStartFragment";

    @NonNull
    public static Intent createLaunchIntent( final Context context ) {
        return createLaunchIntent( context, FRAGMENT_QUIZZES );
    }

    @NonNull
    public static Intent createLaunchIntent( final Context context, final String startFragment ) {
        final Intent intent = new Intent( context, MainActivity.class );
        intent.putExtra( ARG_START_FRAGMENT, startFragment );
        return intent;
    }

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    DispatchingAndroidInjector<Object> dispatchingAndroidInjector;

    private String getStartFragment() {
        final Intent intent = getIntent();
        if( intent != null ) {
            final String startFragment = intent.getStringExtra( ARG_START_FRAGMENT );
            if( !TextUtils.isEmpty( startFragment ) ) {
                return startFragment;
            }
        }
        return FRAGMENT_QUIZZES;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LinearLayout toolbarInfo = findViewById(R.id.toolbarInfo);
        final AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if( state == State.COLLAPSED ) {
                    toolbarInfo.setVisibility(View.INVISIBLE);
                } else {
                    toolbarInfo.setVisibility(View.VISIBLE);
                }
            }
        });

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar( toolbar );

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener( this );

        final CardView toolbarCard = findViewById(R.id.toolbarCard);
        final MainViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(MainViewModel.class);
        viewModel.installUpdateChecker();
        viewModel.loadNextQuiz().observe(this, quiz -> {
            final TextView nextQuizTextView = findViewById(R.id.nextQuizTextView);
            if( quiz != null ) {
                final SimpleDateFormat formatter = new SimpleDateFormat( "dd. MMMM yyyy - HH:mm", Locale.getDefault() );
                final Date quizDate = DateUtils.joomlaDateToJavaDate( quiz.quizDate );
                final String formattedDate = quizDate != null ? formatter.format( quizDate ) : "?";

                nextQuizTextView.setText( getString( R.string.next_quiz, quiz.number, formattedDate) );

                toolbarCard.setOnClickListener(view -> {
                    final Context context = view.getContext();
                    context.startActivity( QuizDetailsActivity.createLaunchIntent( context, quiz.id, quiz.number ) );
                });
            }
            else {
                nextQuizTextView.setText( getString( R.string.next_quiz_unschedule ) );
            }
        });

        viewModel.getNewMessagesCount().observe(this, integer -> {
            if( integer != null && integer > 0 ) {
                BottomNavigationUtils.showOrUpdateBadge( bottomNavigationView, R.id.action_news, integer.toString() );
            } else {
                BottomNavigationUtils.removeBadge( bottomNavigationView, R.id.action_news);
            }
        });

        if( savedInstanceState == null ) {
            setStartFragment();

            toolbarCard.setAlpha( 0f );
            ViewCompat.animate( toolbarCard ).alpha( 1f ).setDuration( 400L ).start();
        }
    }

    private void setStartFragment() {
        final String fragmentTag = getStartFragment();
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        if( FRAGMENT_MESSAGES.equals( fragmentTag ) ) {
            bottomNavigationView.setSelectedItemId( R.id.action_news );
        } else if( FRAGMENT_QUIZZERS.equals( fragmentTag ) ) {
            bottomNavigationView.setSelectedItemId( R.id.action_quizzers );
        } else {
            bottomNavigationView.setSelectedItemId( R.id.action_quizzes );
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

            case R.id.menu_item_about:
                startActivity( AboutActivity.createLaunchIntent( getApplicationContext() ) );
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

        final Fragment currentFragment = getSupportFragmentManager().findFragmentById( R.id.fragmentContainer );
        switch (item.getItemId()) {
            case R.id.action_news:
                if( !( currentFragment instanceof MessagesListFragment ) ) {
                    replacement = MessagesListFragment.newInstance();
                    tag = FRAGMENT_MESSAGES;
                }
                break;

            case R.id.action_quizzes:
                if( !( currentFragment instanceof QuizzesListFragment ) ) {
                    replacement = QuizzesListFragment.newInstance();
                    tag = FRAGMENT_QUIZZES;
                }
                break;

            case R.id.action_quizzers:
                if( !( currentFragment instanceof QuizzersViewPagerFragment) ) {
                    replacement = QuizzersViewPagerFragment.newInstance();
                    tag = FRAGMENT_QUIZZERS;
                }
                break;
        }

        if( replacement != null ) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, replacement, tag).commit();
        }

        return true;
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return dispatchingAndroidInjector;
    }
}
