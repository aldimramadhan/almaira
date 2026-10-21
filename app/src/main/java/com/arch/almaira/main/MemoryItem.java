package com.arch.almaira.main;

public class MemoryItem {
    private final int id;
    private final String title;
    private final String date;
    private final int imageRes;
    private final boolean isAddCard;

    public MemoryItem(int id, String title, String date, int imageRes, boolean isAddCard) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.imageRes = imageRes;
        this.isAddCard = isAddCard;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public int getImageRes() {
        return imageRes;
    }

    public boolean isAddCard() {
        return isAddCard;
    }
}
