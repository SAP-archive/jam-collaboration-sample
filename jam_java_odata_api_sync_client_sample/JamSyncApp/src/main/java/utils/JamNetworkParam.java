package utils;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class JamNetworkParam {
    private StringBuilder paramBuilder = null;
    HashMap<String, String> params = new HashMap<String, String>();

    public void add(final String key, final String value) {
        params.put(key, value);

        if (paramBuilder == null) {
            paramBuilder = new StringBuilder();
        } else {
            paramBuilder.append("&");
        }

        paramBuilder.append(key).append("=").append(value);
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public JSONObject toJson() {
        JSONObject jsonObj = new JSONObject();
        for (final Map.Entry<String, String> entry : params.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            jsonObj.put(key, value);
        }
        return jsonObj;
    }

    @Override
    public String toString() {
        return paramBuilder.toString();
    }

}
