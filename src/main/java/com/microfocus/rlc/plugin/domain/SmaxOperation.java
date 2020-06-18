package com.microfocus.rlc.plugin.domain;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SmaxOperation {
	
    @SerializedName("entities")
    private List<SmaxEntity> entities;

    @SerializedName("operation")
    private String operation;

	public List<SmaxEntity> getEntities() {
		return entities;
    }
    
    public void setOperation(String val) {
        this.operation = val;
    }

    public String getOperation() {
        return this.operation;
    }
}