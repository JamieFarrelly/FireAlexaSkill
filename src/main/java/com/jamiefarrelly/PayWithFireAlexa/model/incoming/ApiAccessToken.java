package com.jamiefarrelly.PayWithFireAlexa.model.incoming;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * API returns more than just the access token, but that's all we care about in this case
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiAccessToken {
    
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
