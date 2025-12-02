package com.example.callfilter;

public class BlockedCall {
    private final String number;
    private final long timestamp;

    public BlockedCall(String number, long timestamp) {
        this.number = number;
        this.timestamp = timestamp;
    }

    public String getNumber() {
        return number;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
