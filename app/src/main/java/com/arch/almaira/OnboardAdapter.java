package com.arch.almaira;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OnboardAdapter extends RecyclerView.Adapter<OnboardAdapter.SlideViewHolder> {

    private final List<OnboardItem> items;
    private final LayoutInflater inflater;

    public OnboardAdapter(Context context, List<OnboardItem> items) {
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getSlideLayoutRes();
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(viewType, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        // Static slide layouts are bound directly via viewType inflation
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
