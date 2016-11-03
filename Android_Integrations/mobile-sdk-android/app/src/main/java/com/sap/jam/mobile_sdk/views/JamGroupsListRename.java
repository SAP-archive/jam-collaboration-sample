package com.sap.jam.mobile_sdk.views;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.gson.Gson;
import com.sap.jam.mobile_sdk.R;
import com.sap.jam.mobile_sdk.session.JamAuthConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JamGroupsListRename extends Fragment {

    private ListView listView;
    private List<String> groupNames;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_jam_groups_list, container, false);
        listView = (ListView) rootView.findViewById(R.id.listView);

        loadGroups();
        return rootView;
    }

    private void loadGroups() {

        AsyncTask network = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                OAuth10aService service = JamAuthConfig.instance().getOAuth10aService();
                final OAuthRequest request = new OAuthRequest(Verb.GET,
                        JamAuthConfig.instance().getServerUrl() + "/api/v1/OData/Groups?$select=Id,Name",
                        service);
                request.addHeader("Accept", "application/json");
                service.signRequest(JamAuthConfig.instance().getOAuth10aAccessToken(), request);

                final Response response = request.send();
                Log.w("TAG", response.getBody());

                Gson gson = new Gson();
                Map result = gson.fromJson(response.getBody(), Map.class);

                return result;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                Map result = (Map)o;
                List<Map> groups = (List) ((Map)result.get("d")).get("results");

                // extract the group names and refresh UI
                groupNames = new ArrayList<>();
                for (Map attr : groups) {
                    groupNames.add((String) attr.get("Name"));
                }

                updateList();
            }
        };

        network.execute();

    }

    private void updateList() {
        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_group, groupNames);
        listView.setAdapter(itemsAdapter);
    }


}
