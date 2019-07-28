package com.sk.quantumsudio.projectq.headline.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.sk.quantumsudio.projectq.headline.Activities.MainActivity;
import com.sk.quantumsudio.projectq.headline.R;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private final static int SPLASH_TIME_OUT = 2000;

    TextView title,subtitle,attribute;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Inside of Splash activity onCreate");
        super.onCreate(savedInstanceState);

        title = findViewById(R.id.tv_title);
        subtitle = findViewById(R.id.tv_subtitle);
        attribute = findViewById(R.id.attribute);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        },SPLASH_TIME_OUT);
    }
}
