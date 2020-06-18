package com.microfocus.rlc.plugin.domain.enums;

public enum SmaxEntityTypes {
    Request,
    Change,
    Incident,
    Idea;

    public static SmaxEntityTypes parse(String value) {
        for (SmaxEntityTypes fieldName : SmaxEntityTypes.values()) {
            if (fieldName.name().equalsIgnoreCase(value)) {
                return fieldName;
            }
        }
        return null;
    }
}