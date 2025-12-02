package com.example.callfilter;

public class WhitelistedContact {
    private final String id;
    private final String name;
    private final String phoneE164;

    public WhitelistedContact(String id, String name, String phoneE164) {
        this.id = id;
        this.name = name;
        this.phoneE164 = phoneE164;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneE164() {
        return phoneE164;
    }
}
