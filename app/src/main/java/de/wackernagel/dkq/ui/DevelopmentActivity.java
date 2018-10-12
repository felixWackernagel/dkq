package de.wackernagel.dkq.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import javax.inject.Inject;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.AndroidInjection;
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
    }

    public void createSampleMessages(View view) {
        final DevelopmentViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(DevelopmentViewModel.class);
        viewModel.insertMessages(
                SampleCreator.createSampleMessages()
        );
    }

    public void dropSampleMessages(View view) {
        final DevelopmentViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(DevelopmentViewModel.class);
        viewModel.deleteAllMessages();
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
        NotificationReceiver.forNewMessages( this, 1 );
    }

    public void testNotificationManyNewMessages(View view) {
        finish();
        NotificationReceiver.forNewMessages( this, 5 );
    }
}
