package com.sap.jam.mobile_sdk.views;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.sap.jam.mobile_sdk.R;
import com.sap.jam.mobile_sdk.session.JamAuthConfig;

public class JamFeedWidgetFragment extends Fragment {

    private WebView webView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_jam_feed_widget, container, false);
        webView = (WebView) rootView.findViewById(R.id.feed_webview);
        webView.getSettings().setJavaScriptEnabled(true);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AsyncTask network = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {

                String token = JamAuthConfig.instance().getSingleUseToken();
                return token;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                String token = (String) o;
                if (token != null) {
                    String url = JamAuthConfig.instance().getServerUrl() + "/widget/v1/feed?single_use_token=" + token;
                    webView.loadUrl(url);
                }

            }
        };

        network.execute();
    }

}
