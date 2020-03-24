package com.axionteq.onlineradio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.axionteq.onlineradio.radio.radio.RadioActivity;

public class LauncherActivity extends AppCompatActivity {

    private final static int SPLASH_TIME_OUT = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_launcher );
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

        new Handler().postDelayed( new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getSharedPreferences( "user_data", MODE_PRIVATE );
                if (sharedPreferences.getBoolean( "loggedIn", false )) {
                    startActivity( new Intent( LauncherActivity.this, RadioActivity.class ) );
                    finish();
                } else {
                    startActivity(new Intent(LauncherActivity.this, RadioActivity.class));
                    finish();
                }
            }
        }, SPLASH_TIME_OUT );
    }
}
