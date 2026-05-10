package com.example.coalawebbackend.domain.info.entity;

public enum InfoCategory {
    NEWS("news"),
    CONTEST("contest"),
    LAB("lab"),
    RESOURCE("resource");

    private final String apiValue;

    InfoCategory(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static InfoCategory from(String value) {
        if (value == null || value.isBlank()) {
            return NEWS;
        }
        for (InfoCategory category : values()) {
            if (category.name().equalsIgnoreCase(value) || category.apiValue.equalsIgnoreCase(value)) {
                return category;
            }
        }
        return NEWS;
    }
}
