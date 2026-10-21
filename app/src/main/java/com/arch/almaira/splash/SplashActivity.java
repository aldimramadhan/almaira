package com.arch.almaira.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.arch.almaira.R;
import com.arch.almaira.main.MainActivity;
import com.arch.almaira.onboard.OnboardActivity;

/**
 * Splash screen activity matching the timing and animations from Neummio,
 * with the circle settling and opening exactly at the vertical and horizontal center.
 *
 * Timeline (Total 10.50s):
 * - 0.00s to 1.50s: Blank pure black screen (#000000)
 * - 1.50s to 3.10s: Circle drop, bounce, and settle directly at center (0, 0)
 * - 3.10s to 5.10s: Circle scale-up animation expanding symmetrically from center
 * - 3.50s to 5.50s: Central Logo Fade In (2s) at exact center
 * - 5.50s to 8.50s: Central Logo Standby (3s)
 * - 8.50s to 10.50s: Central Logo Fade Out (2s)
 * - 10.50s: Smooth fade transition (900ms) to OnboardActivity
 */
public class SplashActivity extends AppCompatActivity {

    private View splashRoot;
    private View splashCircle;
    private ImageView splashLogo;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AnimatorSet masterAnimatorSet = new AnimatorSet();
    private Runnable transitionRunnable;
    private boolean isTransitioned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Immediately dismiss Android 12+ native splash screen to prevent double splash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSplashScreen().setOnExitAnimationListener(splashScreenView -> splashScreenView.remove());
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splashscreen);

        splashRoot = findViewById(R.id.splash_root);
        splashCircle = findViewById(R.id.splash_circle);
        splashLogo = findViewById(R.id.splash_logo);

        // Ensure circle starts completely hidden for the initial 1.50s blank black screen
        splashCircle.setVisibility(View.INVISIBLE);
        splashLogo.setAlpha(0.0f);

        // Wait for layout to measure screen dimensions accurately
        splashRoot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                splashRoot.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                startSplashAnimation();
            }
        });
    }

    private void startSplashAnimation() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        final float screenHeight = splashRoot.getHeight() > 0 ? splashRoot.getHeight() : getResources().getDisplayMetrics().heightPixels;
        final float screenWidth = splashRoot.getWidth() > 0 ? splashRoot.getWidth() : getResources().getDisplayMetrics().widthPixels;

        // 1. Central Logo Sizing: 1/6 of screen height
        final int logoSize = Math.round(screenHeight / 6.0f);
        ViewGroup.LayoutParams logoParams = splashLogo.getLayoutParams();
        if (logoParams != null) {
            logoParams.width = logoSize;
            logoParams.height = logoSize;
            splashLogo.setLayoutParams(logoParams);
        }

        // Adjust circle base size proportionally (~120-140dp)
        final float density = getResources().getDisplayMetrics().density;
        float baseCircleSize = Math.max(120f * density, screenWidth - (260f * density));
        ViewGroup.LayoutParams circleParams = splashCircle.getLayoutParams();
        if (circleParams != null) {
            circleParams.width = (int) baseCircleSize;
            circleParams.height = (int) baseCircleSize;
            splashCircle.setLayoutParams(circleParams);
        }

        // Explicitly set pivots to the exact center of each view
        splashCircle.setPivotX(baseCircleSize / 2f);
        splashCircle.setPivotY(baseCircleSize / 2f);
        splashLogo.setPivotX(logoSize / 2f);
        splashLogo.setPivotY(logoSize / 2f);

        // 2. Prepare Circle initial transform:
        // Starts completely off-screen at the top
        splashCircle.setTranslationX(0.0f);
        splashCircle.setTranslationY(-0.6f * screenHeight);
        splashCircle.setScaleX(1.0f);
        splashCircle.setScaleY(1.0f);

        AccelerateDecelerateInterpolator easeInOut = new AccelerateDecelerateInterpolator();

        // -------------------------------------------------------------
        // ANIMATION TIMELINE:
        // -------------------------------------------------------------

        // --- STEP 1: Circle Drop, Bounce, and Settle directly at center (0.0f) ---
        // Step 1a: Drop from top to overshoot center
        // Delay: 1500ms, Duration: 500ms (1500ms - 2000ms)
        ObjectAnimator circleDrop = ObjectAnimator.ofFloat(splashCircle, View.TRANSLATION_Y, -0.6f * screenHeight, 0.12f * screenHeight);
        circleDrop.setStartDelay(1500);
        circleDrop.setDuration(500);
        circleDrop.setInterpolator(easeInOut);
        circleDrop.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                splashCircle.setVisibility(View.VISIBLE);
            }
        });

        // Step 1b: Bounce up slightly above center
        // Delay: 2000ms, Duration: 450ms (2000ms - 2450ms)
        ObjectAnimator circleBounce = ObjectAnimator.ofFloat(splashCircle, View.TRANSLATION_Y, 0.12f * screenHeight, -0.06f * screenHeight);
        circleBounce.setStartDelay(2000);
        circleBounce.setDuration(450);
        circleBounce.setInterpolator(easeInOut);

        // Step 1c: Settle directly to EXACT center (0.0f)
        // Delay: 2450ms, Duration: 450ms (2450ms - 2900ms)
        ObjectAnimator circleSettle = ObjectAnimator.ofFloat(splashCircle, View.TRANSLATION_Y, -0.06f * screenHeight, 0.0f);
        circleSettle.setStartDelay(2450);
        circleSettle.setDuration(450);
        circleSettle.setInterpolator(easeInOut);

        // --- STEP 2: Circle Scale Up (1.0 -> 35.0) from exact center (0, 0) ---
        // Delay: 3100ms, Duration: 2000ms (3100ms - 5100ms)
        ObjectAnimator circleScaleX = ObjectAnimator.ofFloat(splashCircle, View.SCALE_X, 1.0f, 35.0f);
        circleScaleX.setStartDelay(3100);
        circleScaleX.setDuration(2000);
        circleScaleX.setInterpolator(easeInOut);

        ObjectAnimator circleScaleY = ObjectAnimator.ofFloat(splashCircle, View.SCALE_Y, 1.0f, 35.0f);
        circleScaleY.setStartDelay(3100);
        circleScaleY.setDuration(2000);
        circleScaleY.setInterpolator(easeInOut);

        // --- STEP 3: Central Logo (Fade In -> Standby -> Fade Out) at exact center ---
        // Step 3a: Fade In (0.0 -> 1.0)
        // Delay: 3500ms, Duration: 2000ms (3500ms - 5500ms)
        ObjectAnimator logoFadeIn = ObjectAnimator.ofFloat(splashLogo, View.ALPHA, 0.0f, 1.0f);
        logoFadeIn.setStartDelay(3500);
        logoFadeIn.setDuration(2000);
        logoFadeIn.setInterpolator(easeInOut);

        // Step 3b: Fade Out (1.0 -> 0.0) after 3.0s standby
        // Delay: 8500ms (3500 + 2000 + 3000), Duration: 2000ms (8500ms - 10500ms)
        ObjectAnimator logoFadeOut = ObjectAnimator.ofFloat(splashLogo, View.ALPHA, 1.0f, 0.0f);
        logoFadeOut.setStartDelay(8500);
        logoFadeOut.setDuration(2000);
        logoFadeOut.setInterpolator(easeInOut);

        // Play all animators together with their precise delay offsets
        masterAnimatorSet.playTogether(
                circleDrop,
                circleBounce,
                circleSettle,
                circleScaleX,
                circleScaleY,
                logoFadeIn,
                logoFadeOut
        );

        masterAnimatorSet.start();

        // --- STEP 4: Transition to onboard at 10.50s (10500ms) ---
        transitionRunnable = this::navigateToOnboard;
        mainHandler.postDelayed(transitionRunnable, 10500);
    }

    private void navigateToOnboard() {
        if (isTransitioned || isFinishing() || isDestroyed()) {
            return;
        }
        isTransitioned = true;

        SharedPreferences authPrefs = getSharedPreferences("almaira_auth", MODE_PRIVATE);
        boolean isLoggedIn = authPrefs.getBoolean("is_logged_in", false);

        Intent intent;
        if (isLoggedIn) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, OnboardActivity.class);
        }
        startActivity(intent);

        // 900ms smooth fade transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.fade_in_900, R.anim.fade_out_900);
        } else {
            overridePendingTransition(R.anim.fade_in_900, R.anim.fade_out_900);
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (transitionRunnable != null) {
            mainHandler.removeCallbacks(transitionRunnable);
        }
        if (masterAnimatorSet.isRunning()) {
            masterAnimatorSet.cancel();
        }
    }
}
