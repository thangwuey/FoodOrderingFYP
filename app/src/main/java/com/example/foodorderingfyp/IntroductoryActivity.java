package com.example.foodorderingfyp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class IntroductoryActivity extends AppCompatActivity {

    ImageView ivLogo, ivAppName, ivSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introductory);

        ivLogo = findViewById(R.id.intro_logo);
        ivAppName = findViewById(R.id.intro_app_name);
        ivSplash = findViewById(R.id.intro_bg);

        /*ivSplash.animate().translationY(-1600).setDuration(1000).setStartDelay(4000);
        ivLogo.animate().translationY(1400).setDuration(1000).setStartDelay(4000);
        ivAppName.animate().translationY(1400).setDuration(1000).setStartDelay(4000);*/

        splash();
    }

    private void splash() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(IntroductoryActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);
    }

    // EXIT applications
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String tag = intent.getStringExtra("EXIT_TAG");
        if (tag != null && !TextUtils.isEmpty(tag)) {
            if ("SINGLETASK".equals(tag)) {
                finish();
            }
        }
    }
}