package de.wackernagel.dkq.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.AndroidInjection;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.room.entities.Message;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.ui.widgets.IconImageView;
import de.wackernagel.dkq.utils.DateUtils;
import de.wackernagel.dkq.utils.DeviceUtils;
import de.wackernagel.dkq.utils.GlideUtils;
import de.wackernagel.dkq.viewmodels.MessageDetailsViewModel;
import de.wackernagel.dkq.webservice.Resource;

public class MessageDetailsActivity extends AbstractDkqActivity {

    private static final String ARG_MESSAGE_ID = "messageId";
    private static final String ARG_MESSAGE_NUMBER = "messageNumber";

    public static Intent createLaunchIntent(final Context context, final long messageId, final int messageNumber ) {
        final Intent intent = new Intent( context, MessageDetailsActivity.class );
        intent.putExtra( ARG_MESSAGE_ID, messageId );
        intent.putExtra( ARG_MESSAGE_NUMBER, messageNumber );
        return intent;
    }

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private long getMessageId() {
        final Intent intent = getIntent();
        if( intent != null ) {
            return intent.getLongExtra( ARG_MESSAGE_ID, 0 );
        }
        return 0;
    }

    private int getMessageNumber() {
        final Intent intent = getIntent();
        if( intent != null ) {
            return intent.getIntExtra( ARG_MESSAGE_NUMBER, 0 );
        }
        return 0;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar( toolbar );

        final ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle( "" );
        }

        final MessageDetailsViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(MessageDetailsViewModel.class);
        viewModel.loadMessage( getMessageId(), getMessageNumber() ).observe(this, new Observer<Resource<Message>>() {
            @Override
            public void onChanged(@Nullable Resource<Message> resource) {
                if( resource != null ) {
                    if( resource.data != null ) {
                        final Message message = resource.data;
                        bindViews( message );

                        if( message.quizId == null ) {
                            bindQuizButton( null );
                        } else {
                            viewModel.loadQuiz( message.quizId ).observe(MessageDetailsActivity.this, new Observer<Quiz>() {
                                @Override
                                public void onChanged( Quiz quiz ) {
                                    bindQuizButton( quiz );
                                }
                            });
                        }

                        if( message.read == 0 ) {
                            message.read = 1;
                            viewModel.updateMessage( message );
                        }
                    }
                }
            }
        });
    }

    private void bindQuizButton( final Quiz quiz ) {
        final Button quizButton = findViewById(R.id.quizButton);
        if( quiz == null ) {
            quizButton.setVisibility(View.GONE);
            quizButton.setOnClickListener( null );
        } else {
            quizButton.setVisibility(View.VISIBLE);
            quizButton.setText( getString( R.string.open_quiz, quiz.number ));
            quizButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity( QuizDetailsActivity.createLaunchIntent( v.getContext(), quiz.id, quiz.number ) );
                }
            });
        }
    }

    private void bindViews( final Message message ) {
        final IconImageView image = findViewById(R.id.image);
        final TextView lastUpdate = findViewById(R.id.lastUpdate);
        final TextView title = findViewById(R.id.title);
        final TextView content = findViewById(R.id.content);

        GlideUtils.loadImage( image, message.image );
        if( !TextUtils.isEmpty( message.image ) ) {
            final Intent viewImageIntent = new Intent(Intent.ACTION_VIEW);
            viewImageIntent.setDataAndType(Uri.parse(message.image), "image/*");
            if (getPackageManager().queryIntentActivities(viewImageIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.getContext().startActivity(viewImageIntent);
                    }
                });
                image.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        // position toast below image and at the center of the device
                        int x = 0;
                        int y = view.getBottom() + DeviceUtils.dpToPx( 16f, view.getContext() );
                        Toast toast = Toast.makeText( view.getContext(), R.string.maximize_image, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, x, y);
                        toast.show();
                        return true;
                    }
                });
            }
        }

        final Date lastUpdateDate = DateUtils.joomlaDateToJavaDate( message.lastUpdate );
        if( lastUpdateDate != null ) {
            lastUpdate.setText( new SimpleDateFormat( "dd. MMMM yyyy", Locale.getDefault() ).format( lastUpdateDate ) );
        }
        title.setText( message.title );
        content.setMovementMethod(LinkMovementMethod.getInstance() );
        content.setText( HtmlCompat.fromHtml( message.content, HtmlCompat.FROM_HTML_MODE_COMPACT ) );
    }
}
