package de.wackernagel.dkq.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import de.wackernagel.dkq.DkqConstants;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.utils.AppUtils;

public class AboutActivity extends AbstractDkqActivity {

    public static Intent createLaunchIntent( final Context context ) {
        return new Intent( context, AboutActivity.class );
    }

    @Override
    protected void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_about );

        final Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        final ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setHomeButtonEnabled( true );
        }

        final TextView versionTextView = findViewById( R.id.version );
        versionTextView.setText( getString( R.string.about_version, AppUtils.getVersionName( this ) ) );

        final TextView introTextView = findViewById( R.id.intro );
        introTextView.setMovementMethod( LinkMovementMethod.getInstance() );
        introTextView.setText( HtmlCompat.fromHtml( getString( R.string.about_intro, DkqConstants.EMAIL ), HtmlCompat.FROM_HTML_MODE_COMPACT ) );
    }
}
