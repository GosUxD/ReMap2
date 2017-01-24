package com.example.stefi.remap.view.activities;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stefi.remap.R;


public class SplashActivity extends AppCompatActivity {

    private ImageView Logo;
    private TextView Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);


        Logo = (ImageView) findViewById(R.id.logo_img);
        Text = (TextView) findViewById(R.id.logo_text);

        ObjectAnimator animator3 = ObjectAnimator.ofFloat(Logo, View.TRANSLATION_Y, 70);
        animator3.setInterpolator(new BounceInterpolator());
        animator3.setDuration(2000);

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(Text, View.ALPHA, 1);
        animator2.setDuration(2000);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator3).with(animator2);

        animatorSet.start();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();


    }
}