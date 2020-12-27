package com.microfocus.rlc.plugin.domain;

import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmaxEntityOperationResult {
    private static final Logger logger = LoggerFactory.getLogger(SmaxEntityOperationResult.class);

    @SerializedName("entity")
    private SmaxEntity entity;

    @SerializedName("completion_status")
    private String completion_status;

    public void setCompletion_status(String val) {
        this.completion_status = val;
    }

    public String getCompletion_status() {
        return completion_status;
    }

    public void setEntity(SmaxEntity entity) {
        this.entity = entity;
    }

    public SmaxEntity getEntity() {
        return entity;
    }
}
