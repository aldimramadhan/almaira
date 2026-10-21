package com.arch.almaira.wishlist;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arch.almaira.R;

import java.util.ArrayList;
import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    public interface OnWishlistActionListener {
        void onToggleCompleted(WishlistItem item, int position);
        void onDeleteItem(WishlistItem item, int position);
    }

    private final List<WishlistItem> fullList = new ArrayList<>();
    private final List<WishlistItem> filteredList = new ArrayList<>();
    private String currentStatusFilter = "ALL";
    private String currentCategoryFilter = "ALL";
    private final OnWishlistActionListener actionListener;

    public WishlistAdapter(OnWishlistActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setItems(List<WishlistItem> items) {
        fullList.clear();
        if (items != null) {
            fullList.addAll(items);
        }
        applyFilters();
    }

    public void setStatusFilter(String status) {
        this.currentStatusFilter = status;
        applyFilters();
    }

    public void setCategoryFilter(String category) {
        this.currentCategoryFilter = category;
        applyFilters();
    }

    public String getCurrentStatusFilter() {
        return currentStatusFilter;
    }

    public String getCurrentCategoryFilter() {
        return currentCategoryFilter;
    }

    private void applyFilters() {
        filteredList.clear();
        for (WishlistItem item : fullList) {
            boolean matchStatus = true;
            if ("PENDING".equalsIgnoreCase(currentStatusFilter)) {
                matchStatus = !item.isCompleted();
            } else if ("COMPLETED".equalsIgnoreCase(currentStatusFilter)) {
                matchStatus = item.isCompleted();
            }

            boolean matchCategory = true;
            if (!"ALL".equalsIgnoreCase(currentCategoryFilter)) {
                String cat = item.getCategory();
                matchCategory = cat != null && cat.toLowerCase().contains(currentCategoryFilter.toLowerCase());
            }

            if (matchStatus && matchCategory) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public int getFilteredCount() {
        return filteredList.size();
    }

    public int getTotalCount() {
        return fullList.size();
    }

    public int getCompletedCount() {
        int count = 0;
        for (WishlistItem item : fullList) {
            if (item.isCompleted()) count++;
        }
        return count;
    }

    public int getPendingCount() {
        return getTotalCount() - getCompletedCount();
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        WishlistItem item = filteredList.get(position);
        holder.bind(item, actionListener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class WishlistViewHolder extends RecyclerView.ViewHolder {
        private final View btnToggleCheckbox;
        private final android.widget.ImageView ivCheckbox;
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final TextView tvCategoryBadge;
        private final TextView tvAddedBy;
        private final View btnDelete;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            btnToggleCheckbox = itemView.findViewById(R.id.btn_toggle_checkbox);
            ivCheckbox = itemView.findViewById(R.id.iv_checkbox);
            tvTitle = itemView.findViewById(R.id.tv_wishlist_title);
            tvSubtitle = itemView.findViewById(R.id.tv_wishlist_subtitle);
            tvCategoryBadge = itemView.findViewById(R.id.tv_category_badge);
            tvAddedBy = itemView.findViewById(R.id.tv_added_by);
            btnDelete = itemView.findViewById(R.id.btn_delete_item);
        }

        public void bind(WishlistItem item, OnWishlistActionListener listener) {
            tvTitle.setText(item.getTitle());

            // Category badge with icon emoji
            String cat = item.getCategory();
            if (cat == null || cat.isEmpty()) cat = "Impian";
            String badgeText = cat;
            if (cat.equalsIgnoreCase("Travel")) badgeText = "✈️ " + cat;
            else if (cat.equalsIgnoreCase("Kencan")) badgeText = "🌸 " + cat;
            else if (cat.equalsIgnoreCase("Kuliner")) badgeText = "🧋 " + cat;
            else if (cat.equalsIgnoreCase("Barang")) badgeText = "🎁 " + cat;
            else badgeText = "✨ " + cat;
            tvCategoryBadge.setText(badgeText);

            // Added by tag
            String creator = item.getAddedBy();
            if (creator == null || creator.isEmpty()) creator = "Kita";
            tvAddedBy.setText("oleh " + creator + " ♡");

            if (item.isCompleted()) {
                ivCheckbox.setImageResource(R.drawable.ic_checkbox_checked);
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setTextColor(0xFF9E8C8B);
                if (item.getCompletedDate() != null && !item.getCompletedDate().isEmpty()) {
                    tvSubtitle.setText("Terwujud: " + item.getCompletedDate() + " ♡");
                } else {
                    tvSubtitle.setText("Telah terwujud bersama ♡");
                }
                tvSubtitle.setTextColor(0xFF2E7D32);
            } else {
                ivCheckbox.setImageResource(R.drawable.ic_checkbox_unchecked);
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setTextColor(0xFF2B1D1D);
                if (item.getTargetDate() != null && !item.getTargetDate().isEmpty()) {
                    tvSubtitle.setText("Target: " + item.getTargetDate());
                } else {
                    tvSubtitle.setText("Rencana impian berdua");
                }
                tvSubtitle.setTextColor(0xFF786564);
            }

            View.OnClickListener toggleClickListener = v -> {
                if (listener != null) {
                    listener.onToggleCompleted(item, getAdapterPosition());
                }
            };

            btnToggleCheckbox.setOnClickListener(toggleClickListener);
            itemView.setOnClickListener(toggleClickListener);

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteItem(item, getAdapterPosition());
                }
            });
        }
    }
}
