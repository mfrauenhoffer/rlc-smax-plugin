package com.microfocus.rlc.plugin.domain;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SmaxBulkOperationResult {

    private static final Logger logger = LoggerFactory.getLogger(SmaxBulkOperationResult.class);

    @SerializedName("entity_result_list")
    private List<SmaxEntityOperationResult> entityResults;

    @SerializedName("meta")
    private Meta bulkOperationMetadata;

    public SmaxBulkOperationResult() {}

    public static SmaxBulkOperationResult parse(String jsonResult) {
        logger.debug("In SmaxBulkOperationResult::parse()");
        logger.debug("JSON Response to Parse: " + jsonResult);

        Gson gson = new Gson();
        return gson.fromJson(jsonResult, SmaxBulkOperationResult.class);
    }

    public List<SmaxEntityOperationResult> getEntityResults() {
        return entityResults;
    }

    public void setEntityResults(List<SmaxEntityOperationResult> entityResults) {
        this.entityResults = entityResults;
    }

    public Meta getBulkOperationMetadata() {
        return bulkOperationMetadata;
    }

    public void setBulkOperationMetadata(Meta bulkOperationMetadata) {
        this.bulkOperationMetadata = bulkOperationMetadata;
    }

    public String getOperationResultStatus() {
        return bulkOperationMetadata.getCompletion_status();
    }

    public String getSmaxEntityId() {
        return entityResults.get(0).getEntity().getEntityId();
    }

    static class Meta {
        private String completion_status;

        public String getCompletion_status() {
            return completion_status;
        }

        public void setCompletion_status(String val) {
            this.completion_status = val;
        }
    }
}
