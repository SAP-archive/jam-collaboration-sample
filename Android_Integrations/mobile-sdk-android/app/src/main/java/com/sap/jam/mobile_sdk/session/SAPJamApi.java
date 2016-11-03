package com.sap.jam.mobile_sdk.session;

import android.util.Log;

import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1RequestToken;

public class SAPJamApi extends DefaultApi10a {

    private final String server;
    private final String authorizeUrl;
    private final String requestUrl;
    private final String accessTokenUrl;


    protected SAPJamApi(String server, String domain) {
        this.server = server;

        this.requestUrl = getServerUrl() + "/oauth/request_token";
        this.accessTokenUrl = getServerUrl() + "/oauth/access_token";

        if (domain != null) {
            this.authorizeUrl = getServerUrl() + "/c/" + domain + "/oauth/authorize";
        }
        else {
            // Creates authorization url for normal oauth clients
            this.authorizeUrl = getServerUrl() + "/oauth/authorize";
        }
    }

    String getServerUrl() {
        return "https://" + server;
    }

    public static SAPJamApi getInstanceForServer(String server, String domain) {
        return new SAPJamApi(server, domain);
    }

    @Override
    public String getAccessTokenEndpoint() {
        return accessTokenUrl;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return requestUrl;
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return authorizeUrl + "?oauth_token=" + requestToken.getToken();
    }

}
