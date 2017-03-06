package com.sap.jam.mobile_sdk;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.sap.jam.mobile_sdk.session.JamAuthConfig;
import com.sap.jam.mobile_sdk.session.JamOAuthDialog;
import com.sap.jam.mobile_sdk.views.JamMainTabbedActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.auth_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginOAuthLogin();
            }
        });

        // Uses Key and Secret from your SAP Cloud Platform Jam instance's OAuth Client to perform OAuth 1.0a authentication
        JamAuthConfig.instance().configure("developer.sapjam.com",
                "<Oauth client key>",
                "<Oauth client secret>");

        // Uses Key and Secret from your Jam instance's OAuth Client to perform OAuth 1.0a authentication
        /*JamAuthConfig.instance().configure("<domain>",
                "<Oauth client key>",
                "<Oauth client secret>");*/

        if (JamAuthConfig.instance().isLoggedIn()) {
            goToMainApp();
        }
        else {
            beginOAuthLogin();
        }
    }

    private void goToMainApp() {
        Intent intent = new Intent(this, JamMainTabbedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void beginOAuthLogin() {
        final OAuth10aService service = JamAuthConfig.instance().getOAuth10aService();

        AsyncTask network = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                return service.getRequestToken();
            }

            @Override
            protected void onPostExecute(Object object) {
                super.onPostExecute(object);

                if (object != null) {
                    final OAuth1RequestToken requestToken = (OAuth1RequestToken) object;
                    String authUrl = service.getAuthorizationUrl(requestToken);

                    JamOAuthDialog dialog = new JamOAuthDialog(MainActivity.this, authUrl);
                    dialog.oauthListener = new JamOAuthDialog.ConfirmedOAuthAccessListener() {
                        @Override
                        public void onFinishOAuthAccess(String oauthToken, String oauthVerifier) {
                            processOAuthVerifier(requestToken, oauthVerifier);
                        }
                    };
                    dialog.show();
                }
            }
        };

        network.execute();
    }

    private void processOAuthVerifier(final OAuth1RequestToken requestToken, final String oauthVerifier) {
        final OAuth10aService service = JamAuthConfig.instance().getOAuth10aService();

        AsyncTask network = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, oauthVerifier);

                JamAuthConfig.instance().storeCredentials(accessToken);

                return true;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                goToMainApp();
            }
        };
        network.execute();
    }
}
