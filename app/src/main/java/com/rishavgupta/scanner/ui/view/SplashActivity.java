package com.rishavgupta.scanner.ui.view;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.rishavgupta.scanner.R;
import com.rishavgupta.scanner.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding activitySplashBinding;
    private TextView tvSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        init();
        setUpAnimation();
        proceedToMainScreen();
    }


    private void init() {
        tvSplash = activitySplashBinding.tvSplash;
    }

    private void setUpAnimation() {
        Animation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        fadeInAnimation.setDuration(2000);
        tvSplash.startAnimation(fadeInAnimation);
    }

    private void proceedToMainScreen() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, ImageListActivity.class));
            finish();
        }, 2500);
    }
}
