package com.datarain.pdp.common.valueobject;

/**
 * اضافه شد: Value Object برای IP Address
 */
public record IpAddress(String value) {

    public IpAddress {
        if (value == null || value.isBlank()) {
            value = "unknown";
        }
    }

    public boolean isLocalhost() {
        return "127.0.0.1".equals(value) || "::1".equals(value) || "0:0:0:0:0:0:0:1".equals(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
