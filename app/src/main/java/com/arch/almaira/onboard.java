package com.arch.almaira;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class onboard extends BaseActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsContainer;
    private LinearLayout bottomBar;
    private View skipContainer;
    private View btnSkip;
    private MaterialButton btnAction;
    private final View[] dots = new View[3];

    private int currentPage = 0;
    private boolean isExiting = false;
    private ObjectAnimator idleFloatAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboard_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupViewPager();
        setupActions();
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        dotsContainer = findViewById(R.id.dots_container);
        bottomBar = findViewById(R.id.bottom_bar);
        skipContainer = findViewById(R.id.skip_container);
        btnSkip = findViewById(R.id.btn_skip);
        btnAction = findViewById(R.id.btn_action);

        dots[0] = findViewById(R.id.dot_0);
        dots[1] = findViewById(R.id.dot_1);
        dots[2] = findViewById(R.id.dot_2);
    }

    private void setupViewPager() {
        List<OnboardItem> slides = new ArrayList<>();
        slides.add(new OnboardItem(R.layout.slide_onboard_1));
        slides.add(new OnboardItem(R.layout.slide_onboard_2));
        slides.add(new OnboardItem(R.layout.slide_onboard_3));

        OnboardAdapter adapter = new OnboardAdapter(this, slides);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        // Disable overscroll glow for cleaner, softer feel
        View child = viewPager.getChildAt(0);
        if (child instanceof RecyclerView) {
            child.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }

        // Cute multi-layer parallax, tilt & bouncy scale PageTransformer
        final float density = getResources().getDisplayMetrics().density;
        viewPager.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);

            if (absPos >= 1f) {
                page.setAlpha(0f);
                return;
            }

            // Smooth page fade
            page.setAlpha(1f - (absPos * 0.35f));

            View illustration = page.findViewById(R.id.slide_illustration);
            View title = page.findViewById(R.id.slide_title);
            View subtitle = page.findViewById(R.id.slide_subtitle);
            View logo = page.findViewById(R.id.slide_logo);

            int pageWidth = page.getWidth();

            if (illustration != null) {
                // Cute gentle swing/tilt + soft pop-in scale + floating arc
                float scale = 0.80f + (1f - absPos) * 0.20f;
                illustration.setScaleX(scale);
                illustration.setScaleY(scale);
                illustration.setRotation(-position * 8.5f);
                illustration.setTranslationX(-position * pageWidth * 0.18f);
                illustration.setTranslationY(-absPos * 28f * density);
            }

            if (logo != null) {
                float logoScale = 0.85f + (1f - absPos) * 0.15f;
                logo.setScaleX(logoScale);
                logo.setScaleY(logoScale);
                logo.setTranslationX(position * pageWidth * 0.15f);
                logo.setTranslationY(-absPos * 16f * density);
                logo.setAlpha(1f - absPos);
            }

            if (title != null) {
                // Staggered parallax on title
                title.setTranslationX(position * pageWidth * 0.32f);
                title.setTranslationY(absPos * 18f * density);
                title.setAlpha(Math.max(0f, 1f - absPos * 1.3f));
            }

            if (subtitle != null) {
                // Deeper staggered parallax on subtitle so it cascades nicely after title
                subtitle.setTranslationX(position * pageWidth * 0.52f);
                subtitle.setTranslationY(absPos * 28f * density);
                subtitle.setAlpha(Math.max(0f, 1f - absPos * 1.45f));
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                updateIndicators(position);
                updateBottomBar(position);
                playSlideCuteArrivalAnimation(position);
            }
        });

        // Play initial arrival animation once laid out
        viewPager.post(() -> playSlideCuteArrivalAnimation(0));
    }

    /**
     * Plays a cute springy pop + continuous gentle floating bob on the active slide's illustration.
     */
    private void playSlideCuteArrivalAnimation(int position) {
        if (idleFloatAnimator != null) {
            idleFloatAnimator.cancel();
            idleFloatAnimator = null;
        }

        View child = viewPager.getChildAt(0);
        if (!(child instanceof RecyclerView)) return;

        RecyclerView rv = (RecyclerView) child;
        RecyclerView.ViewHolder vh = rv.findViewHolderForAdapterPosition(position);
        if (vh == null || vh.itemView == null) return;

        View illustration = vh.itemView.findViewById(R.id.slide_illustration);
        View title = vh.itemView.findViewById(R.id.slide_title);

        if (title != null) {
            title.animate()
                    .scaleX(1.04f)
                    .scaleY(1.04f)
                    .setDuration(180)
                    .withEndAction(() -> title.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(240)
                            .setInterpolator(new OvershootInterpolator(2.0f))
                            .start())
                    .start();
        }

        if (illustration != null) {
            // Happy little spring bounce on arrival
            PropertyValuesHolder popX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.92f, 1.03f, 1.0f);
            PropertyValuesHolder popY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.92f, 1.03f, 1.0f);
            PropertyValuesHolder wiggle = PropertyValuesHolder.ofFloat(View.ROTATION, -2.5f, 2.0f, 0f);

            ObjectAnimator arrivalPop = ObjectAnimator.ofPropertyValuesHolder(illustration, popX, popY, wiggle);
            arrivalPop.setDuration(520);
            arrivalPop.setInterpolator(new OvershootInterpolator(1.8f));
            arrivalPop.start();

            // Gentle floating idle animation
            float floatOffset = 6f * getResources().getDisplayMetrics().density;
            idleFloatAnimator = ObjectAnimator.ofFloat(illustration, View.TRANSLATION_Y, 0f, -floatOffset);
            idleFloatAnimator.setDuration(1800);
            idleFloatAnimator.setRepeatCount(ValueAnimator.INFINITE);
            idleFloatAnimator.setRepeatMode(ValueAnimator.REVERSE);
            idleFloatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            idleFloatAnimator.start();
        }
    }

    private void setupActions() {
        btnAction.setOnClickListener(v -> {
            // Cute tactile squish-and-pop on button press
            v.animate()
                    .scaleX(0.93f)
                    .scaleY(0.93f)
                    .setDuration(90)
                    .withEndAction(() -> v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(220)
                            .setInterpolator(new OvershootInterpolator(2.5f))
                            .start())
                    .start();

            if (currentPage < 2) {
                viewPager.setCurrentItem(currentPage + 1, true);
            } else {
                finishOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> showSkipConfirmationDialog());
    }

    private void updateIndicators(int activePosition) {
        if (dotsContainer != null) {
            ChangeBounds dotTransition = new ChangeBounds();
            dotTransition.setDuration(320);
            dotTransition.setInterpolator(new OvershootInterpolator(2.4f));
            TransitionManager.beginDelayedTransition(dotsContainer, dotTransition);
        }

        float density = getResources().getDisplayMetrics().density;
        int activeWidth = Math.round(26 * density);
        int inactiveWidth = Math.round(10 * density);
        int height = Math.round(10 * density);

        for (int i = 0; i < dots.length; i++) {
            boolean isActive = (i == activePosition);
            View dot = dots[i];
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
            params.width = isActive ? activeWidth : inactiveWidth;
            params.height = height;
            dot.setLayoutParams(params);
            dot.setBackgroundResource(isActive ? R.drawable.dot_active : R.drawable.dot_inactive);

            if (isActive) {
                dot.setScaleX(0.8f);
                dot.setScaleY(0.8f);
                dot.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(300)
                        .setInterpolator(new OvershootInterpolator(3.0f))
                        .start();
            }
        }
    }

    private void updateBottomBar(int position) {
        TransitionSet transitionSet = new TransitionSet();
        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(340);
        changeBounds.setInterpolator(new OvershootInterpolator(1.4f));
        Fade fade = new Fade();
        fade.setDuration(200);
        transitionSet.addTransition(changeBounds);
        transitionSet.addTransition(fade);
        TransitionManager.beginDelayedTransition(bottomBar, transitionSet);

        LinearLayout.LayoutParams btnParams = (LinearLayout.LayoutParams) btnAction.getLayoutParams();
        float density = getResources().getDisplayMetrics().density;

        if (position == 2) {
            // Slide 3: Hide "Lewati" and expand "Mulai Sekarang ->" to full width with bouncy transition
            skipContainer.setVisibility(View.GONE);
            btnParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            btnAction.setLayoutParams(btnParams);
            btnAction.setText(R.string.get_started_text);
        } else {
            // Slide 1 & 2: Show "Lewati" on the left and "Lanjut ->" pill on the right
            skipContainer.setVisibility(View.VISIBLE);
            btnParams.width = Math.round(205 * density);
            btnAction.setLayoutParams(btnParams);
            btnAction.setText(R.string.next_text);
        }
    }

    private void showSkipConfirmationDialog() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Lewati Pengenalan?")
                .setMessage("Langsung masuk ke ruang kecil kita berdua di almaira?")
                .setNegativeButton("Batal", (d, which) -> d.dismiss())
                .setPositiveButton("Ya, Lewati", (d, which) -> {
                    d.dismiss();
                    finishOnboarding();
                })
                .create();

        dialog.show();

        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    getColor(R.color.warm_rose)
            );
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    getColor(R.color.text_secondary)
            );
        }
    }

    private void finishOnboarding() {
        if (isExiting) return;
        isExiting = true;

        try {
            Class<?> signinClass = Class.forName("com.arch.almaira.signin");
            Intent intent = new Intent(onboard.this, signinClass);
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException ignored) {
            isExiting = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (idleFloatAnimator != null) {
            idleFloatAnimator.cancel();
        }
    }
}
