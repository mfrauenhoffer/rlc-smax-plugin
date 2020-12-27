package com.microfocus.rlc.plugin.client;

import com.microfocus.rlc.plugin.domain.SmaxBulkOperationResult;
import com.microfocus.rlc.plugin.domain.SmaxEntity;
import com.microfocus.rlc.plugin.domain.SmaxEntityOperation;

import java.util.List;

public interface SmaxClient {

    String startSmaxSession(String url, String tenantId, String username, String password);

    List<SmaxEntity> getRecords(String serverUrl, String tenantId, String sessionId, String entityType, List<String> propertyLayout);

    SmaxEntity getRecord(String serverUrl, String tenantId, String sessionId, String entityType, List<String> propertyLayout, String entityId);

    SmaxBulkOperationResult createSmaxRecord(String serverUrl, String tenantId, String sessionId, SmaxEntityOperation create);
}