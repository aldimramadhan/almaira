package com.arch.almaira.signin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.arch.almaira.R;
import com.arch.almaira.base.BaseActivity;
import com.arch.almaira.main.MainActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class SignInActivity extends BaseActivity {

    private static final String PREF_AUTH = "almaira_auth";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LOGGED_USER = "logged_user";

    // Static credentials
    private static final String USER_1 = "aldimramadhan";
    private static final String USER_2 = "iraa";
    private static final String STATIC_PASSWORD = "110726";

    private MaterialCardView cardInputUsername;
    private MaterialCardView cardInputPassword;
    private EditText etUsername;
    private EditText etPassword;
    private View btnTogglePassword;
    private ImageView ivTogglePassword;

    private View rowRememberMe;
    private ImageView cbRememberMe;
    private View btnSigninMasuk;

    private boolean isPasswordVisible = false;
    private boolean isRememberMeChecked = true;
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences authPrefs = getSharedPreferences(PREF_AUTH, MODE_PRIVATE);
        if (authPrefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            navigateToMain();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signin_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
            windowInsetsController.setAppearanceLightNavigationBars(true);
        }

        initViews();
        setupInteractions();
        loadSavedCredentials();
    }

    private void initViews() {
        cardInputUsername = findViewById(R.id.card_input_username);
        cardInputPassword = findViewById(R.id.card_input_password);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        ivTogglePassword = findViewById(R.id.iv_toggle_password);

        rowRememberMe = findViewById(R.id.row_remember_me);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        btnSigninMasuk = findViewById(R.id.btn_signin_masuk);
    }

    private void setupInteractions() {
        // Subtle border highlight when Username or Password input is focused
        etUsername.setOnFocusChangeListener((v, hasFocus) -> {
            cardInputUsername.setStrokeColor(Color.parseColor(hasFocus ? "#FFD45863" : "#FFF1E5E2"));
            cardInputUsername.setStrokeWidth(dpToPx(hasFocus ? 2 : 1));
        });

        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            cardInputPassword.setStrokeColor(Color.parseColor(hasFocus ? "#FFD45863" : "#FFF1E5E2"));
            cardInputPassword.setStrokeWidth(dpToPx(hasFocus ? 2 : 1));
        });

        // Toggle password visibility
        btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            Typeface currentTypeface = etPassword.getTypeface();
            int selection = etPassword.getSelectionEnd();

            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_signin_eye_on);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_signin_eye_off);
            }

            etPassword.setTypeface(currentTypeface);
            if (selection >= 0) {
                etPassword.setSelection(selection);
            }
        });

        // Toggle "Ingat saya" checkbox
        rowRememberMe.setOnClickListener(v -> {
            isRememberMeChecked = !isRememberMeChecked;
            cbRememberMe.setImageResource(isRememberMeChecked
                    ? R.drawable.ic_checkbox_checked
                    : R.drawable.ic_checkbox_unchecked);
        });

        // Primary Sign-In button ("Masuk ->") with subtle bounce feedback & credential validation
        btnSigninMasuk.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(90)
                    .withEndAction(() -> v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(160)
                            .withEndAction(this::attemptLogin)
                            .start())
                    .start();
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Masukkan username kamu ♡");
            etUsername.requestFocus();
            shakeView(cardInputUsername);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Masukkan password kamu ♡");
            etPassword.requestFocus();
            shakeView(cardInputPassword);
            return;
        }

        boolean isValidUser = username.equalsIgnoreCase(USER_1) || username.equalsIgnoreCase(USER_2);
        boolean isValidPass = password.equals(STATIC_PASSWORD);

        if (isValidUser && isValidPass) {
            String displayName = username.equalsIgnoreCase(USER_1) ? "Aldi" : "Ira";

            // Save login session if Remember Me is checked
            SharedPreferences prefs = getSharedPreferences(PREF_AUTH, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_IS_LOGGED_IN, isRememberMeChecked);
            editor.putString(KEY_LOGGED_USER, username.toLowerCase(Locale.ROOT));
            editor.apply();

            showCustomSnackBar("Selamat datang kembali, " + displayName + " ♡", R.drawable.ic_heart);

            findViewById(R.id.signin_root).postDelayed(this::navigateToMain, 400);
        } else {
            shakeView(cardInputUsername);
            shakeView(cardInputPassword);
            showCustomSnackBar("Username atau password salah ya sayang ♡", R.drawable.ic_heart);
        }
    }

    private void loadSavedCredentials() {
        SharedPreferences prefs = getSharedPreferences(PREF_AUTH, MODE_PRIVATE);
        String savedUser = prefs.getString(KEY_LOGGED_USER, "");
        if (!TextUtils.isEmpty(savedUser)) {
            etUsername.setText(savedUser);
            etPassword.requestFocus();
        }
    }

    private void shakeView(View view) {
        view.animate()
                .translationX(-16f)
                .setDuration(50)
                .withEndAction(() -> view.animate()
                        .translationX(16f)
                        .setDuration(50)
                        .withEndAction(() -> view.animate()
                                .translationX(-8f)
                                .setDuration(50)
                                .withEndAction(() -> view.animate()
                                        .translationX(8f)
                                        .setDuration(50)
                                        .withEndAction(() -> view.animate()
                                                .translationX(0f)
                                                .setDuration(50)
                                                .start())
                                        .start())
                                .start())
                        .start())
                .start();
    }

    private void showCustomSnackBar(String message, int iconRes) {
        View rootView = findViewById(R.id.signin_root);
        Snackbar snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);

        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        layout.setPadding(0, 0, 0, 0);

        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.deep_rose_plum));
        card.setRadius(dpToPx(18));
        card.setCardElevation(dpToPx(6));
        card.setStrokeWidth(0);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));

        if (iconRes != 0) {
            ImageView icon = new ImageView(this);
            icon.setImageResource(iconRes);
            icon.setColorFilter(ContextCompat.getColor(this, R.color.warm_rose));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dpToPx(18), dpToPx(18));
            lp.setMarginEnd(dpToPx(10));
            row.addView(icon, lp);
        }

        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(13f);
        tv.setTypeface(null, Typeface.BOLD);
        row.addView(tv);

        card.addView(row);
        layout.addView(card, 0);
        snackbar.show();
    }

    private void navigateToMain() {
        if (isNavigating) return;
        isNavigating = true;

        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
