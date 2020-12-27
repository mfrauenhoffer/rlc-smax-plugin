package com.microfocus.rlc.plugin.domain;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SmaxEntityOperation {

    @SerializedName("entities")
    private ArrayList<SmaxEntity> entities;

    @SerializedName("operation")
    private String operation;

    public SmaxEntityOperation(ArrayList<SmaxEntity> arrEntities, String operationToPerform) {
        this.entities = arrEntities;
        this.operation = operationToPerform;
    }

    public ArrayList<SmaxEntity> getEntities() {
        return entities;
    }

    public void setEntities(ArrayList<SmaxEntity> val) {
        entities = val;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
