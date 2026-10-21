package com.arch.almaira.wishlist;

import org.json.JSONException;
import org.json.JSONObject;

public class WishlistItem {
    private long id;
    private String title;
    private String category;
    private boolean isCompleted;
    private String addedBy;
    private String targetDate;
    private String completedDate;

    public WishlistItem(long id, String title, String category, boolean isCompleted, String addedBy, String targetDate, String completedDate) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.isCompleted = isCompleted;
        this.addedBy = addedBy;
        this.targetDate = targetDate;
        this.completedDate = completedDate;
    }

    public WishlistItem(String title, String category, String addedBy, String targetDate) {
        this(System.currentTimeMillis(), title, category, false, addedBy, targetDate, null);
    }

    public String getCreatedBy() {
        return addedBy;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }

    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("title", title);
            json.put("category", category);
            json.put("isCompleted", isCompleted);
            json.put("addedBy", addedBy);
            json.put("targetDate", targetDate);
            json.put("completedDate", completedDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static WishlistItem fromJsonObject(JSONObject json) {
        if (json == null) return null;
        long id = json.optLong("id", System.currentTimeMillis());
        String title = json.optString("title", "");
        String category = json.optString("category", "Umum");
        boolean isCompleted = json.optBoolean("isCompleted", false);
        String addedBy = json.optString("addedBy", "Aldi");
        String targetDate = json.optString("targetDate", "");
        String completedDate = json.optString("completedDate", "");
        return new WishlistItem(id, title, category, isCompleted, addedBy, targetDate, completedDate);
    }
}
