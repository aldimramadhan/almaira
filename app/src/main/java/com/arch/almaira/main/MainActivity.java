package com.arch.almaira.main;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.arch.almaira.R;
import com.arch.almaira.base.BaseActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private static final String KEY_HAS_SELECTED_INITIAL_MOOD = "has_selected_initial_mood";
    private static final String KEY_SELECTED_MOOD_KAMU = "selected_mood_kamu";
    private static final String KEY_SELECTED_MOOD_PASANGAN = "selected_mood_pasangan";

    private static final String PREF_SHARED_NOTES = "almaira_shared_notes";
    private static final String PREF_KEY_NOTE_KAMU = "note_kamu";
    private static final String PREF_KEY_NOTE_KAMU_TIME = "note_kamu_time";
    private static final String PREF_KEY_NOTE_PASANGAN = "note_pasangan";
    private static final String PREF_KEY_NOTE_PASANGAN_TIME = "note_pasangan_time";

    private TextView tvGreetingTitle;
    private View btnNotification;
    private View btnSettings;
    private View cardHeroSpace;

    // Catatan Kita Components
    private View cardSharedNotes;
    private View headerNotesCard;
    private View cardNoteKamu;
    private View cardNotePasangan;
    private TextView tvNoteKamuAuthor;
    private TextView tvNotePasanganAuthor;
    private TextView tvNoteKamuContent;
    private TextView tvNoteKamuTime;
    private TextView tvNotePasanganContent;
    private TextView tvNotePasanganTime;
    private ImageView ivNoteKamuAction;
    private ImageView ivNotePasanganAction;

    // Mood Kita Components
    private View headerMoodCard;
    private View cardMoodKamu;
    private View cardMoodPasangan;
    private TextView tvMoodKamu;
    private TextView tvMoodPasangan;

    // Wishlist Kita Components
    private View headerWishlistCard;
    private LinearLayout itemWish1, itemWish2, itemWish3;
    private ImageView cbWish1, cbWish2, cbWish3;

    // Kenangan Kita Components
    private View headerMemoriesCard;
    private View cardMemory1, cardMemory2, cardMemory3;
    private View cardAddMemory;

    // Bottom Navigation
    private View navHome;
    private View navWishlist;
    private View navMood;
    private View navMemories;
    private View navProfile;

    private String currentUserName = "Aldi";
    private String partnerName = "Ira";
    private boolean isUserAldi = true;

    private boolean hasSelectedInitialMood = false;
    private int currentMoodKamuIndex = 4; // Default "Kangen"
    private int currentMoodPasanganIndex = 3; // Default "Semangat"

    private final boolean[] wishChecked = new boolean[]{true, false, false};

    private final String[] moodLabels = new String[]{
            "Senang",
            "Sedih",
            "Kesel",
            "Semangat",
            "Kangen",
            "Sayang",
            "Tenang",
            "Capek",
            "Biasa Aja"
    };

    private final String[] moodEmojis = new String[]{
            "😊",
            "😢",
            "😤",
            "🤩",
            "🥺",
            "🥰",
            "😌",
            "🫠",
            "😐"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root), (v, insets) -> {
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
        initAuthUser();
        setupListeners();
        loadSharedNotes();

        if (savedInstanceState != null) {
            hasSelectedInitialMood = savedInstanceState.getBoolean(KEY_HAS_SELECTED_INITIAL_MOOD, false);
            currentMoodKamuIndex = savedInstanceState.getInt(KEY_SELECTED_MOOD_KAMU, 4);
            currentMoodPasanganIndex = savedInstanceState.getInt(KEY_SELECTED_MOOD_PASANGAN, 3);
            if (currentMoodKamuIndex >= 0 && currentMoodKamuIndex < moodLabels.length) {
                applyMoodUpdate(true, currentMoodKamuIndex, false);
            }
            if (currentMoodPasanganIndex >= 0 && currentMoodPasanganIndex < moodLabels.length) {
                applyMoodUpdate(false, currentMoodPasanganIndex, false);
            }
        } else {
            // Apply default visually: Kangen & Semangat
            applyMoodUpdate(true, 4, false);
            applyMoodUpdate(false, 3, false);
        }

        // Show Mood Check-In Dialog when entering MainActivity for the first time
        if (!hasSelectedInitialMood) {
            findViewById(R.id.main_root).post(() -> showMoodCheckInDialog(true, true));
        }
    }

    private void initAuthUser() {
        SharedPreferences authPrefs = getSharedPreferences("almaira_auth", MODE_PRIVATE);
        String loggedUser = authPrefs.getString("logged_user", "aldimramadhan");
        if ("iraa".equalsIgnoreCase(loggedUser)) {
            currentUserName = "Ira";
            partnerName = "Aldi";
            isUserAldi = false;
        } else {
            currentUserName = "Aldi";
            partnerName = "Ira";
            isUserAldi = true;
        }

        if (tvGreetingTitle != null) {
            tvGreetingTitle.setText("Hai, Aldi & Ira");
        }

        if (tvNoteKamuAuthor != null && tvNotePasanganAuthor != null) {
            tvNoteKamuAuthor.setText("Dari Aldi");
            tvNotePasanganAuthor.setText("Dari Ira");
        }

        // Visual indicator: pencil for own note, lock for partner's note
        if (ivNoteKamuAction != null && ivNotePasanganAction != null) {
            if (isUserAldi) {
                ivNoteKamuAction.setImageResource(R.drawable.ic_edit_pencil);
                ivNoteKamuAction.setColorFilter(Color.parseColor("#FFC95B63"));
                ivNotePasanganAction.setImageResource(R.drawable.ic_signin_lock);
                ivNotePasanganAction.setColorFilter(Color.parseColor("#FF9E6D38"));
            } else {
                ivNoteKamuAction.setImageResource(R.drawable.ic_signin_lock);
                ivNoteKamuAction.setColorFilter(Color.parseColor("#FF9E6D38"));
                ivNotePasanganAction.setImageResource(R.drawable.ic_edit_pencil);
                ivNotePasanganAction.setColorFilter(Color.parseColor("#FFC95B63"));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_HAS_SELECTED_INITIAL_MOOD, hasSelectedInitialMood);
        outState.putInt(KEY_SELECTED_MOOD_KAMU, currentMoodKamuIndex);
        outState.putInt(KEY_SELECTED_MOOD_PASANGAN, currentMoodPasanganIndex);
    }

    private void initViews() {
        tvGreetingTitle = findViewById(R.id.tv_greeting_title);
        btnNotification = findViewById(R.id.btn_notification);
        btnSettings = findViewById(R.id.btn_settings);
        cardHeroSpace = findViewById(R.id.card_hero_space);

        // Catatan Kita
        cardSharedNotes = findViewById(R.id.card_shared_notes);
        headerNotesCard = findViewById(R.id.header_notes_card);
        cardNoteKamu = findViewById(R.id.card_note_kamu);
        cardNotePasangan = findViewById(R.id.card_note_pasangan);
        tvNoteKamuAuthor = findViewById(R.id.tv_note_kamu_author);
        tvNotePasanganAuthor = findViewById(R.id.tv_note_pasangan_author);
        tvNoteKamuContent = findViewById(R.id.tv_note_kamu_content);
        tvNoteKamuTime = findViewById(R.id.tv_note_kamu_time);
        tvNotePasanganContent = findViewById(R.id.tv_note_pasangan_content);
        tvNotePasanganTime = findViewById(R.id.tv_note_pasangan_time);
        ivNoteKamuAction = findViewById(R.id.iv_note_kamu_action);
        ivNotePasanganAction = findViewById(R.id.iv_note_pasangan_action);

        // Mood Kita
        headerMoodCard = findViewById(R.id.header_mood_card);
        cardMoodKamu = findViewById(R.id.card_mood_kamu);
        cardMoodPasangan = findViewById(R.id.card_mood_pasangan);
        tvMoodKamu = findViewById(R.id.tv_mood_kamu);
        tvMoodPasangan = findViewById(R.id.tv_mood_pasangan);

        // Wishlist Kita
        headerWishlistCard = findViewById(R.id.header_wishlist_card);
        itemWish1 = findViewById(R.id.item_wish_1);
        itemWish2 = findViewById(R.id.item_wish_2);
        itemWish3 = findViewById(R.id.item_wish_3);
        cbWish1 = findViewById(R.id.cb_wish_1);
        cbWish2 = findViewById(R.id.cb_wish_2);
        cbWish3 = findViewById(R.id.cb_wish_3);

        // Kenangan Kita
        headerMemoriesCard = findViewById(R.id.header_memories_card);
        cardMemory1 = findViewById(R.id.card_memory_1);
        cardMemory2 = findViewById(R.id.card_memory_2);
        cardMemory3 = findViewById(R.id.card_memory_3);
        cardAddMemory = findViewById(R.id.card_add_memory);

        // Bottom Navigation
        navHome = findViewById(R.id.nav_home);
        navWishlist = findViewById(R.id.nav_wishlist);
        navMood = findViewById(R.id.nav_mood);
        navMemories = findViewById(R.id.nav_memories);
        navProfile = findViewById(R.id.nav_profile);
    }

    private void setupListeners() {
        btnNotification.setOnClickListener(v ->
                showCustomSnackBar("Beranda • Pasanganmu memperbarui Catatan Kita ♡", R.drawable.ic_heart)
        );

        btnSettings.setOnClickListener(v ->
                showCustomSnackBar("Pengaturan Ruang Privat almaira ♡", R.drawable.ic_heart)
        );

        cardHeroSpace.setOnClickListener(v ->
                showCustomSnackBar("Hari ke-468 bersama ♡", R.drawable.ic_heart)
        );

        // Catatan Kita Listeners
        // Header allows the active user to write their own note
        if (headerNotesCard != null) {
            headerNotesCard.setOnClickListener(v -> showWriteNoteDialog());
        }

        // Aldi's note card ("Dari Aldi"):
        // If user is Aldi -> opens editor
        // If user is Ira -> opens read-only viewer (cannot edit Aldi's note)
        cardNoteKamu.setOnClickListener(v -> {
            if (isUserAldi) {
                showWriteNoteDialog();
            } else {
                showReadOnlyNoteDialog("Aldi", tvNoteKamuContent.getText().toString(), tvNoteKamuTime.getText().toString(), R.drawable.img_mood_kamu);
            }
        });

        // Ira's note card ("Dari Ira"):
        // If user is Ira -> opens editor
        // If user is Aldi -> opens read-only viewer (cannot edit Ira's note)
        cardNotePasangan.setOnClickListener(v -> {
            if (!isUserAldi) {
                showWriteNoteDialog();
            } else {
                showReadOnlyNoteDialog("Ira", tvNotePasanganContent.getText().toString(), tvNotePasanganTime.getText().toString(), R.drawable.img_mood_pasangan);
            }
        });

        // Mood Kita Bento Card & Selectors
        headerMoodCard.setOnClickListener(v -> showMoodCheckInDialog(true, false));
        cardMoodKamu.setOnClickListener(v -> showMoodCheckInDialog(true, false));
        cardMoodPasangan.setOnClickListener(v -> showMoodCheckInDialog(false, false));

        // Wishlist Kita Bento Card & Checkboxes
        headerWishlistCard.setOnClickListener(v ->
                showCustomSnackBar("Membuka daftar lengkap Wishlist Kita", R.drawable.ic_wishlist_bag_pink)
        );

        itemWish1.setOnClickListener(v -> toggleWishlistItem(0, cbWish1));
        itemWish2.setOnClickListener(v -> toggleWishlistItem(1, cbWish2));
        itemWish3.setOnClickListener(v -> toggleWishlistItem(2, cbWish3));

        // Kenangan Kita Full-Width Card
        headerMemoriesCard.setOnClickListener(v ->
                showCustomSnackBar("Membuka galeri lengkap Kenangan Kita", R.drawable.ic_gallery_pink)
        );

        cardMemory1.setOnClickListener(v ->
                showCustomSnackBar("Kenangan: Menikmati senja berdua di pantai ♡", R.drawable.ic_gallery_pink)
        );

        cardMemory2.setOnClickListener(v ->
                showCustomSnackBar("Kenangan: Buket bunga tulip spesial ♡", R.drawable.ic_heart)
        );

        cardMemory3.setOnClickListener(v ->
                showCustomSnackBar("Kenangan: Janji hati di bawah senja ♡", R.drawable.ic_heart)
        );

        if (cardAddMemory != null) {
            cardAddMemory.setOnClickListener(v ->
                    showCustomSnackBar("Tambah kenangan foto baru bersama ♡", R.drawable.ic_add)
            );
        }

        // Bottom Navigation
        navHome.setOnClickListener(v -> {
            // Already on Beranda
        });

        navWishlist.setOnClickListener(v ->
                showCustomSnackBar("Membuka menu Wishlist Kita", R.drawable.ic_heart)
        );

        navMood.setOnClickListener(v ->
                showMoodCheckInDialog(true, false)
        );

        navMemories.setOnClickListener(v ->
                showCustomSnackBar("Membuka menu Kenangan Kita", R.drawable.ic_gallery_pink)
        );

        navProfile.setOnClickListener(v ->
                showCustomSnackBar("Membuka menu Ruang Kita", R.drawable.ic_heart)
        );
    }

    /**
     * Loads the shared love notes written by both partners from SharedPreferences.
     */
    private void loadSharedNotes() {
        SharedPreferences prefs = getSharedPreferences(PREF_SHARED_NOTES, MODE_PRIVATE);

        String defaultNoteKamu = "✨ Bangga selalu sama kamu, terima kasih untuk hari ini ya ♡";
        String defaultTimeKamu = "Hari ini, 15:51";

        String defaultNotePasangan = "Jangan lupa minum air yang banyak dan kabari aku ya ♡";
        String defaultTimePasangan = "Hari ini, 08:15";

        String noteKamu = prefs.getString(PREF_KEY_NOTE_KAMU, defaultNoteKamu);
        String timeKamu = prefs.getString(PREF_KEY_NOTE_KAMU_TIME, defaultTimeKamu);

        String notePasangan = prefs.getString(PREF_KEY_NOTE_PASANGAN, defaultNotePasangan);
        String timePasangan = prefs.getString(PREF_KEY_NOTE_PASANGAN_TIME, defaultTimePasangan);

        tvNoteKamuContent.setText(noteKamu);
        tvNoteKamuTime.setText(timeKamu);

        tvNotePasanganContent.setText(notePasangan);
        tvNotePasanganTime.setText(timePasangan);
    }

    /**
     * Opens the Love Note Writing Dialog strictly for the currently logged-in user.
     * Aldi can only edit Aldi's note, and Ira can only edit Ira's note.
     */
    private void showWriteNoteDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_write_note);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.55f);
        }

        TextView tvDialogTitle = dialog.findViewById(R.id.tv_dialog_write_title);
        TextView tvDialogSubtitle = dialog.findViewById(R.id.tv_dialog_note_subtitle);
        EditText etNoteInput = dialog.findViewById(R.id.et_note_input);
        View btnSaveNote = dialog.findViewById(R.id.btn_save_note);

        TextView chip1 = dialog.findViewById(R.id.chip_quick_1);
        TextView chip2 = dialog.findViewById(R.id.chip_quick_2);
        TextView chip3 = dialog.findViewById(R.id.chip_quick_3);
        TextView chip4 = dialog.findViewById(R.id.chip_quick_4);

        if (tvDialogTitle != null) {
            tvDialogTitle.setText("Tulis Catatan " + currentUserName + " ♡");
        }
        if (tvDialogSubtitle != null) {
            tvDialogSubtitle.setText("Tinggalkan pesan manis dari " + currentUserName + " untuk " + partnerName + " hari ini.");
        }

        // Pre-fill strictly with current user's existing note
        String currentContent = isUserAldi ? tvNoteKamuContent.getText().toString() : tvNotePasanganContent.getText().toString();
        etNoteInput.setText(currentContent);
        etNoteInput.setSelection(etNoteInput.getText().length());

        // Quick Preset Chips
        View.OnClickListener chipClickListener = v -> {
            if (v instanceof TextView) {
                String presetText = ((TextView) v).getText().toString();
                etNoteInput.setText(presetText);
                etNoteInput.setSelection(presetText.length());
            }
        };

        if (chip1 != null) chip1.setOnClickListener(chipClickListener);
        if (chip2 != null) chip2.setOnClickListener(chipClickListener);
        if (chip3 != null) chip3.setOnClickListener(chipClickListener);
        if (chip4 != null) chip4.setOnClickListener(chipClickListener);

        btnSaveNote.setOnClickListener(v -> {
            String noteText = etNoteInput.getText().toString().trim();
            if (TextUtils.isEmpty(noteText)) {
                etNoteInput.setError("Tulis pesanmu terlebih dahulu ya ♡");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String timeFormatted = "Hari ini, " + sdf.format(new Date());

            SharedPreferences.Editor editor = getSharedPreferences(PREF_SHARED_NOTES, MODE_PRIVATE).edit();

            if (isUserAldi) {
                // Strictly updates Aldi's note
                editor.putString(PREF_KEY_NOTE_KAMU, noteText);
                editor.putString(PREF_KEY_NOTE_KAMU_TIME, timeFormatted);
                editor.apply();

                tvNoteKamuContent.setText(noteText);
                tvNoteKamuTime.setText(timeFormatted);

                cardNoteKamu.setScaleX(0.95f);
                cardNoteKamu.setScaleY(0.95f);
                cardNoteKamu.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(280)
                        .setInterpolator(new OvershootInterpolator(2.0f))
                        .start();
            } else {
                // Strictly updates Ira's note
                editor.putString(PREF_KEY_NOTE_PASANGAN, noteText);
                editor.putString(PREF_KEY_NOTE_PASANGAN_TIME, timeFormatted);
                editor.apply();

                tvNotePasanganContent.setText(noteText);
                tvNotePasanganTime.setText(timeFormatted);

                cardNotePasangan.setScaleX(0.95f);
                cardNotePasangan.setScaleY(0.95f);
                cardNotePasangan.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(280)
                        .setInterpolator(new OvershootInterpolator(2.0f))
                        .start();
            }

            showCustomSnackBar("Pesan dari " + currentUserName + " berhasil ditempel di Catatan Kita ♡", R.drawable.ic_bookmark_heart);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Opens the read-only dialog when a user taps on their partner's note.
     * Prevents editing and provides clear feedback that only the partner can modify it.
     */
    private void showReadOnlyNoteDialog(String authorName, String content, String time, int avatarRes) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_read_note);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.55f);
        }

        ImageView ivAvatar = dialog.findViewById(R.id.iv_read_author_avatar);
        TextView tvAuthorTitle = dialog.findViewById(R.id.tv_read_author_title);
        TextView tvNoteTime = dialog.findViewById(R.id.tv_read_note_time);
        TextView tvNoteContent = dialog.findViewById(R.id.tv_read_note_content);
        TextView tvSecurityHint = dialog.findViewById(R.id.tv_read_security_hint);
        View btnReply = dialog.findViewById(R.id.btn_reply_note);
        TextView tvBtnReply = dialog.findViewById(R.id.tv_btn_reply_text);
        View btnClose = dialog.findViewById(R.id.btn_close_read_dialog);

        if (ivAvatar != null) ivAvatar.setImageResource(avatarRes);
        if (tvAuthorTitle != null) tvAuthorTitle.setText("Catatan dari " + authorName + " ♡");
        if (tvNoteTime != null) tvNoteTime.setText(time);
        if (tvNoteContent != null) tvNoteContent.setText(content);
        if (tvSecurityHint != null) {
            tvSecurityHint.setText("Catatan ini hanya dapat diubah oleh " + authorName + ". Kamu bisa membalasnya dengan catatanmu sendiri ♡");
        }
        if (tvBtnReply != null) {
            tvBtnReply.setText("Balas dengan Catatan " + currentUserName);
        }

        if (btnReply != null) {
            btnReply.setOnClickListener(v -> {
                dialog.dismiss();
                showWriteNoteDialog();
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    /**
     * Displays the card-based Mood Check-In Dialog.
     */
    private void showMoodCheckInDialog(boolean isKamu, boolean mandatoryFirst) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_mood_check_in);
        dialog.setCancelable(!mandatoryFirst);
        dialog.setCanceledOnTouchOutside(!mandatoryFirst);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.52f);
        }

        TextView tvTitle = dialog.findViewById(R.id.tv_dialog_mood_title);
        TextView tvSubtitle = dialog.findViewById(R.id.tv_dialog_mood_subtitle);
        MaterialCardView btnConfirm = dialog.findViewById(R.id.btn_confirm_mood);
        TextView tvBtnConfirm = dialog.findViewById(R.id.tv_btn_confirm_mood);
        View gridMoodCards = dialog.findViewById(R.id.grid_mood_cards);

        if (!isKamu) {
            tvTitle.setText("Bagaimana mood\npasanganmu hari ini? ♡");
            tvSubtitle.setText("Pilih suasana hati pasanganmu di ruang kita berdua.");
        }

        int[] cardIds = new int[]{
                R.id.card_mood_opt_0,
                R.id.card_mood_opt_1,
                R.id.card_mood_opt_2,
                R.id.card_mood_opt_3,
                R.id.card_mood_opt_4,
                R.id.card_mood_opt_5,
                R.id.card_mood_opt_6,
                R.id.card_mood_opt_7,
                R.id.card_mood_opt_8
        };

        MaterialCardView[] cards = new MaterialCardView[cardIds.length];
        for (int i = 0; i < cardIds.length; i++) {
            cards[i] = dialog.findViewById(cardIds[i]);
        }

        final int[] selectedIndex = new int[]{
                isKamu ? currentMoodKamuIndex : currentMoodPasanganIndex
        };

        // Helper to refresh visual highlight of all 9 mood cards
        Runnable refreshSelectionUI = () -> {
            for (int i = 0; i < cards.length; i++) {
                boolean isSelected = (i == selectedIndex[0]);
                if (isSelected) {
                    cards[i].setCardBackgroundColor(Color.parseColor("#FFFCECE9"));
                    cards[i].setStrokeColor(Color.parseColor("#FFD45863"));
                    cards[i].setStrokeWidth(dpToPx(2));
                } else {
                    cards[i].setCardBackgroundColor(Color.WHITE);
                    cards[i].setStrokeColor(Color.parseColor("#FFF1E3DF"));
                    cards[i].setStrokeWidth(dpToPx(1));
                }
            }

            if (selectedIndex[0] >= 0 && selectedIndex[0] < moodLabels.length) {
                btnConfirm.setCardBackgroundColor(Color.parseColor("#FFD45863"));
                btnConfirm.setAlpha(1.0f);
                tvBtnConfirm.setText("Simpan Mood: " + moodEmojis[selectedIndex[0]] + " " + moodLabels[selectedIndex[0]] + " ♡");
            } else {
                btnConfirm.setCardBackgroundColor(Color.parseColor("#FFE29CA2"));
                btnConfirm.setAlpha(0.85f);
                tvBtnConfirm.setText("Pilih Mood Terlebih Dahulu");
            }
        };

        refreshSelectionUI.run();

        for (int i = 0; i < cards.length; i++) {
            final int index = i;
            cards[i].setOnClickListener(v -> {
                selectedIndex[0] = index;
                refreshSelectionUI.run();

                v.setScaleX(0.92f);
                v.setScaleY(0.92f);
                v.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(220)
                        .setInterpolator(new OvershootInterpolator(2.0f))
                        .start();
            });
        }

        btnConfirm.setOnClickListener(v -> {
            if (selectedIndex[0] < 0) {
                gridMoodCards.animate()
                        .scaleX(1.03f)
                        .scaleY(1.03f)
                        .setDuration(110)
                        .withEndAction(() -> gridMoodCards.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(110)
                                .start())
                        .start();
                return;
            }

            hasSelectedInitialMood = true;
            applyMoodUpdate(isKamu, selectedIndex[0], true);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void applyMoodUpdate(boolean isKamu, int index, boolean showFeedback) {
        String label = moodLabels[index];
        String emoji = moodEmojis[index];

        if (isKamu) {
            currentMoodKamuIndex = index;
            if (tvMoodKamu != null) {
                tvMoodKamu.setText(label);
            }
            if (showFeedback) {
                showCustomSnackBar("Mood kamu hari ini: " + emoji + " " + label + " ♡", R.drawable.ic_heart);
            }
        } else {
            currentMoodPasanganIndex = index;
            if (tvMoodPasangan != null) {
                tvMoodPasangan.setText(label);
            }
            if (showFeedback) {
                showCustomSnackBar("Mood pasanganmu: " + emoji + " " + label + " ♡", R.drawable.ic_heart);
            }
        }
    }

    private void toggleWishlistItem(int index, ImageView checkbox) {
        wishChecked[index] = !wishChecked[index];
        checkbox.setImageResource(wishChecked[index]
                ? R.drawable.ic_checkbox_checked
                : R.drawable.ic_checkbox_unchecked);
    }

    private void showCustomSnackBar(String message, int iconRes) {
        View rootView = findViewById(R.id.main_root);
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

    private int dpToPx(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
