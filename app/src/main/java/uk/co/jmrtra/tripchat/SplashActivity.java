package uk.co.jmrtra.tripchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class SplashActivity extends AppCompatActivity {

    private Button mFacebookBtn;
    private Button mGoogleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mFacebookBtn = (Button) findViewById(R.id.splash_button_facebook);
        mGoogleBtn = (Button) findViewById(R.id.splash_button_google);

        mFacebookBtn.setOnClickListener(mLoginClickListener);
        mGoogleBtn.setOnClickListener(mLoginClickListener);
    }

    View.OnClickListener mLoginClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.splash_button_facebook) {
                loginFacebook();
            } else {
                loginGoogle();
            }
        }
    };

    private void loginFacebook() {
        //TODO: Replace with real login mechanism
        startActivity(new Intent(this, MainActivity.class));
    }

    private void loginGoogle() {
        //TODO: Replace with real login mechanism
        startActivity(new Intent(this, MainActivity.class));
    }

}