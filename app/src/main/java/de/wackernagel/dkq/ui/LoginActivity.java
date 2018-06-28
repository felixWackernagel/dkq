package de.wackernagel.dkq.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout usernameContainer;
    private TextInputEditText usernameField;
    private TextInputLayout passwordContainer;
    private TextInputEditText passwordField;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameContainer = findViewById(R.id.usernameContainer);
        usernameField = findViewById(R.id.usernameField);
        usernameField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if( !hasFocus ) {
                    isRequired( usernameContainer );
                }
            }
        });
        passwordContainer = findViewById(R.id.passwordContainer);
        passwordField = findViewById(R.id.passwordField);
        passwordField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if( !hasFocus ) {
                    isRequired( passwordContainer );
                }
            }
        });
        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login(null);
                    return true;
                }
                return false;
            }
        });

        if( !DkqPreferences.showLoginScreen( this ) ) {
            openMainActivity();
        }
    }

    public void login( @Nullable final View view ) {
        isRequired( usernameContainer );
        isRequired( passwordContainer );

        final String username = usernameField.getText().toString();
        final String password = passwordField.getText().toString();
        if( TextUtils.equals( username,"dkq" ) && TextUtils.equals( password,"app" ) ) {
            usernameContainer.setError( null );
            passwordContainer.setError( null );
            openMainActivity();
        } else {
            final String loginError = getString( R.string.error_login );
            usernameContainer.setError( loginError );
            passwordContainer.setError( loginError );
        }
    }

    private void isRequired( final TextInputLayout input ) {
        final EditText editText = input.getEditText();
        if( editText == null ) {
            // nothing to validate
            return;
        }

        final String error = getString( R.string.error_required );
        if( TextUtils.isEmpty( editText.getText().toString() ) ) {
            input.setError( error );
        } else if( error.equals( input.getError() ) ) {
            input.setError( null );
        }
    }

    private void openMainActivity() {
        final Intent intent = new Intent( this, MainActivity.class );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity( intent );
        finish();
    }
}
