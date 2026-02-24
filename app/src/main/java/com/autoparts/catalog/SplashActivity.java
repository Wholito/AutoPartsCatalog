package com.autoparts.catalog;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } catch (Exception e) {
                Log.e("SplashActivity", "Failed to start MainActivity", e);
            }
            finish();
        }, SPLASH_DELAY);
    }
}
