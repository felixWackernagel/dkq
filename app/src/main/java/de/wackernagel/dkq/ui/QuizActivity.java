package de.wackernagel.dkq.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;

import javax.inject.Inject;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
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
import de.wackernagel.dkq.utils.CalendarBuilder;
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

    private void setFabBehavior( @Nullable final Quiz quiz ) {
        final FloatingActionButton fab = findViewById(R.id.fab);
        if( quiz == null ) {
            fab.hide();
        } else {
            fab.show();
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final BottomSheetDialog modalBottomSheet = new BottomSheetDialog( QuizActivity.this );
                    final View bottomSheetView = getLayoutInflater().inflate( R.layout.bottom_sheet_quiz, null, false );
                    addCalendarAction( quiz, bottomSheetView.findViewById( R.id.calendarAction ) );
                    addMapsAction( quiz, bottomSheetView.findViewById( R.id.mapsAction ) );
                    addShareAction( quiz, bottomSheetView.findViewById( R.id.shareAction ) );
                    modalBottomSheet.setContentView( bottomSheetView );
                    modalBottomSheet.show();
                }
            });
        }
    }

    private void addCalendarAction( @NonNull final Quiz quiz, @Nullable final View view) {
        if( view != null ) {
            final Date startTime = DateUtils.joomlaDateToJavaDate( quiz.quizDate );
            final CalendarBuilder builder = new CalendarBuilder().title( "DKQ #" + quiz.number );

            if( startTime != null ) {
                builder.beginTime(startTime.getTime());
            }

            String location = !TextUtils.isEmpty( quiz.location ) ? quiz.location : null;
            location = !TextUtils.isEmpty( quiz.address ) ? ( location != null ? location + ", " + quiz.address : quiz.address ) : location;
            if( location != null ) {
                builder.location(location);
            }

            final Intent intent = builder.build();
            if( existApplication( intent, getApplicationContext() ) ) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if( startTime == null )
                            Toast.makeText( v.getContext(), R.string.quiz_bottom_sheet_calendar_no_date, Toast.LENGTH_LONG ).show();
                        else
                            startActivity( intent );
                    }
                });
            } else {
                view.setVisibility( View.GONE );
            }
        }
    }

    private void addMapsAction( @NonNull final Quiz quiz, @Nullable final View view ) {
        if( view != null ) {
            final String uriBegin = "geo:" + quiz.latitude + "," + quiz.longitude;
            final String query = quiz.latitude + "," + quiz.longitude + "(" + quiz.location + ")";
            final String encodedQuery = Uri.encode(query);
            final String uriString = uriBegin + "?q=" + encodedQuery;
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse( uriString ) );

            if( existApplication( intent, getApplicationContext() ) ) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if( quiz.latitude == 0d && quiz.longitude == 0d )
                            Toast.makeText( v.getContext(), R.string.quiz_bottom_sheet_maps_no_location, Toast.LENGTH_LONG ).show();
                        else
                            startActivity( intent );
                    }
                });
            } else {
                view.setVisibility( View.GONE );
            }
        }
    }

    private void addShareAction( @NonNull final Quiz quiz, @Nullable final View view) {
        if( view != null ) {
            final Intent intent = ShareCompat.IntentBuilder.from( this )
                    .setType( "text/plain" )
                    .setText( getResources().getString( R.string.quiz_bottom_sheet_share_message,
                            quiz.number,
                            DateUtils.getDateFromJoomlaDate( quiz.quizDate, "??.??.????" ),
                            DateUtils.getTimeFromJoomlaDate( quiz.quizDate, "??:??"),
                            TextUtils.isEmpty( quiz.location ) ? getString( R.string.unknown_word ): quiz.location ) )
                    .setChooserTitle( getResources().getText(R.string.quiz_bottom_sheet_share_chooser ) )
                    .createChooserIntent();

            if( existApplication( intent, getApplicationContext() ) ) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity( intent );
                    }
                });
            } else {
                view.setVisibility( View.GONE );
            }
        }
    }

    public boolean existApplication( final Intent intent, final Context context ) {
        return !( intent == null || context == null ) && context.getPackageManager().queryIntentActivities( intent, PackageManager.MATCH_DEFAULT_ONLY ).size() > 0;
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
        final TextView dateView = findViewById(R.id.quiz_date);
        dateView.setText( DateUtils.getDateFromJoomlaDate( quiz.quizDate, getString(R.string.unknown_word) ) );
    }

    private void setQuizTime( @NonNull final Quiz quiz ) {
        String value = getString(R.string.unknown_word);
        final TextView timeView = findViewById(R.id.quiz_time);
        final String time = DateUtils.getTimeFromJoomlaDate( quiz.quizDate, value );
        if( !value.equals( time ) ) {
            value = getString( R.string.time_format, time );
        }
        timeView.setText( value );
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
