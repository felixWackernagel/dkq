package de.wackernagel.dkq.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.AndroidInjection;
import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.receiver.NotificationReceiver;
import de.wackernagel.dkq.room.SampleCreator;
import de.wackernagel.dkq.viewmodels.DevelopmentViewModel;

public class DevelopmentActivity extends AbstractDkqActivity {

    public static Intent createLaunchIntent(final Context context ) {
        return new Intent( context, DevelopmentActivity.class );
    }

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private DevelopmentViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_development);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar( toolbar );

        final ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeButtonEnabled(true);
        }

        final TextView timestamps = findViewById(R.id.timestamps);
        timestamps.setText( getString( R.string.timestamps_text, DkqPreferences.getLastUpdateWorkerExecutionTime( getApplicationContext() ) ) );

        final TextView networkInfo = findViewById( R.id.networkInfo );
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if( netInfo != null ) {
            networkInfo.setText( getString( R.string.network_info_text, netInfo.isConnected(), netInfo.getSubtypeName() + ", " + netInfo.getExtraInfo() ) );
        } else {
            networkInfo.setText( getString( R.string.network_info_text, false, "No NetworkInfo" ) );
        }

        viewModel = ViewModelProviders.of( this, viewModelFactory ).get(DevelopmentViewModel.class);
    }

    public void createSampleMessages(View view) {
        viewModel.insertMessages( SampleCreator.createSampleMessages( null ) );
    }

    public void dropSampleMessages(View view) {
        viewModel.deleteAllMessages();
    }

    public void testNotificationOneNewQuizInline(View view) {
        NotificationReceiver.forNextQuiz( this,1 );
    }

    public void testNotificationOneNewQuiz(View view) {
        finish();
        NotificationReceiver.forNextQuiz( this,1 );
    }

    public void testNotificationManyNewQuizzes(View view) {
        finish();
        NotificationReceiver.forNextQuiz( this, 5 );
    }

    public void testNotificationOneNewMessage(View view) {
        finish();
        NotificationReceiver.forNewMessages( this, 1, "Neues von Foo & Bar" );
    }

    public void testNotificationManyNewMessages(View view) {
        finish();
        NotificationReceiver.forNewMessages( this, 5, null );
    }

    public void insertSampleQuiz( View view ) {
        viewModel.insertQuiz( SampleCreator.createSampleFutureQuiz() );
    }

    public void deleteSampleQuizzes( View view ) {
        viewModel.deleteQuizzesByNumber( new int[]{ SampleCreator.createSampleFutureQuiz().number } );
    }

    public void dropAllAndCreateSamples( View view ) {
        viewModel.dropAllAndCreateSamples();
    }

    public void dropAll( View view ) {
        viewModel.dropAll();
    }
}
