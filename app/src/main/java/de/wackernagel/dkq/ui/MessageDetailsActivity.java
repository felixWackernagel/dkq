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

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import de.wackernagel.dkq.DkqLog;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.room.message.Message;
import de.wackernagel.dkq.ui.widgets.IconImageView;
import de.wackernagel.dkq.utils.DateUtils;
import de.wackernagel.dkq.utils.DeviceUtils;
import de.wackernagel.dkq.utils.GlideUtils;
import de.wackernagel.dkq.viewmodels.MessageDetailsViewModel;

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

        final MessageDetailsViewModel viewModel = new ViewModelProvider( this, viewModelFactory ).get(MessageDetailsViewModel.class);
        viewModel.loadMessage( getMessageId(), getMessageNumber() ).observe(this, resource -> {
            if( resource != null ) {
                DkqLog.i("MessageDetailActivity", resource.status + ": " + " Item=" + resource.data + (resource.message != null ? " , " + resource.message : "" )  );
                if( resource.data != null ) {
                    final Message message = resource.data;
                    bindViews( message );

                    if( message.getQuizId() == null ) {
                        bindQuizButton( null );
                    } else {
                        viewModel.loadQuiz( message.getQuizId() ).observe(MessageDetailsActivity.this, this::bindQuizButton);
                    }

                    if( !message.isRead() ) {
                        message.setRead( true );
                        viewModel.updateMessage( message );
                    }
                } else {
                    clearViews();
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
            quizButton.setOnClickListener(v -> v.getContext().startActivity( QuizDetailsActivity.createLaunchIntent( v.getContext(), quiz.id, quiz.number ) ));
        }
    }

    private void bindViews( final Message message ) {
        final IconImageView image = findViewById(R.id.image);
        final TextView lastUpdate = findViewById(R.id.lastUpdate);
        final TextView title = findViewById(R.id.title);
        final TextView content = findViewById(R.id.content);

        final int width = DeviceUtils.getDeviceWidth( this );
        final int height = GlideUtils.fourThreeHeightOf( width );

        GlideUtils.loadImage( image, message.getImage(), message.getVersion(), width, height );
        if( !TextUtils.isEmpty( message.getImage() ) ) {
            final Intent viewImageIntent = new Intent(Intent.ACTION_VIEW);
            viewImageIntent.setDataAndType(Uri.parse(message.getImage()), "image/*");
            if (getPackageManager().queryIntentActivities(viewImageIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
                image.setOnClickListener(v -> v.getContext().startActivity(viewImageIntent));
                image.setOnLongClickListener(view -> {
                    // position toast below image and at the center of the device
                    int x = 0;
                    int y = view.getBottom() + DeviceUtils.dpToPx( 16f, view.getContext() );
                    Toast toast = Toast.makeText( view.getContext(), R.string.maximize_image, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, x, y);
                    toast.show();
                    return true;
                });
            }
        }

        final Date lastUpdateDate = DateUtils.joomlaDateToJavaDate( message.getLastUpdate() );
        if( lastUpdateDate != null ) {
            lastUpdate.setText( new SimpleDateFormat( "dd. MMMM yyyy", Locale.getDefault() ).format( lastUpdateDate ) );
        }
        title.setText( message.getTitle() );
        content.setMovementMethod(LinkMovementMethod.getInstance() );
        content.setText( HtmlCompat.fromHtml( message.getContent(), HtmlCompat.FROM_HTML_MODE_COMPACT ) );
    }

    private void clearViews() {
        final IconImageView image = findViewById(R.id.image);
        final TextView lastUpdate = findViewById(R.id.lastUpdate);
        final TextView title = findViewById(R.id.title);
        final TextView content = findViewById(R.id.content);

        Glide.with( image ).clear( image );
        image.setOnClickListener( null );
        lastUpdate.setText( R.string.unknown_word );
        title.setText( R.string.unknown_word );
        content.setText( R.string.unknown_word );

        bindQuizButton( null );
    }
}
