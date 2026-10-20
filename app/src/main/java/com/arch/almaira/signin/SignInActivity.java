package com.arch.almaira.signin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.arch.almaira.R;
import com.arch.almaira.base.BaseActivity;
import com.google.android.material.card.MaterialCardView;

public class SignInActivity extends BaseActivity {

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

        // Primary Sign-In button ("Masuk ->") with subtle bounce feedback
        btnSigninMasuk.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(90)
                    .withEndAction(() -> v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(160)
                            .withEndAction(this::navigateToMain)
                            .start())
                    .start();
        });
    }

    private void navigateToMain() {
        if (isNavigating) return;
        isNavigating = true;

        try {
            Class<?> mainClass = Class.forName("com.arch.almaira.main.MainActivity");
            startActivity(new Intent(SignInActivity.this, mainClass));
            finish();
        } catch (ClassNotFoundException e) {
            try {
                Class<?> legacyMain = Class.forName("com.arch.almaira.MainActivity");
                startActivity(new Intent(SignInActivity.this, legacyMain));
                finish();
            } catch (ClassNotFoundException ignored) {
                isNavigating = false;
            }
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
