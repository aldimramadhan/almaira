package com.arch.almaira.main;

import android.app.Dialog;
import android.content.Intent;
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
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.arch.almaira.R;
import com.arch.almaira.base.BaseActivity;
import com.arch.almaira.signin.SignInActivity;
import com.arch.almaira.wishlist.WishlistActivity;
import com.arch.almaira.wishlist.WishlistItem;
import com.arch.almaira.wishlist.WishlistManager;
import android.graphics.Paint;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.google.android.material.carousel.MultiBrowseCarouselStrategy;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    private NestedScrollView scrollView;
    private View topBarContainer;
    private View btnNotification;
    private View btnSettings;
    private View cardHeroSpace;
    private TextView tvDaysCount;
    private TextView tvAnniversaryDate;

    // Catatan Kita Components
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
    private TextView tvWish1, tvWish2, tvWish3;
    private View btnAddWishlist;

    // Kenangan Kita Carousel Components
    private View headerMemoriesCard;
    private View btnAddMemoryHeader;
    private RecyclerView recyclerMemoriesCarousel;
    private MemoriesCarouselAdapter memoriesAdapter;
    private CarouselSnapHelper carouselSnapHelper;
    private int currentCarouselPosition = 0;
    private final List<MemoryItem> memoryList = new ArrayList<>();

    // Bottom Navigation (Qibbly Style Split Floating Bar)
    private MaterialCardView cardNavHome;
    private View navHome;
    private ImageView ivNavHome;
    private View navWishlist, pillNavWishlist;
    private ImageView ivNavWishlist;
    private View navMood, pillNavMood;
    private ImageView ivNavMood;
    private View navMemories, pillNavMemories;
    private ImageView ivNavMemories;
    private View navProfile, pillNavProfile;
    private ImageView ivNavProfile;
    private int currentNavIndex = 0;

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
            v.setPadding(0, 0, 0, 0);

            View statusBarFiller = findViewById(R.id.view_status_bar_filler);
            if (statusBarFiller != null) {
                ViewGroup.LayoutParams lp = statusBarFiller.getLayoutParams();
                lp.height = systemBars.top;
                statusBarFiller.setLayoutParams(lp);
            }

            View topBar = findViewById(R.id.top_bar_container);
            if (topBar != null) {
                topBar.setPadding(0, 0, 0, 0);
            }

            View scrollContent = findViewById(R.id.scroll_content_layout);
            if (scrollContent != null) {
                scrollContent.setPadding(
                        dpToPx(16),
                        systemBars.top + dpToPx(66),
                        dpToPx(16),
                        systemBars.bottom + dpToPx(88)
                );
            }

            View layoutBottomNav = findViewById(R.id.layout_bottom_nav);
            if (layoutBottomNav != null) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) layoutBottomNav.getLayoutParams();
                lp.bottomMargin = systemBars.bottom + dpToPx(14);
                layoutBottomNav.setLayoutParams(lp);
            }

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
        setupMemoriesCarousel();
        loadSharedNotes();
        updateDaysTogether();

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
            tvGreetingTitle.setText("Hai, " + currentUserName);
        }

        if (tvNoteKamuAuthor != null && tvNotePasanganAuthor != null) {
            tvNoteKamuAuthor.setText("Dari Aldi");
            tvNotePasanganAuthor.setText("Dari Ira");
        }

        if (ivNoteKamuAction != null) {
            ivNoteKamuAction.setImageResource(R.drawable.ic_more_vert);
            ivNoteKamuAction.setColorFilter(Color.parseColor("#FFC4A8A5"));
        }
        if (ivNotePasanganAction != null) {
            ivNotePasanganAction.setImageResource(R.drawable.ic_more_vert);
            ivNotePasanganAction.setColorFilter(Color.parseColor("#FFC4A8A5"));
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
        scrollView = findViewById(R.id.scroll_view);
        topBarContainer = findViewById(R.id.top_bar_container);
        tvGreetingTitle = findViewById(R.id.tv_greeting_title);
        btnNotification = findViewById(R.id.btn_notification);
        btnSettings = findViewById(R.id.btn_settings);
        cardHeroSpace = findViewById(R.id.card_hero_space);
        tvDaysCount = findViewById(R.id.tv_days_count);
        tvAnniversaryDate = findViewById(R.id.tv_anniversary_date);

        // Catatan Kita
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
        tvWish1 = findViewById(R.id.tv_wish_1);
        tvWish2 = findViewById(R.id.tv_wish_2);
        tvWish3 = findViewById(R.id.tv_wish_3);
        btnAddWishlist = findViewById(R.id.btn_add_wishlist);

        // Kenangan Kita Carousel
        headerMemoriesCard = findViewById(R.id.header_memories_card);
        btnAddMemoryHeader = findViewById(R.id.btn_add_memory_header);
        recyclerMemoriesCarousel = findViewById(R.id.recycler_memories_carousel);

        // Bottom Navigation (Qibbly Style Split Floating Bar)
        cardNavHome = findViewById(R.id.card_nav_home);
        navHome = findViewById(R.id.nav_home);
        ivNavHome = findViewById(R.id.iv_nav_home);

        navWishlist = findViewById(R.id.nav_wishlist);
        pillNavWishlist = findViewById(R.id.pill_nav_wishlist);
        ivNavWishlist = findViewById(R.id.iv_nav_wishlist);

        navMood = findViewById(R.id.nav_mood);
        pillNavMood = findViewById(R.id.pill_nav_mood);
        ivNavMood = findViewById(R.id.iv_nav_mood);

        navMemories = findViewById(R.id.nav_memories);
        pillNavMemories = findViewById(R.id.pill_nav_memories);
        ivNavMemories = findViewById(R.id.iv_nav_memories);

        navProfile = findViewById(R.id.nav_profile);
        pillNavProfile = findViewById(R.id.pill_nav_profile);
        ivNavProfile = findViewById(R.id.iv_nav_profile);
    }

    private void setupListeners() {
        btnNotification.setOnClickListener(v ->
                showCustomSnackBar("Beranda • Pasanganmu memperbarui Catatan Kita ♡", R.drawable.ic_heart)
        );

        btnSettings.setOnClickListener(v -> showSettingsDialog());

        cardHeroSpace.setOnClickListener(v -> {
            String days = tvDaysCount != null ? tvDaysCount.getText().toString() : "103";
            showCustomSnackBar("Hari ke-" + days + " bersama sejak 11 Juli 2026 ♡", R.drawable.ic_heart);
        });

        // Catatan Kita Listeners
        // Header allows the active user to write their own note
        if (headerNotesCard != null) {
            headerNotesCard.setOnClickListener(v -> showWriteNoteDialog());
        }

        // Aldi's note card ("Dari Aldi"):
        View.OnClickListener aldiNoteClickListener = v -> {
            if (isUserAldi) {
                showWriteNoteDialog();
            } else {
                showReadOnlyNoteDialog("Aldi", tvNoteKamuContent.getText().toString(), tvNoteKamuTime.getText().toString(), R.drawable.avatar_aldi);
            }
        };
        cardNoteKamu.setOnClickListener(aldiNoteClickListener);
        if (ivNoteKamuAction != null) {
            ivNoteKamuAction.setOnClickListener(aldiNoteClickListener);
        }

        // Ira's note card ("Dari Ira"):
        View.OnClickListener iraNoteClickListener = v -> {
            if (!isUserAldi) {
                showWriteNoteDialog();
            } else {
                showReadOnlyNoteDialog("Ira", tvNotePasanganContent.getText().toString(), tvNotePasanganTime.getText().toString(), R.drawable.avatar_ira);
            }
        };
        cardNotePasangan.setOnClickListener(iraNoteClickListener);
        if (ivNotePasanganAction != null) {
            ivNotePasanganAction.setOnClickListener(iraNoteClickListener);
        }

        // Mood Kita Bento Card & Selectors
        headerMoodCard.setOnClickListener(v -> showMoodCheckInDialog(true, false));
        cardMoodKamu.setOnClickListener(v -> showMoodCheckInDialog(true, false));
        cardMoodPasangan.setOnClickListener(v -> showMoodCheckInDialog(false, false));

        // Wishlist Kita Bento Card & Checkboxes
        headerWishlistCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, WishlistActivity.class);
            startActivity(intent);
        });

        itemWish1.setOnClickListener(v -> toggleWishlistItem(0));
        itemWish2.setOnClickListener(v -> toggleWishlistItem(1));
        itemWish3.setOnClickListener(v -> toggleWishlistItem(2));

        if (btnAddWishlist != null) {
            btnAddWishlist.setOnClickListener(v -> {
                Intent intent = new Intent(this, WishlistActivity.class);
                intent.putExtra(WishlistActivity.EXTRA_OPEN_ADD, true);
                startActivity(intent);
            });
        }

        // Kenangan Kita Header & Actions
        if (headerMemoriesCard != null) {
            headerMemoriesCard.setOnClickListener(v ->
                    showCustomSnackBar("Membuka galeri lengkap Kenangan Kita ♡", R.drawable.ic_gallery_pink)
            );
        }

        if (btnAddMemoryHeader != null) {
            btnAddMemoryHeader.setOnClickListener(v -> showAddMemoryDialog());
        }

        // Bottom Navigation (Qibbly Style Split Floating Bar)
        selectNavTab(0);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                selectNavTab(0);
                if (scrollView != null) {
                    scrollView.smoothScrollTo(0, 0);
                }
                showCustomSnackBar("Beranda Ruang Kita ♡", R.drawable.ic_heart);
            });
        }

        if (navWishlist != null) {
            navWishlist.setOnClickListener(v -> {
                selectNavTab(1);
                Intent intent = new Intent(this, WishlistActivity.class);
                startActivity(intent);
            });
        }

        if (navMood != null) {
            navMood.setOnClickListener(v -> {
                selectNavTab(2);
                showMoodCheckInDialog(true, false);
            });
        }

        if (navMemories != null) {
            navMemories.setOnClickListener(v -> {
                selectNavTab(3);
                showCustomSnackBar("Membuka menu Kenangan Kita", R.drawable.ic_gallery_pink);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                selectNavTab(4);
                showCustomSnackBar("Membuka menu Ruang Kita", R.drawable.ic_heart);
            });
        }
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

    private void toggleWishlistItem(int index) {
        List<WishlistItem> items = WishlistManager.getItems(this);
        if (index < items.size()) {
            WishlistItem item = items.get(index);
            boolean newState = !item.isCompleted();
            item.setCompleted(newState);
            if (newState) {
                SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));
                item.setCompletedDate(sdf.format(new Date()));
            } else {
                item.setCompletedDate(null);
            }
            WishlistManager.updateItem(this, item);
            refreshWishlistPreview();
            if (newState) {
                showCustomSnackBar("Impian terwujud: " + item.getTitle() + " ♡ 🎉", R.drawable.ic_heart);
            }
        }
    }

    private void refreshWishlistPreview() {
        List<WishlistItem> items = WishlistManager.getItems(this);
        if (items.size() > 0 && tvWish1 != null && cbWish1 != null) {
            WishlistItem item0 = items.get(0);
            tvWish1.setText(item0.getTitle());
            cbWish1.setImageResource(item0.isCompleted() ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);
            if (item0.isCompleted()) {
                tvWish1.setPaintFlags(tvWish1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvWish1.setTextColor(0xFF9E8C8B);
            } else {
                tvWish1.setPaintFlags(tvWish1.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvWish1.setTextColor(0xFF2B1D1D);
            }
        }
        if (items.size() > 1 && tvWish2 != null && cbWish2 != null) {
            WishlistItem item1 = items.get(1);
            tvWish2.setText(item1.getTitle());
            cbWish2.setImageResource(item1.isCompleted() ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);
            if (item1.isCompleted()) {
                tvWish2.setPaintFlags(tvWish2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvWish2.setTextColor(0xFF9E8C8B);
            } else {
                tvWish2.setPaintFlags(tvWish2.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvWish2.setTextColor(0xFF2B1D1D);
            }
        }
        if (items.size() > 2 && tvWish3 != null && cbWish3 != null) {
            WishlistItem item2 = items.get(2);
            tvWish3.setText(item2.getTitle());
            cbWish3.setImageResource(item2.isCompleted() ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);
            if (item2.isCompleted()) {
                tvWish3.setPaintFlags(tvWish3.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvWish3.setTextColor(0xFF9E8C8B);
            } else {
                tvWish3.setPaintFlags(tvWish3.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvWish3.setTextColor(0xFF2B1D1D);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectNavTab(0);
        refreshWishlistPreview();
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

    /**
     * Calculates the exact days together since 11 Juli 2026 dynamically.
     */
    private void updateDaysTogether() {
        try {
            Calendar startCal = Calendar.getInstance();
            startCal.set(2026, Calendar.JULY, 11, 0, 0, 0);
            startCal.set(Calendar.MILLISECOND, 0);

            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);

            long diffMillis = todayCal.getTimeInMillis() - startCal.getTimeInMillis();
            long daysTogether = (diffMillis / (1000 * 60 * 60 * 24)) + 1; // 11 Juli 2026 is Day 1
            if (daysTogether < 1) {
                daysTogether = 1;
            }

            if (tvDaysCount != null) {
                tvDaysCount.setText(String.valueOf(daysTogether));
            }
            if (tvAnniversaryDate != null) {
                tvAnniversaryDate.setText("11 Juli 2026");
            }
        } catch (Exception e) {
            if (tvDaysCount != null) {
                tvDaysCount.setText("103");
            }
            if (tvAnniversaryDate != null) {
                tvAnniversaryDate.setText("11 Juli 2026");
            }
        }
    }

    private void setupMemoriesCarousel() {
        if (recyclerMemoriesCarousel == null) return;

        CarouselLayoutManager layoutManager = new CarouselLayoutManager(new MultiBrowseCarouselStrategy());
        recyclerMemoriesCarousel.setLayoutManager(layoutManager);

        carouselSnapHelper = new CarouselSnapHelper();
        carouselSnapHelper.attachToRecyclerView(recyclerMemoriesCarousel);

        memoryList.clear();
        memoryList.add(new MemoryItem(1, "Senja Berdua di Pantai", "11 Juli 2026", R.drawable.img_memory_1, false));
        memoryList.add(new MemoryItem(2, "Buket Bunga Favoritmu", "18 Agustus 2026", R.drawable.img_memory_2, false));
        memoryList.add(new MemoryItem(3, "Janji Manis di Bawah Senja", "02 September 2026", R.drawable.img_memory_3, false));
        memoryList.add(new MemoryItem(4, "Momen Hangat Pertama", "28 September 2026", R.drawable.img_hero_couple_sunset, false));
        memoryList.add(new MemoryItem(5, "Rencana Masa Depan Kita", "14 Oktober 2026", R.drawable.img_bg_dreams_quote, false));

        memoriesAdapter = new MemoriesCarouselAdapter(this, memoryList, (item, adapterPos) -> {
            if (adapterPos == currentCarouselPosition) {
                showMemoryPreviewDialog(item);
            } else {
                recyclerMemoriesCarousel.smoothScrollToPosition(adapterPos);
            }
        });

        int startPos = memoriesAdapter.getInitialStartPosition();
        currentCarouselPosition = startPos;
        memoriesAdapter.setActivePosition(startPos);

        recyclerMemoriesCarousel.setAdapter(memoriesAdapter);
        recyclerMemoriesCarousel.scrollToPosition(startPos);

        recyclerMemoriesCarousel.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                checkCarouselSlideTransition();
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    finalizeCarouselSlidePosition();
                }
            }
        });

        recyclerMemoriesCarousel.post(this::finalizeCarouselSlidePosition);
    }

    private void checkCarouselSlideTransition() {
        if (recyclerMemoriesCarousel == null) return;
        RecyclerView.LayoutManager lm = recyclerMemoriesCarousel.getLayoutManager();
        if (lm == null) return;

        int childCount = recyclerMemoriesCarousel.getChildCount();
        if (childCount == 0) return;

        int startPadding = recyclerMemoriesCarousel.getPaddingStart();

        View closestChild = null;
        int minDistance = Integer.MAX_VALUE;
        int closestPos = -1;

        for (int i = 0; i < childCount; i++) {
            View child = recyclerMemoriesCarousel.getChildAt(i);
            if (child == null) continue;
            int dist = Math.abs(child.getLeft() - startPadding);
            if (dist < minDistance) {
                minDistance = dist;
                closestChild = child;
                closestPos = recyclerMemoriesCarousel.getChildAdapterPosition(child);
            }
        }

        if (closestPos != -1 && closestPos != currentCarouselPosition) {
            int oldPos = currentCarouselPosition;
            currentCarouselPosition = closestPos;

            if (memoriesAdapter != null) {
                memoriesAdapter.setActivePosition(closestPos);
            }

            for (int i = 0; i < childCount; i++) {
                View child = recyclerMemoriesCarousel.getChildAt(i);
                if (child == null) continue;
                int pos = recyclerMemoriesCarousel.getChildAdapterPosition(child);
                View overlay = child.findViewById(R.id.layout_memory_text_overlay);
                if (overlay == null) continue;

                if (pos == closestPos) {
                    // Slide paling kiri yang baru: muncul dengan animasi fade
                    overlay.animate().cancel();
                    overlay.setAlpha(0f);
                    overlay.setVisibility(View.VISIBLE);
                    overlay.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();
                } else if (pos == oldPos) {
                    // Slide sebelumnya: animasi fade-out
                    overlay.animate().cancel();
                    overlay.animate()
                            .alpha(0f)
                            .setDuration(180)
                            .withEndAction(() -> overlay.setVisibility(View.INVISIBLE))
                            .start();
                } else {
                    // Slide tengah dan kanan: tetap tersembunyi
                    overlay.animate().cancel();
                    overlay.setAlpha(0f);
                    overlay.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void finalizeCarouselSlidePosition() {
        if (recyclerMemoriesCarousel == null) return;
        RecyclerView.LayoutManager lm = recyclerMemoriesCarousel.getLayoutManager();
        if (lm == null) return;

        View snapView = carouselSnapHelper != null ? carouselSnapHelper.findSnapView(lm) : null;
        int snapPos = snapView != null ? recyclerMemoriesCarousel.getChildAdapterPosition(snapView) : RecyclerView.NO_POSITION;
        if (snapPos == RecyclerView.NO_POSITION) {
            int startPadding = recyclerMemoriesCarousel.getPaddingStart();
            int minDistance = Integer.MAX_VALUE;
            int count = recyclerMemoriesCarousel.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = recyclerMemoriesCarousel.getChildAt(i);
                if (child == null) continue;
                int dist = Math.abs(child.getLeft() - startPadding);
                if (dist < minDistance) {
                    minDistance = dist;
                    snapPos = recyclerMemoriesCarousel.getChildAdapterPosition(child);
                }
            }
        }

        if (snapPos != RecyclerView.NO_POSITION) {
            currentCarouselPosition = snapPos;
            if (memoriesAdapter != null) {
                memoriesAdapter.setActivePosition(snapPos);
            }
        }

        int childCount = recyclerMemoriesCarousel.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = recyclerMemoriesCarousel.getChildAt(i);
            if (child == null) continue;
            int pos = recyclerMemoriesCarousel.getChildAdapterPosition(child);
            View overlay = child.findViewById(R.id.layout_memory_text_overlay);
            if (overlay == null) continue;

            if (pos == currentCarouselPosition) {
                overlay.animate().cancel();
                if (overlay.getVisibility() != View.VISIBLE || overlay.getAlpha() < 1f) {
                    overlay.setVisibility(View.VISIBLE);
                    overlay.animate().alpha(1f).setDuration(220).start();
                }
            } else {
                overlay.animate().cancel();
                overlay.setAlpha(0f);
                overlay.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void showMemoryPreviewDialog(MemoryItem item) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_memory_preview);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageView ivImage = dialog.findViewById(R.id.iv_preview_image);
        TextView tvTitle = dialog.findViewById(R.id.tv_preview_title);
        TextView tvDate = dialog.findViewById(R.id.tv_preview_date);
        View btnClose = dialog.findViewById(R.id.btn_close_preview);
        View btnActionDismiss = dialog.findViewById(R.id.btn_action_dismiss);

        if (ivImage != null && item.getImageRes() != 0) {
            ivImage.setImageResource(item.getImageRes());
        }
        if (tvTitle != null) {
            tvTitle.setText(item.getTitle());
        }
        if (tvDate != null) {
            tvDate.setText(item.getDate());
        }

        View.OnClickListener dismissListener = v -> dialog.dismiss();
        if (btnClose != null) btnClose.setOnClickListener(dismissListener);
        if (btnActionDismiss != null) btnActionDismiss.setOnClickListener(dismissListener);

        dialog.show();
    }

    private void showAddMemoryDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_memory);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etTitle = dialog.findViewById(R.id.et_memory_title);
        EditText etDate = dialog.findViewById(R.id.et_memory_date);
        View btnCancel = dialog.findViewById(R.id.btn_cancel_add_memory);
        View btnSave = dialog.findViewById(R.id.btn_save_memory);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        String defaultDate = sdf.format(new Date());
        if (etDate != null) {
            etDate.setText(defaultDate);
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String title = etTitle != null ? etTitle.getText().toString().trim() : "";
                String date = etDate != null ? etDate.getText().toString().trim() : "";

                if (TextUtils.isEmpty(title)) {
                    if (etTitle != null) etTitle.setError("Beri judul kenangan terlebih dahulu ♡");
                    return;
                }
                if (TextUtils.isEmpty(date)) {
                    date = defaultDate;
                }

                // Pick rotating stock photos for new memories
                int[] photoChoices = new int[]{
                        R.drawable.img_memory_1,
                        R.drawable.img_memory_2,
                        R.drawable.img_memory_3,
                        R.drawable.img_hero_couple_sunset
                };
                int chosenPhoto = photoChoices[(memoryList.size()) % photoChoices.length];

                // Insert right before the last "Tambah Kenangan" card
                int insertPos = Math.max(0, memoryList.size() - 1);
                MemoryItem newMemory = new MemoryItem(memoryList.size() + 1, title, date, chosenPhoto, false);
                memoryList.add(insertPos, newMemory);

                if (memoriesAdapter != null) {
                    memoriesAdapter.notifyItemInserted(insertPos);
                }

                if (recyclerMemoriesCarousel != null) {
                    recyclerMemoriesCarousel.smoothScrollToPosition(insertPos);
                }

                dialog.dismiss();
                showCustomSnackBar("Kenangan baru berhasil disimpan ♡", R.drawable.ic_heart);
            });
        }

        dialog.show();
    }

    private void selectNavTab(int index) {
        currentNavIndex = index;

        int inactiveColor = Color.parseColor("#FF9E8C8B");
        int activeColor = Color.parseColor("#FFD45863");

        // Update secondary left tabs
        if (pillNavWishlist != null) pillNavWishlist.setBackgroundResource(index == 1 ? R.drawable.bg_nav_pill_active : 0);
        if (ivNavWishlist != null) ivNavWishlist.setColorFilter(index == 1 ? activeColor : inactiveColor);

        if (pillNavMood != null) pillNavMood.setBackgroundResource(index == 2 ? R.drawable.bg_nav_pill_active : 0);
        if (ivNavMood != null) ivNavMood.setColorFilter(index == 2 ? activeColor : inactiveColor);

        if (pillNavMemories != null) pillNavMemories.setBackgroundResource(index == 3 ? R.drawable.bg_nav_pill_active : 0);
        if (ivNavMemories != null) ivNavMemories.setColorFilter(index == 3 ? activeColor : inactiveColor);

        if (pillNavProfile != null) pillNavProfile.setBackgroundResource(index == 4 ? R.drawable.bg_nav_pill_active : 0);
        if (ivNavProfile != null) ivNavProfile.setColorFilter(index == 4 ? activeColor : inactiveColor);

        // Update primary right Home button
        boolean isHome = (index == 0);
        if (cardNavHome != null) {
            cardNavHome.setStrokeWidth(0);
            cardNavHome.setOutlineSpotShadowColor(Color.parseColor(isHome ? "#66D45863" : "#26463032"));
        }
        if (navHome != null) {
            navHome.setBackgroundResource(isHome ? R.drawable.bg_home_btn_gradient : R.drawable.bg_home_btn_inactive);
        }
        if (ivNavHome != null) {
            ivNavHome.setColorFilter(isHome ? Color.WHITE : inactiveColor);
        }
    }

    private void showSettingsDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Pengaturan Ruang Kita ♡")
                .setMessage("Masuk sebagai: " + currentUserName + "\nIngin keluar dari akun?")
                .setPositiveButton("Keluar (Logout)", (d, which) -> {
                    SharedPreferences authPrefs = getSharedPreferences("almaira_auth", MODE_PRIVATE);
                    authPrefs.edit().putBoolean("is_logged_in", false).apply();

                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private int dpToPx(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
