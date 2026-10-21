package com.arch.almaira.wishlist;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WishlistManager {
    private static final String PREF_NAME = "almaira_wishlist_prefs";
    private static final String KEY_ITEMS = "wishlist_items_json";

    public static List<WishlistItem> getItems(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_ITEMS, null);
        List<WishlistItem> list = new ArrayList<>();

        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            list = getDefaultItems();
            saveItems(context, list);
            return list;
        }

        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                WishlistItem item = WishlistItem.fromJsonObject(arr.getJSONObject(i));
                if (item != null) {
                    list.add(item);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            list = getDefaultItems();
            saveItems(context, list);
        }

        return list;
    }

    public static void saveItems(Context context, List<WishlistItem> list) {
        JSONArray arr = new JSONArray();
        for (WishlistItem item : list) {
            arr.put(item.toJsonObject());
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ITEMS, arr.toString()).apply();
    }

    public static void addItem(Context context, WishlistItem newItem) {
        List<WishlistItem> list = getItems(context);
        list.add(0, newItem);
        saveItems(context, list);
    }

    public static void updateItem(Context context, WishlistItem updatedItem) {
        List<WishlistItem> list = getItems(context);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == updatedItem.getId()) {
                list.set(i, updatedItem);
                break;
            }
        }
        saveItems(context, list);
    }

    public static boolean toggleItem(Context context, long id) {
        List<WishlistItem> list = getItems(context);
        boolean newState = false;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        String todayStr = sdf.format(new Date());

        for (WishlistItem item : list) {
            if (item.getId() == id) {
                item.setCompleted(!item.isCompleted());
                newState = item.isCompleted();
                if (newState) {
                    item.setCompletedDate(todayStr);
                } else {
                    item.setCompletedDate("");
                }
                break;
            }
        }
        saveItems(context, list);
        return newState;
    }

    public static void deleteItem(Context context, long id) {
        List<WishlistItem> list = getItems(context);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == id) {
                list.remove(i);
                break;
            }
        }
        saveItems(context, list);
    }

    public static int getCompletedCount(List<WishlistItem> list) {
        int count = 0;
        for (WishlistItem item : list) {
            if (item.isCompleted()) count++;
        }
        return count;
    }

    private static List<WishlistItem> getDefaultItems() {
        List<WishlistItem> list = new ArrayList<>();
        list.add(new WishlistItem(1, "Umrah ke Makkah & Madinah", "Travel & Trip", true, "Aldi & Ira", "Tahun 2026", "10 Agustus 2026"));
        list.add(new WishlistItem(2, "Jalan-jalan berdua ke Pantai", "Kencan & Momen", false, "Aldi", "November 2026", ""));
        list.add(new WishlistItem(3, "Jajan Kuliner Malam & Street Food", "Kuliner & Jajan", false, "Ira", "Minggu ini", ""));
        list.add(new WishlistItem(4, "Beli Cincin Couple Impian", "Barang Impian", false, "Aldi", "Akhir Tahun 2026", ""));
        list.add(new WishlistItem(5, "Nonton Bioskop Midnight Berdua", "Kencan & Momen", true, "Ira", "Bulan Lalu", "15 September 2026"));
        list.add(new WishlistItem(6, "Liburan Santai ke Kebun Teh & Bunga", "Travel & Trip", false, "Ira", "Desember 2026", ""));
        return list;
    }
}
