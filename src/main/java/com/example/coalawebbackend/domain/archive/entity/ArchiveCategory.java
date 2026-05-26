package com.example.coalawebbackend.domain.archive.entity;

public enum ArchiveCategory {
    LABS("labs"),
    AGENTS("agents");

    private final String apiValue;

    ArchiveCategory(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static ArchiveCategory from(String value) {
        if (value == null || value.isBlank()) {
            return LABS;
        }

        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "agent", "agents", "skill", "skills", "harness", "harnesses" -> AGENTS;
            case "lab", "labs", "research", "seminar", "seminars" -> LABS;
            default -> LABS;
        };
    }
}
