package utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;

public class JamNetworkResult {
    public InputStream inputStream = null;

    public JSONObject toJson() throws IOException {
        JSONObject jsonResult = null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        if (bufferedReader != null) {
            String resultStr = "";
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                resultStr += line;
            }
            bufferedReader.close();
            System.out.println("        JSON response: '" + resultStr + "'");
            jsonResult = new JSONObject(resultStr);
        }
        return jsonResult;
    }

}
