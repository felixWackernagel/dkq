package de.wackernagel.dkq.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.text.HtmlCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.utils.AppUtils;
import de.wackernagel.dkq.utils.DateUtils;
import de.wackernagel.dkq.viewmodels.MainViewModel;
import de.wackernagel.dkq.webservice.Resource;

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

        final ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeButtonEnabled( true );
        }

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener( this );

        final DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        final NavigationView navigationView = findViewById( R.id.navigationView );
        navigationView.setNavigationItemSelectedListener( new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected( @NonNull final MenuItem menuItem) {
                drawerLayout.setTag(R.id.drawerLayout, menuItem.getItemId());
                drawerLayout.closeDrawers();
                return true;
            }
        });

        final ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle( this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed( drawerView );

                navigationView.setCheckedItem(R.id.drawer_item_main);

                Object itemId = drawerLayout.getTag( R.id.drawerLayout );
                drawerLayout.setTag( R.id.drawerLayout, null );
                if( itemId != null ) {
                    switch ( (int) itemId ) {
                        case R.id.drawer_item_ranking:
                            break;

                        case R.id.drawer_item_about:
                            break;

                        case R.id.drawer_preferences:
                            startActivity( PreferencesActivity.createLaunchIntent( getApplicationContext() ) );
                            break;

                        case R.id.drawer_item_development:
                            startActivity( DevelopmentActivity.createLaunchIntent( getApplicationContext() ) );
                            break;

                        case R.id.drawer_item_main:
                        default:
                            break;
                    }
                }
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        final MainViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(MainViewModel.class);
        viewModel.installUpdateChecker();
        viewModel.isNewAppVersion().observe( this, new Observer<Boolean>() {
            @Override
            public void onChanged( final Boolean newVersion ) {
                if( newVersion != null && newVersion ) {
                    showChangelog();
                }
            }
        } );
        viewModel.loadNextQuiz().observe(this, new Observer<Resource<Quiz>>() {
            @Override
            public void onChanged( final Resource<Quiz> quiz ) {
                if( quiz != null ) {
                    final TextView nextQuizTextView = findViewById(R.id.nextQuizTextView);
                    if( quiz.data != null ) {
                        final SimpleDateFormat formatter = new SimpleDateFormat( "dd. MMMM yyyy - HH:mm", Locale.getDefault() );
                        final Date quizDate = DateUtils.joomlaDateToJavaDate( quiz.data.quizDate );
                        final String formattedDate = quizDate != null ? formatter.format( quizDate ) : "?";

                        nextQuizTextView.setText( getString( R.string.next_quiz, quiz.data.number, formattedDate) );

                        final CardView toolbarCard = findViewById(R.id.toolbarCard);
                        toolbarCard.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Context context = view.getContext();
                                context.startActivity( QuizActivity.createLaunchIntent( context, quiz.data.id, quiz.data.number ) );
                            }
                        });
                    } else {
                        nextQuizTextView.setText( getString( R.string.next_quiz_unschedule ) );
                    }
                }
            }
        });

        if( savedInstanceState == null ) {
            getSupportFragmentManager().beginTransaction()
                .replace( R.id.container, QuizzesListFragment.newInstance(), "quizzes" )
                .commit();
        }
    }

    private void showChangelog() {
        new AlertDialog.Builder( this )
            .setTitle(R.string.changelog_title)
            .setMessage(HtmlCompat.fromHtml( getResources().getString(R.string.changelog_message), HtmlCompat.FROM_HTML_MODE_COMPACT))
            .setCancelable(false)
            .setPositiveButton(R.string.close_word, null)
            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    DkqPreferences.setLastVersionCode( getApplication(), AppUtils.getAppVersion( getApplication() ));
                }
            })
            .create()
            .show();
    }

    @Override
    public void onBackPressed() {
        final DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        if( drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_news:
                getSupportFragmentManager().beginTransaction()
                        .replace( R.id.container, MessagesListFragment.newInstance(), "messages" )
                        .commit();
                return true;

            case R.id.action_quizzes:
                getSupportFragmentManager().beginTransaction()
                        .replace( R.id.container, QuizzesListFragment.newInstance(), "quizzes" )
                        .commit();
                return true;
        }
        return true;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }
}
