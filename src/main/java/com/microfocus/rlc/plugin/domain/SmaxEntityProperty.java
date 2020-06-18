package com.microfocus.rlc.plugin.domain;

public class SmaxEntityProperty {
    private String propertyName;
    private String propertyDisplayName;
    private String propertyType;

    public SmaxEntityProperty() {}

    public SmaxEntityProperty(String name, String displayName, String type) {
        propertyName = name;
        propertyDisplayName = displayName;
        propertyType = type;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String val) {
        propertyName = val;
    }

    public String getPropertyDisplayName() {
        return propertyDisplayName;
    }

    public void setPropertyDisplayName(String val) {
        propertyDisplayName = val;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String val) {
        propertyType = val;
    }
}