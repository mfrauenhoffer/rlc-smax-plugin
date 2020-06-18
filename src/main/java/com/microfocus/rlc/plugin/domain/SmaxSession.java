package com.microfocus.rlc.plugin.domain;

public class SmaxSession {

    private String sessionId;

    public SmaxSession(String session) {
        sessionId = session;
    }
    
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String session) {
        this.sessionId = session;
    }
}