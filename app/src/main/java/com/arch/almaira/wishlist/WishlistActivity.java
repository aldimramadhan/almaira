package com.arch.almaira.wishlist;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arch.almaira.R;
import com.arch.almaira.base.BaseActivity;
import com.arch.almaira.main.MainActivity;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class WishlistActivity extends BaseActivity implements WishlistAdapter.OnWishlistActionListener {

    public static final String EXTRA_OPEN_ADD = "extra_open_add";

    private NestedScrollView scrollView;
    private RecyclerView recyclerWishlist;
    private WishlistAdapter adapter;
    private View layoutEmptyState;
    private TextView tvSectionTitle, tvItemsCount;

    // Bento Progress Views
    private TextView tvProgressStatus, tvProgressPercentage;
    private ProgressBar progressBarWishlist;
    private TextView tvStatTotal, tvStatCompleted, tvStatPending;

    // Status Filter Tabs (Semua, Belum Selesai, Selesai)
    private TextView tabStatusAll, tabStatusPending, tabStatusCompleted;
    private TextView[] statusTabs;

    // Category Filter Chips
    private TextView chipCatAll, chipCatTravel, chipCatDate, chipCatCulinary, chipCatGoods, chipCatOther;
    private TextView[] categoryChips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wishlist);

        setupWindowInsets();
        initViews();
        setupRecyclerView();
        setupFilters();
        setupListeners();

        loadWishlistData();

        // If launched with request to open add modal directly
        if (getIntent().getBooleanExtra(EXTRA_OPEN_ADD, false)) {
            showAddWishlistDialog();
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.wishlist_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            View statusBarFiller = findViewById(R.id.view_status_bar_filler);
            if (statusBarFiller != null) {
                ViewGroup.LayoutParams lp = statusBarFiller.getLayoutParams();
                lp.height = systemBars.top;
                statusBarFiller.setLayoutParams(lp);
            }

            View scrollContent = findViewById(R.id.wishlist_content_layout);
            if (scrollContent != null) {
                scrollContent.setPadding(
                        dpToPx(18),
                        systemBars.top + dpToPx(66),
                        dpToPx(18),
                        systemBars.bottom + dpToPx(90)
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
    }

    private void initViews() {
        scrollView = findViewById(R.id.scroll_view_wishlist);
        recyclerWishlist = findViewById(R.id.recycler_wishlist);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        tvSectionTitle = findViewById(R.id.tv_section_title);
        tvItemsCount = findViewById(R.id.tv_items_count);

        tvProgressStatus = findViewById(R.id.tv_progress_status);
        tvProgressPercentage = findViewById(R.id.tv_progress_percentage);
        progressBarWishlist = findViewById(R.id.progress_bar_wishlist);
        tvStatTotal = findViewById(R.id.tv_stat_total);
        tvStatCompleted = findViewById(R.id.tv_stat_completed);
        tvStatPending = findViewById(R.id.tv_stat_pending);

        // Status Tabs (Terpisah dari Kategori)
        tabStatusAll = findViewById(R.id.tab_status_all);
        tabStatusPending = findViewById(R.id.tab_status_pending);
        tabStatusCompleted = findViewById(R.id.tab_status_completed);
        statusTabs = new TextView[]{tabStatusAll, tabStatusPending, tabStatusCompleted};

        // Category Filter Chips
        chipCatAll = findViewById(R.id.chip_cat_all);
        chipCatTravel = findViewById(R.id.chip_cat_travel);
        chipCatDate = findViewById(R.id.chip_cat_date);
        chipCatCulinary = findViewById(R.id.chip_cat_culinary);
        chipCatGoods = findViewById(R.id.chip_cat_goods);
        chipCatOther = findViewById(R.id.chip_cat_other);
        categoryChips = new TextView[]{chipCatAll, chipCatTravel, chipCatDate, chipCatCulinary, chipCatGoods, chipCatOther};
    }

    private void setupRecyclerView() {
        adapter = new WishlistAdapter(this);
        recyclerWishlist.setLayoutManager(new LinearLayoutManager(this));
        recyclerWishlist.setAdapter(adapter);
    }

    private void setupFilters() {
        // Status Filter Tabs
        if (tabStatusAll != null) {
            tabStatusAll.setOnClickListener(v -> selectStatusTab(tabStatusAll, "ALL"));
        }
        if (tabStatusPending != null) {
            tabStatusPending.setOnClickListener(v -> selectStatusTab(tabStatusPending, "PENDING"));
        }
        if (tabStatusCompleted != null) {
            tabStatusCompleted.setOnClickListener(v -> selectStatusTab(tabStatusCompleted, "COMPLETED"));
        }

        // Category Filter Chips
        if (chipCatAll != null) {
            chipCatAll.setOnClickListener(v -> selectCategoryChip(chipCatAll, "ALL"));
        }
        if (chipCatTravel != null) {
            chipCatTravel.setOnClickListener(v -> selectCategoryChip(chipCatTravel, "Travel"));
        }
        if (chipCatDate != null) {
            chipCatDate.setOnClickListener(v -> selectCategoryChip(chipCatDate, "Kencan"));
        }
        if (chipCatCulinary != null) {
            chipCatCulinary.setOnClickListener(v -> selectCategoryChip(chipCatCulinary, "Kuliner"));
        }
        if (chipCatGoods != null) {
            chipCatGoods.setOnClickListener(v -> selectCategoryChip(chipCatGoods, "Barang"));
        }
        if (chipCatOther != null) {
            chipCatOther.setOnClickListener(v -> selectCategoryChip(chipCatOther, "Lainnya"));
        }
    }

    private void selectStatusTab(TextView selectedTab, String statusKey) {
        for (TextView tab : statusTabs) {
            if (tab == null) continue;
            if (tab == selectedTab) {
                tab.setBackgroundResource(R.drawable.bg_status_tab_active);
                tab.setTextColor(0xFFD45863);
                tab.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tab.setBackgroundColor(Color.TRANSPARENT);
                tab.setTextColor(0xFF786564);
                tab.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
        adapter.setStatusFilter(statusKey);
        updateEmptyStateAndCount();
    }

    private void selectCategoryChip(TextView selectedChip, String categoryKey) {
        for (TextView chip : categoryChips) {
            if (chip == null) continue;
            if (chip == selectedChip) {
                chip.setBackgroundResource(R.drawable.bg_filter_chip_active);
                chip.setTextColor(Color.WHITE);
                chip.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                chip.setBackgroundResource(R.drawable.bg_filter_chip_inactive);
                chip.setTextColor(0xFF786564);
                chip.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
        adapter.setCategoryFilter(categoryKey);
        updateEmptyStateAndCount();
    }

    private void setupListeners() {
        View btnAddTop = findViewById(R.id.btn_add_wishlist_top);
        View cardAddTop = findViewById(R.id.card_add_wishlist);
        View.OnClickListener openAddListener = v -> showAddWishlistDialog();
        if (btnAddTop != null) btnAddTop.setOnClickListener(openAddListener);
        if (cardAddTop != null) cardAddTop.setOnClickListener(openAddListener);

        View btnEmptyAdd = findViewById(R.id.btn_empty_add);
        if (btnEmptyAdd != null) {
            btnEmptyAdd.setOnClickListener(openAddListener);
        }

        // Bottom Navigation Listeners
        View navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        View navWishlist = findViewById(R.id.nav_wishlist);
        if (navWishlist != null) {
            navWishlist.setOnClickListener(v -> {
                if (scrollView != null) {
                    scrollView.smoothScrollTo(0, 0);
                }
            });
        }

        View navMood = findViewById(R.id.nav_mood);
        if (navMood != null) {
            navMood.setOnClickListener(v -> Toast.makeText(this, "Fitur Mood Kita ♡", Toast.LENGTH_SHORT).show());
        }

        View navMemories = findViewById(R.id.nav_memories);
        if (navMemories != null) {
            navMemories.setOnClickListener(v -> finish());
        }

        View navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> Toast.makeText(this, "Ruang Kita ♡", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadWishlistData() {
        List<WishlistItem> items = WishlistManager.getItems(this);
        adapter.setItems(items);
        updateProgressSummary();
        updateEmptyStateAndCount();
    }

    private void updateProgressSummary() {
        int total = adapter.getTotalCount();
        int completed = adapter.getCompletedCount();
        int pending = total - completed;
        int percentage = total > 0 ? (int) Math.round(((double) completed / total) * 100) : 0;

        progressBarWishlist.setProgress(percentage);
        tvProgressPercentage.setText(percentage + "%");
        tvProgressStatus.setText(completed + " dari " + total + " impian telah terwujud ♡");

        tvStatTotal.setText("Total: " + total);
        tvStatCompleted.setText("✓ Tercapai: " + completed);
        tvStatPending.setText("• Belum: " + pending);
    }

    private void updateEmptyStateAndCount() {
        int filteredCount = adapter.getFilteredCount();
        tvItemsCount.setText(filteredCount + " impian");

        if (filteredCount == 0) {
            recyclerWishlist.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerWishlist.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onToggleCompleted(WishlistItem item, int position) {
        boolean newState = !item.isCompleted();
        item.setCompleted(newState);
        if (newState) {
            SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));
            item.setCompletedDate(sdf.format(new Date()));
        } else {
            item.setCompletedDate(null);
        }

        WishlistManager.updateItem(this, item);
        adapter.notifyDataSetChanged();
        updateProgressSummary();
        updateEmptyStateAndCount();

        if (newState) {
            Toast.makeText(this, "Selamat! Satu impian kalian telah terwujud ♡ 🎉", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteItem(WishlistItem item, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Impian?")
                .setMessage("Yakin ingin menghapus \"" + item.getTitle() + "\" dari wishlist kalian?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    WishlistManager.deleteItem(this, item.getId());
                    loadWishlistData();
                    Toast.makeText(this, "Impian berhasil dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showAddWishlistDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_wishlist);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        EditText etTitle = dialog.findViewById(R.id.et_wish_title);
        View btnCancel = dialog.findViewById(R.id.btn_cancel_add);
        View btnSave = dialog.findViewById(R.id.btn_save_wish);

        // 2. Select Kategori (Cutout Label)
        View boxSelectCategory = dialog.findViewById(R.id.box_select_category);
        TextView tvSelectedCategory = dialog.findViewById(R.id.tv_selected_category);
        final String[] selectedCategory = {"Travel"};

        if (boxSelectCategory != null) {
            boxSelectCategory.setOnClickListener(v -> {
                showSelectCategoryDialog(tvSelectedCategory, selectedCategory);
            });
        }

        // 3. Select Target Waktu (Cutout Label)
        View cardSelectTargetDate = dialog.findViewById(R.id.card_select_target_date);
        TextView tvSelectedTargetDate = dialog.findViewById(R.id.tv_selected_target_date);
        View btnClearTargetDate = dialog.findViewById(R.id.btn_clear_target_date);
        View ivDropdownDate = dialog.findViewById(R.id.iv_dropdown_date);

        final String[] selectedTargetDate = {""};

        if (cardSelectTargetDate != null) {
            cardSelectTargetDate.setOnClickListener(v -> {
                showProgressiveDatePickerDialog(tvSelectedTargetDate, btnClearTargetDate, ivDropdownDate, selectedTargetDate);
            });
        }

        if (btnClearTargetDate != null) {
            btnClearTargetDate.setOnClickListener(v -> {
                selectedTargetDate[0] = "";
                tvSelectedTargetDate.setText("");
                tvSelectedTargetDate.setHint("Pilih waktu...");
                tvSelectedTargetDate.setTypeface(null, android.graphics.Typeface.NORMAL);
                btnClearTargetDate.setVisibility(View.GONE);
                if (ivDropdownDate != null) ivDropdownDate.setVisibility(View.VISIBLE);
            });
        }

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                if (title.isEmpty()) {
                    etTitle.setError("Silakan tulis impian kalian");
                    return;
                }

                String targetDate = selectedTargetDate[0];
                WishlistItem newItem = new WishlistItem(title, selectedCategory[0], "Aldi & Ira", targetDate);
                WishlistManager.addItem(this, newItem);

                loadWishlistData();
                dialog.dismiss();
                Toast.makeText(this, "Impian baru berhasil ditambahkan! ✨", Toast.LENGTH_SHORT).show();
            });
        }

        dialog.show();
    }

    /**
     * Dialog Pemilihan Kategori Impian
     */
    private void showSelectCategoryDialog(TextView tvSelectedCategory, String[] selectedCategory) {
        Dialog catDialog = new Dialog(this);
        catDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        catDialog.setContentView(R.layout.dialog_select_category);

        if (catDialog.getWindow() != null) {
            catDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            catDialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            catDialog.getWindow().setGravity(Gravity.CENTER);
        }

        LinearLayout layoutOptions = catDialog.findViewById(R.id.layout_category_options);
        View btnCancel = catDialog.findViewById(R.id.btn_cancel_category);

        String[] categories = {"✈️ Travel & Trip", "🌸 Kencan", "🧋 Kuliner", "🎁 Barang Impian", "✨ Lainnya"};
        String[] categoryKeys = {"Travel", "Kencan", "Kuliner", "Barang", "Lainnya"};

        for (int i = 0; i < categories.length; i++) {
            final String catDisplay = categories[i];
            final String catKey = categoryKeys[i];

            TextView optionView = new TextView(this);
            optionView.setText(catDisplay);
            optionView.setTextSize(13.5f);
            optionView.setGravity(Gravity.CENTER_VERTICAL);
            optionView.setPadding(dpToPx(16), dpToPx(13), dpToPx(16), dpToPx(13));

            boolean isCurrent = catKey.equalsIgnoreCase(selectedCategory[0]);
            if (isCurrent) {
                optionView.setBackgroundResource(R.drawable.bg_picker_cell_selected);
                optionView.setTextColor(Color.WHITE);
                optionView.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                optionView.setBackgroundResource(R.drawable.bg_picker_cell);
                optionView.setTextColor(0xFF2B1D1D);
                optionView.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.bottomMargin = dpToPx(8);
            optionView.setLayoutParams(lp);

            optionView.setOnClickListener(v -> {
                selectedCategory[0] = catKey;
                tvSelectedCategory.setText(catDisplay);
                catDialog.dismiss();
            });

            layoutOptions.addView(optionView);
        }

        if (btnCancel != null) btnCancel.setOnClickListener(v -> catDialog.dismiss());
        catDialog.show();
    }

    /**
     * Progressive Date Picker Dialog:
     * Alur: Pilih Tahun -> Langsung ke Bulan -> Langsung ke Tanggal.
     * Jika tidak memilih bulan, simpan Tahun saja.
     * Jika tidak memilih tanggal, simpan Bulan & Tahun saja.
     */
    private void showProgressiveDatePickerDialog(TextView tvSelectedDate, View btnClearDate, View ivDropdownDate, String[] selectedTargetDate) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_progressive_date_picker);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        // Header Views
        TextView tvDialogBadge = dialog.findViewById(R.id.tv_dialog_badge);
        TextView tvStepTitle = dialog.findViewById(R.id.tv_step_title);
        TextView tvStepSubtitle = dialog.findViewById(R.id.tv_step_subtitle);

        // Steps Containers
        View layoutStepYear = dialog.findViewById(R.id.layout_step_year);
        View layoutStepMonth = dialog.findViewById(R.id.layout_step_month);
        View layoutStepDate = dialog.findViewById(R.id.layout_step_date);

        // Step 1 Views
        GridLayout gridYears = dialog.findViewById(R.id.grid_years);

        // Step 2 Views
        View btnConfirmYearOnly = dialog.findViewById(R.id.btn_confirm_year_only);
        TextView tvBtnConfirmYear = dialog.findViewById(R.id.tv_btn_confirm_year);
        GridLayout gridMonths = dialog.findViewById(R.id.grid_months);
        View btnBackToYear = dialog.findViewById(R.id.btn_back_to_year);

        // Step 3 Views
        View btnConfirmMonthOnly = dialog.findViewById(R.id.btn_confirm_month_only);
        TextView tvBtnConfirmMonth = dialog.findViewById(R.id.tv_btn_confirm_month);
        GridLayout gridDays = dialog.findViewById(R.id.grid_days);
        View btnBackToMonth = dialog.findViewById(R.id.btn_back_to_month);

        View btnCancel = dialog.findViewById(R.id.btn_cancel_progressive);

        final int currentYearNow = Calendar.getInstance().get(Calendar.YEAR);
        final int currentMonthNow = Calendar.getInstance().get(Calendar.MONTH);

        final int[] chosenYear = {currentYearNow};
        final int[] chosenMonth = {currentMonthNow};
        final String[] chosenMonthName = {""};

        String[] monthsList = {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"};

        // Methods to transition between steps:
        Runnable showStep1 = () -> {
            tvDialogBadge.setText("Langkah 1 • Target Tahun");
            tvStepTitle.setText("Pilih Tahun Target ♡");
            tvStepSubtitle.setText("Tentukan tahun target impian berdua");

            layoutStepYear.setVisibility(View.VISIBLE);
            layoutStepMonth.setVisibility(View.GONE);
            layoutStepDate.setVisibility(View.GONE);
        };

        Runnable showStep2 = () -> {
            tvDialogBadge.setText("Langkah 2 • Target Bulan (" + chosenYear[0] + ")");
            tvStepTitle.setText("Pilih Bulan ♡");
            tvStepSubtitle.setText("Pilih bulan, atau simpan hanya tahun " + chosenYear[0]);

            tvBtnConfirmYear.setText("✓ Cukup Pilih Tahun " + chosenYear[0] + " Saja");

            // Populate Months (Step 2)
            gridMonths.removeAllViews();
            for (int i = 0; i < monthsList.length; i++) {
                final String monthName = monthsList[i];
                final int monthIndex = i;
                TextView monthView = new TextView(this);
                monthView.setText(monthName);
                monthView.setTextSize(12.5f);
                monthView.setGravity(Gravity.CENTER);

                boolean isCurrent = (chosenYear[0] == currentYearNow && monthIndex == currentMonthNow);
                if (isCurrent) {
                    monthView.setBackgroundResource(R.drawable.bg_picker_cell_selected);
                    monthView.setTextColor(Color.WHITE);
                    monthView.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    monthView.setBackgroundResource(R.drawable.bg_picker_cell);
                    monthView.setTextColor(0xFF4A3E3D);
                    monthView.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
                monthView.setPadding(0, dpToPx(12), 0, dpToPx(12));

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.columnSpec = GridLayout.spec(i % 3, 1f);
                params.rowSpec = GridLayout.spec(i / 3);
                params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
                monthView.setLayoutParams(params);

                monthView.setOnClickListener(v -> {
                    chosenMonth[0] = monthIndex;
                    chosenMonthName[0] = monthName;

                    // Step 3 transition
                    tvDialogBadge.setText("Langkah 3 • Tanggal (" + chosenMonthName[0] + " " + chosenYear[0] + ")");
                    tvStepTitle.setText("Pilih Tanggal Impian ♡");
                    tvStepSubtitle.setText("Pilih tanggal, atau simpan " + chosenMonthName[0] + " " + chosenYear[0]);
                    tvBtnConfirmMonth.setText("✓ Cukup Pilih " + chosenMonthName[0] + " " + chosenYear[0] + " Saja");

                    // Render days for this month and year
                    renderDaysGrid(gridDays, chosenYear[0], chosenMonth[0], (day) -> {
                        String formatted = day + " " + chosenMonthName[0] + " " + chosenYear[0];
                        selectedTargetDate[0] = formatted;
                        tvSelectedDate.setText(formatted);
                        tvSelectedDate.setTextColor(0xFF2B1D1D);
                        tvSelectedDate.setTypeface(null, android.graphics.Typeface.BOLD);
                        btnClearDate.setVisibility(View.VISIBLE);
                        if (ivDropdownDate != null) ivDropdownDate.setVisibility(View.GONE);
                        dialog.dismiss();
                    });

                    layoutStepYear.setVisibility(View.GONE);
                    layoutStepMonth.setVisibility(View.GONE);
                    layoutStepDate.setVisibility(View.VISIBLE);
                });

                gridMonths.addView(monthView);
            }

            layoutStepYear.setVisibility(View.GONE);
            layoutStepMonth.setVisibility(View.VISIBLE);
            layoutStepDate.setVisibility(View.GONE);
        };

        // Populate Years (Step 1)
        int startYear = currentYearNow;
        int totalYears = 9;
        gridYears.removeAllViews();
        for (int i = 0; i < totalYears; i++) {
            final int year = startYear + i;
            TextView yearView = new TextView(this);
            yearView.setText(String.valueOf(year));
            yearView.setTextSize(14f);
            yearView.setGravity(Gravity.CENTER);

            if (year == currentYearNow) {
                yearView.setBackgroundResource(R.drawable.bg_picker_cell_selected);
                yearView.setTextColor(Color.WHITE);
                yearView.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                yearView.setBackgroundResource(R.drawable.bg_picker_cell);
                yearView.setTextColor(0xFF4A3E3D);
                yearView.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
            yearView.setPadding(0, dpToPx(14), 0, dpToPx(14));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 3, 1f);
            params.rowSpec = GridLayout.spec(i / 3);
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            yearView.setLayoutParams(params);

            yearView.setOnClickListener(v -> {
                chosenYear[0] = year;
                showStep2.run();
            });

            gridYears.addView(yearView);
        }

        // Action: Cukup Pilih Tahun Saja
        btnConfirmYearOnly.setOnClickListener(v -> {
            String result = "Tahun " + chosenYear[0];
            selectedTargetDate[0] = result;
            tvSelectedDate.setText(result);
            tvSelectedDate.setTextColor(0xFF2B1D1D);
            tvSelectedDate.setTypeface(null, android.graphics.Typeface.BOLD);
            btnClearDate.setVisibility(View.VISIBLE);
            if (ivDropdownDate != null) ivDropdownDate.setVisibility(View.GONE);
            dialog.dismiss();
        });

        // Action: Cukup Pilih Bulan & Tahun Saja
        btnConfirmMonthOnly.setOnClickListener(v -> {
            String result = chosenMonthName[0] + " " + chosenYear[0];
            selectedTargetDate[0] = result;
            tvSelectedDate.setText(result);
            tvSelectedDate.setTextColor(0xFF2B1D1D);
            tvSelectedDate.setTypeface(null, android.graphics.Typeface.BOLD);
            btnClearDate.setVisibility(View.VISIBLE);
            if (ivDropdownDate != null) ivDropdownDate.setVisibility(View.GONE);
            dialog.dismiss();
        });

        // Navigation back
        btnBackToYear.setOnClickListener(v -> showStep1.run());
        btnBackToMonth.setOnClickListener(v -> showStep2.run());

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        showStep1.run();
        dialog.show();
    }

    private interface OnDaySelectedListener {
        void onDaySelected(int day);
    }

    private void renderDaysGrid(GridLayout gridDays, int year, int month, OnDaySelectedListener listener) {
        gridDays.removeAllViews();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // Sunday = 1
        int emptyOffset = firstDayOfWeek - Calendar.SUNDAY;
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();
        boolean isCurrentMonth = (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < emptyOffset; i++) {
            View emptyView = new View(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = dpToPx(38);
            params.columnSpec = GridLayout.spec(i % 7, 1f);
            params.rowSpec = GridLayout.spec(i / 7);
            params.setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
            emptyView.setLayoutParams(params);
            gridDays.addView(emptyView);
        }

        for (int day = 1; day <= maxDays; day++) {
            final int selectedDay = day;
            int cellIndex = emptyOffset + day - 1;
            int col = cellIndex % 7;
            int row = cellIndex / 7;

            TextView dayView = new TextView(this);
            dayView.setText(String.valueOf(day));
            dayView.setTextSize(12.5f);
            dayView.setGravity(Gravity.CENTER);

            boolean isToday = isCurrentMonth && (day == todayDay);
            if (isToday) {
                dayView.setBackgroundResource(R.drawable.bg_picker_cell_selected);
                dayView.setTextColor(Color.WHITE);
                dayView.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                dayView.setBackgroundResource(R.drawable.bg_picker_cell);
                if (col == 0) {
                    dayView.setTextColor(0xFFD45863);
                } else {
                    dayView.setTextColor(0xFF2B1D1D);
                }
                dayView.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = dpToPx(38);
            params.columnSpec = GridLayout.spec(col, 1f);
            params.rowSpec = GridLayout.spec(row);
            params.setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
            dayView.setLayoutParams(params);

            dayView.setOnClickListener(v -> listener.onDaySelected(selectedDay));

            gridDays.addView(dayView);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
