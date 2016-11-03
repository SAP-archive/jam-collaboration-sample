package com.sap.jam.mobile_sdk.session;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;

import com.sap.jam.mobile_sdk.R;

import android.util.Log;

public class JamOAuthDialog extends Dialog {
    private WebView webView;
    private final String initialUrl;
    private WebSettings webSettings;

    private static final String TAG = "JamOAuthDialog";

    public ConfirmedOAuthAccessListener oauthListener = new ConfirmedOAuthAccessListener() { // dummy to avoid NPE
        @Override
        public void onFinishOAuthAccess(String oauthToken, String oauthVerifier) {
        }
    };

    public interface ConfirmedOAuthAccessListener {
        void onFinishOAuthAccess(String oauthToken, String oauthVerifier);
    }

    public JamOAuthDialog(Context context, String authUrl) {
        super(context);
        initialUrl = authUrl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.auth_dialog);
        setCancelable(true);

        webView = (WebView) findViewById(R.id.webview);

        // Wipe old web cache
        webView.clearCache(true);
        webView.clearFormData();
        webView.clearHistory();
        webView.clearMatches();
        webView.clearSslPreferences();

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "---------------");
                Log.d(TAG, "JamAuthConfig.OAUTH_CALLBACK = " + JamAuthConfig.OAUTH_CALLBACK);
                Log.d(TAG, "url = " + url);
                Log.d(TAG, "---------------");

                if (url.startsWith(JamAuthConfig.OAUTH_CALLBACK)) {
                    Uri uri = Uri.parse(url);
                    String oauthToken = uri.getQueryParameter(JamAuthConfig.KEY_OAUTH_TOKEN);
                    String oauthVerifier = uri.getQueryParameter(JamAuthConfig.KEY_OAUTH_VERIFIER);

                    Log.d(TAG, "oauthToken = " + oauthToken);
                    Log.d(TAG, "oauthVerifier = " + oauthVerifier);

                    if (oauthToken != null && oauthVerifier != null) {
                        oauthListener.onFinishOAuthAccess(oauthToken, oauthVerifier);
                    }

                    JamOAuthDialog.this.dismiss();
                }
            }
        });

        webView.loadUrl(initialUrl);
    }
}
