package com.datarain.pdp.common.valueobject;

import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * اضافه شد: Value Object برای Email
 * به جای اینکه همه جا String ساده استفاده کنیم، type-safe هستیم
 * validation هم داخل خودشه
 */
@Embeddable
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    public Email {
        Objects.requireNonNull(value, "Email cannot be null");
        if (!EMAIL_PATTERN.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
        value = value.trim().toLowerCase();
    }

    @Override
    public String toString() {
        return value;
    }
}
