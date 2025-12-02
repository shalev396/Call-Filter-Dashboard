package com.example.callfilter;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final String PREFS_NAME = "call_filter_prefs";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_SCHEDULE = "schedule_json";
    private static final String KEY_WHITELIST = "whitelist_json";
    private static final String KEY_BLOCKED_CALLS = "blocked_calls_json";

    private final SharedPreferences prefs;
    private final Gson gson;

    public ConfigManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public boolean isEnabled() {
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    public void setEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public List<DaySchedule> getSchedule() {
        String json = prefs.getString(KEY_SCHEDULE, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<DaySchedule>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void setSchedule(List<DaySchedule> schedule) {
        String json = gson.toJson(schedule);
        prefs.edit().putString(KEY_SCHEDULE, json).apply();
    }

    public List<WhitelistedContact> getWhitelist() {
        String json = prefs.getString(KEY_WHITELIST, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<WhitelistedContact>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void setWhitelist(List<WhitelistedContact> wl) {
        String json = gson.toJson(wl);
        prefs.edit().putString(KEY_WHITELIST, json).apply();
    }

    public List<BlockedCall> getBlockedCalls() {
        String json = prefs.getString(KEY_BLOCKED_CALLS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<BlockedCall>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void addBlockedCall(BlockedCall call) {
        List<BlockedCall> calls = getBlockedCalls();
        calls.add(0, call); // Add to the top of the list
        String json = gson.toJson(calls);
        prefs.edit().putString(KEY_BLOCKED_CALLS, json).apply();
    }
}
