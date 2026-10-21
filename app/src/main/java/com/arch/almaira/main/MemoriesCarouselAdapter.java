package com.arch.almaira.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arch.almaira.R;

import java.util.List;

public class MemoriesCarouselAdapter extends RecyclerView.Adapter<MemoriesCarouselAdapter.MemoryViewHolder> {

    public static final int LOOP_FACTOR = 400; // Continuous smooth browsing without boundary locks

    public interface OnMemoryClickListener {
        void onMemoryClick(MemoryItem item, int adapterPosition);
    }

    private final Context context;
    private final List<MemoryItem> memoryList;
    private final OnMemoryClickListener listener;
    private int activePosition = 0;

    public MemoriesCarouselAdapter(Context context, List<MemoryItem> memoryList, OnMemoryClickListener listener) {
        this.context = context;
        this.memoryList = memoryList;
        this.listener = listener;
    }

    public void setActivePosition(int position) {
        this.activePosition = position;
    }

    public int getActivePosition() {
        return activePosition;
    }

    public int getInitialStartPosition() {
        if (memoryList == null || memoryList.isEmpty()) return 0;
        int half = LOOP_FACTOR / 2;
        return half * memoryList.size();
    }

    @NonNull
    @Override
    public MemoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_memory_carousel, parent, false);
        return new MemoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoryViewHolder holder, int position) {
        if (memoryList == null || memoryList.isEmpty()) return;

        int actualPos = position % memoryList.size();
        MemoryItem item = memoryList.get(actualPos);

        if (holder.containerMemory != null) {
            holder.containerMemory.setVisibility(View.VISIBLE);
        }

        if (item.getImageRes() != 0 && holder.ivMemoryImage != null) {
            holder.ivMemoryImage.setImageResource(item.getImageRes());
        }

        // Tampilkan hanya tanggal di kartu foto
        if (holder.tvMemoryTitle != null) {
            holder.tvMemoryTitle.setText(item.getDate());
        }
        if (holder.tvMemoryDate != null) {
            holder.tvMemoryDate.setText(item.getDate());
        }

        // Tanggal hanya tampil di slide paling kiri (posisi aktif)
        if (holder.layoutTextOverlay != null) {
            holder.layoutTextOverlay.animate().cancel();
            if (position == activePosition) {
                holder.layoutTextOverlay.setAlpha(1.0f);
                holder.layoutTextOverlay.setVisibility(View.VISIBLE);
            } else {
                holder.layoutTextOverlay.setAlpha(0.0f);
                holder.layoutTextOverlay.setVisibility(View.INVISIBLE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMemoryClick(item, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        if (memoryList == null || memoryList.isEmpty()) return 0;
        return memoryList.size() * LOOP_FACTOR;
    }

    static class MemoryViewHolder extends RecyclerView.ViewHolder {
        View containerMemory;
        View layoutTextOverlay;
        ImageView ivMemoryImage;
        TextView tvMemoryTitle;
        TextView tvMemoryDate;

        public MemoryViewHolder(@NonNull View itemView) {
            super(itemView);
            containerMemory = itemView.findViewById(R.id.container_memory);
            layoutTextOverlay = itemView.findViewById(R.id.layout_memory_text_overlay);
            ivMemoryImage = itemView.findViewById(R.id.iv_memory_image);
            tvMemoryTitle = itemView.findViewById(R.id.tv_memory_title);
            tvMemoryDate = itemView.findViewById(R.id.tv_memory_date);
        }
    }
}
