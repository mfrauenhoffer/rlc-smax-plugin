package com.microfocus.rlc.plugin.client;

import com.google.gson.Gson;
import com.microfocus.rlc.plugin.domain.SmaxAuthenticationRequest;
import com.microfocus.rlc.plugin.domain.SmaxEntity;
import com.microfocus.rlc.plugin.domain.SmaxQueryResult;
import com.microfocus.rlc.plugin.utils.LocalHttpClient;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

@Service
public class SmaxClientImpl implements SmaxClient {

    private static final Logger logger = LoggerFactory.getLogger(SmaxClientImpl.class);
    private static final String REST_PATH = "/rest/";
    private static final String EMS_PATH = "/ems/";
    private static final String APPLICATION_JSON = "application/json";
    private static final String SESSION_COOKIE_KEY = "LWSSO_COOKIE_KEY";
    private static final String AUTHENTICATION_PATH = "/auth/authentication-endpoint/authenticate/login";
    private static final String REQUEST_CONTENT_TYPE = "Content-Type";
    private static final String REQUEST_COOKIE_HEADER = "Cookie";

    @Override
    public String startSmaxSession(String serverUrl, String tenantId, String username, String password) {
        String url = serverUrl + AUTHENTICATION_PATH + "?TENANTID=" + tenantId;
        Gson gson = new Gson();
        SmaxAuthenticationRequest authRequestObject = new SmaxAuthenticationRequest(username, password);
        String requestBody = gson.toJson(authRequestObject);
        HashMap<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put(REQUEST_CONTENT_TYPE, APPLICATION_JSON);
        
        return LocalHttpClient.doPOST(url, requestHeaders, requestBody);
    }

    @Override
    public List<SmaxEntity> getRecords(String serverUrl, String tenantId, String sessionId, String entityType, List<String> propertyLayout) {

        String layoutString = generateLayoutString(propertyLayout);
        String url = serverUrl + REST_PATH + tenantId + EMS_PATH + entityType + "?layout=" + layoutString;
        logger.debug("getRecords URL String: " + url);
        HashMap<String,String> requestHeaders = new HashMap<String,String>();
        requestHeaders.put(REQUEST_CONTENT_TYPE, APPLICATION_JSON);
        requestHeaders.put(REQUEST_COOKIE_HEADER, SESSION_COOKIE_KEY + "=" + sessionId);

        return SmaxQueryResult.parse(LocalHttpClient.doGET(url, requestHeaders));        
    }

    @Override
    public SmaxEntity getRecord(String serverUrl, String tenantId, String sessionId, String entityType, List<String> propertyLayout, String requestId) {
        String layoutString = generateLayoutString(propertyLayout);
        String url = serverUrl + REST_PATH + tenantId + EMS_PATH + entityType + "/" + requestId + "?layout=" + layoutString;
        HashMap<String, String> requestHeaders = new HashMap<String,String>();
        requestHeaders.put(REQUEST_CONTENT_TYPE, APPLICATION_JSON);
        requestHeaders.put(REQUEST_COOKIE_HEADER, SESSION_COOKIE_KEY + "=" + sessionId);

        return SmaxQueryResult.parse(LocalHttpClient.doGET(url, requestHeaders)).get(0);
    }

    private String generateLayoutString(List<String> propertyList) {
        String layout = "";
        for (String property : propertyList) {
            layout = layout + property + ",";
            logger.debug("layout String: " + layout);
        }
        layout = StringUtils.chop(layout);
        logger.debug("Layout string completed: " + layout);
        return layout;
    }
}