package com.microfocus.rlc.plugin.domain;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SmaxQueryResult {

	private static final Logger logger = LoggerFactory.getLogger(SmaxQueryResult.class);
	
    @SerializedName("entities")
    private List<SmaxEntity> entities;

	public List<SmaxEntity> getEntities() {
		return entities;
	}

	public static List<SmaxEntity> parse(String json) {
        /*logger.debug("In SmaxQueryResult#parse()");
        logger.debug("Begun parsing JSON");
        logger.debug("JSON Repose to Parse: " + json);*/
        Gson gson = new Gson();
        SmaxQueryResult results = gson.fromJson(json, SmaxQueryResult.class);
        
        /*logger.debug("Number of result entities found: " + result.getEntities().size());
        logger.debug("First Entity, Entity ID: " + result.getEntities().get(0).getEntityId()); */
        return results.getEntities();
    }
}