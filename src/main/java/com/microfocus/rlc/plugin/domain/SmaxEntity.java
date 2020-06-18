package com.microfocus.rlc.plugin.domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SmaxEntity {

    private static final Logger logger = LoggerFactory.getLogger(SmaxEntity.class);

    public static final String ENTITY_ID_KEY = "Id";
    public static final String ENTITY_TYPE_KEY = "entityType";
    public static final String ENTITY_TITLE_KEY = "entityTitle";
    public static final String ENTITY_DESCRIPTION_KEY = "entityDescription";
    public static final String ENTITY_CREATION_DATE_KEY = "createdOn";
    public static final String ENTITY_CREATED_BY_KEY = "createdBy";
    public static final String ENTITY_ADDITIONAL_PARAMS_KEY = "entityAddtlParamsToReturn";
    
    @SerializedName("entity_type")
    private String entityType;

    @SerializedName("properties")
    private Map<String, Object> entity_properties;

    @SerializedName("related_properties")
    private Map<String, Object> entity_related_properties;

    @Expose(serialize = false, deserialize = false)
    private String entityTitle;

    public SmaxEntity() {
    }

    public String getEntityId() {
        try {
            return entity_properties.get(ENTITY_ID_KEY).toString();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public void setEntityTitle(String val) {
        entityTitle = val;
    }

    public String getEntityTitleKey() {
        return entityTitle;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getPropertyAsString(String key) {
        if (key.contains(".")) {
            // Need to split the key to two seperate keys
            //logger.debug("Key contains . character; actual key = " + key);
            String keyArray[] = key.split("\\.");
            //logger.debug("keyArray has following capacity: " + keyArray.length);
            Object containerKeyObject = entity_related_properties.get(keyArray[0]);
            return ((LinkedTreeMap) containerKeyObject).get(keyArray[1]).toString();
        } else {
            return entity_properties.get(key).toString();
        }
    }

    public Long getPropertyAsLong(String key) {
        try {
            if (entity_properties.get(key).getClass() == Long.class) {
                return (Long) entity_properties.get(key);
            }

            if (entity_properties.get(key).getClass() == Double.class) {
                return ((Double) entity_properties.get(key)).longValue();
            }
            return (Long) entity_properties.get(key);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }


}