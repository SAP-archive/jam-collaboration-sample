package com.sap.jam.mobile_sdk.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.sap.jam.mobile_sdk.JamSDKDemoApp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JamAuthConfig {

    public static final String OAUTH_CALLBACK = "sapjamsdk://oauth-callback/jam";
    public static final String KEY_OAUTH_TOKEN = "oauth_token";
    public static final String KEY_OAUTH_VERIFIER = "oauth_verifier";

    private static final String PREF_KEY = "jamAuth";
    private static final String PREF_ACCESS_TOKEN = "accessToken";
    private static final String PREF_ACCESS_SECRET = "accessSecret";

    private static final Pattern SINGLE_USE_TOKEN_PATTERN = Pattern.compile("(?<=<single_use_token id=\")[^\"]+");

    private OAuth10aService oauthService;
    private OAuth1AccessToken oauthToken;

    private static class Holder {
        private static final JamAuthConfig sharedInstance = new JamAuthConfig();
    }

    public static JamAuthConfig instance() {
        return Holder.sharedInstance;
    }

    public boolean isLoggedIn() {
        return oauthToken != null;
    }

    public OAuth1AccessToken getOAuth10aAccessToken() {
        return oauthToken;
    }

    public OAuth10aService getOAuth10aService() {
        return oauthService;
    }

    // Configures the URLs required to perform OAuth 1.0a authentication and authorization to get a single use
    // access token.
    // Use this method for normal oauth clients
    public void configure(String server, String consumerKey, String consumerSecret) {
        configure(server, consumerKey, consumerSecret, null);
    }

    public void configure(String server, String consumerKey, String consumerSecret, String companyDomain) {
        oauthService = new ServiceBuilder()
                .apiKey(consumerKey)
                .apiSecret(consumerSecret)
                .callback(OAUTH_CALLBACK)
        .build(SAPJamApi.getInstanceForServer(server, companyDomain));

        oauthToken = null;

        // Load Oauth tokens if exist
        Context appContext = JamSDKDemoApp.getAppContext();
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_KEY, 0);
        if (prefs.contains(PREF_ACCESS_TOKEN) && prefs.contains(PREF_ACCESS_SECRET)) {
            oauthToken = new OAuth1AccessToken(prefs.getString(PREF_ACCESS_TOKEN, null), prefs.getString(PREF_ACCESS_SECRET, null));
        }
    }

    public void storeCredentials(OAuth1AccessToken accessToken) {
        this.oauthToken = accessToken;

        // TODO: store access token and secret in AccountManager or more securely
        // Using SharedPreferences for demonstration
        Context appContext = JamSDKDemoApp.getAppContext();
        SharedPreferences.Editor prefEditor = appContext.getSharedPreferences(PREF_KEY, 0).edit();
        prefEditor.putString(PREF_ACCESS_TOKEN, accessToken.getToken());
        prefEditor.putString(PREF_ACCESS_SECRET, accessToken.getTokenSecret());
        prefEditor.commit();
    }

    public String getServerUrl() {
        return ((SAPJamApi)oauthService.getApi()).getServerUrl();
    }

    // Must do this on background thread
    public String getSingleUseToken() {
        OAuth10aService service = JamAuthConfig.instance().getOAuth10aService();
        final OAuthRequest request = new OAuthRequest(Verb.POST,
                JamAuthConfig.instance().getServerUrl() + "/v1/single_use_tokens",
                service);
        service.signRequest(JamAuthConfig.instance().getOAuth10aAccessToken(), request);

        final Response response = request.send();
        String body = response.getBody();

        Matcher matcher = SINGLE_USE_TOKEN_PATTERN.matcher(body);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

}
