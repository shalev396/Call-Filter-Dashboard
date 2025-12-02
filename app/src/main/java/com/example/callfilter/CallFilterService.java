package com.example.callfilter;

import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.time.ZonedDateTime;
import java.util.List;

public class CallFilterService extends CallScreeningService {

    private static final String TAG = "CallFilterService";

    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {
        Log.d(TAG, "onScreenCall: --- New Call Received ---");
        ConfigManager configManager = new ConfigManager(getApplicationContext());

        if (!configManager.isEnabled()) {
            Log.d(TAG, "onScreenCall: Filter is DISABLED. Allowing call.");
            respondToCall(callDetails, new CallResponse.Builder().build());
            return;
        }
        Log.d(TAG, "onScreenCall: Filter is ENABLED.");

        String incomingNumber = callDetails.getHandle().getSchemeSpecificPart();
        String normalizedNumber = PhoneNumberUtils.normalizeNumber(incomingNumber);
        Log.d(TAG, "onScreenCall: Incoming number: " + normalizedNumber);

        List<WhitelistedContact> whitelist = configManager.getWhitelist();
        for (WhitelistedContact contact : whitelist) {
            if (PhoneNumberUtils.compare(contact.getPhoneE164(), normalizedNumber)) {
                Log.d(TAG, "onScreenCall: Number is in the WHITELIST. Allowing call.");
                respondToCall(callDetails, new CallResponse.Builder().build());
                return;
            }
        }
        Log.d(TAG, "onScreenCall: Number is NOT in the whitelist.");

        List<DaySchedule> schedule = configManager.getSchedule();
        if (isAllowedNow(schedule)) {
            Log.d(TAG, "onScreenCall: Current time is INSIDE a scheduled window. Allowing call.");
            respondToCall(callDetails, new CallResponse.Builder().build());
        } else {
            Log.d(TAG, "onScreenCall: Current time is OUTSIDE a scheduled window. BLOCKING call.");
            configManager.addBlockedCall(new BlockedCall(normalizedNumber, System.currentTimeMillis()));
            respondToCall(callDetails, new CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(true)
                    .build());
        }
    }

    private boolean isAllowedNow(List<DaySchedule> schedule) {
        ZonedDateTime now = ZonedDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue() % 7; // Sunday = 0, Monday = 1, ...
        int minutes = now.getHour() * 60 + now.getMinute();
        Log.d(TAG, "isAllowedNow: Checking schedule for day=" + dayOfWeek + ", minutes=" + minutes);

        for (DaySchedule daySchedule : schedule) {
            if (daySchedule.getDayOfWeek() == dayOfWeek) {
                Log.d(TAG, "isAllowedNow: Found schedule for today.");
                for (TimeWindow window : daySchedule.getWindows()) {
                    Log.d(TAG, "isAllowedNow: Checking window: " + window.getStartMinutes() + " - " + window.getEndMinutes());
                    if (minutes >= window.getStartMinutes() && minutes < window.getEndMinutes()) {
                        Log.d(TAG, "isAllowedNow: Current time is INSIDE this window.");
                        return true;
                    }
                }
                Log.d(TAG, "isAllowedNow: Current time is OUTSIDE all of today's windows.");
                break; // No need to check other days
            }
        }

        Log.d(TAG, "isAllowedNow: No schedule found for today or time is outside all windows.");
        return false;
    }
}
