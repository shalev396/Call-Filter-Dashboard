package com.example.offdutycallfilter;

public class WhitelistedContact {
    private final String name;
    private final String phoneE164;

    public WhitelistedContact(String name, String phoneE164) {
        this.name = name;
        this.phoneE164 = phoneE164;
    }

    public String getName() {
        return name;
    }

    public String getPhoneE164() {
        return phoneE164;
    }
}
