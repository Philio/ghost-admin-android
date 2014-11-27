package me.philio.ghostadmin.ui;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.Timer;
import java.util.TimerTask;

import me.philio.ghostadmin.R;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashActivity extends ActionBarActivity {

    private Timer mTimer;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Window background transition
        final TransitionDrawable transition = (TransitionDrawable) getResources()
                .getDrawable(R.drawable.transition_fade_in);
        getWindow().setBackgroundDrawable(transition);

        // Title alpha fade in
        final Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.alpha_fade_in);

        // Logo zoom / fly in
        Animation scale = AnimationUtils.loadAnimation(this, R.anim.scale_zoom_in);
        scale.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Kick off the other animations
                transition.startTransition(250);
                View title = findViewById(R.id.txt_title);
                title.startAnimation(fadeIn);
                title.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        findViewById(R.id.img_ghost).startAnimation(scale);

        // Schedule a timer to check for a valid account
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // TODO
            }
        }, 1500);
    }

    @Override
    protected void onStop() {
        // Stop any running timer
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }

        // Finish the activity
        finish();

        super.onStop();
    }

}
