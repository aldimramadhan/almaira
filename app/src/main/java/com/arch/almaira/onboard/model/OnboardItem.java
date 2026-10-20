package com.arch.almaira.onboard.model;

import androidx.annotation.LayoutRes;

public class OnboardItem {
    @LayoutRes
    private final int slideLayoutRes;

    public OnboardItem(@LayoutRes int slideLayoutRes) {
        this.slideLayoutRes = slideLayoutRes;
    }

    @LayoutRes
    public int getSlideLayoutRes() {
        return slideLayoutRes;
    }
}
