package utils;

import com.sap.jam.api.security.SignatureUtil;

import org.opensaml.xml.util.Base64;

import java.net.Proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.JamConfig;
import utils.JamNetworkManager;

public class JamTokenManager {
    final static String API_ODATA_TOKEN = "/api/v1/auth/token";

    private static JamTokenManager instance = new JamTokenManager();
    Map<String, Map<String, String>> hostTokenHashMap = new HashMap<String, Map<String, String>>();

    private JamTokenManager() {
    }

    public static JamTokenManager getInstance() {
        return instance;
    }

    public void setTokenFromConfig( JamConfig.ConfigInfo config, String memberEmail ) throws Exception {
        String token = getTokenForMember(config.host,
            config.proxy,
            memberEmail, 
            config.clientId,
            config.clientSecret,
            config.grantType,
            config.samlConfig);
        System.out.println("\n");

        JamNetworkManager.getInstance().SetOAuthToken(token);
        JamNetworkManager.getInstance().SetProxy(config.proxy);

    }
    public String retrieveFromOAuthToken(final String host, final Proxy proxy, final JamNetworkParam headerParam)
            throws UnsupportedEncodingException, MalformedURLException, IOException {
        final JamNetworkUrl url = new JamNetworkUrl(host + API_ODATA_TOKEN);

        System.out.println("       Retrieving oAuth via: " + url.toString());
        final HttpURLConnection connection = JamNetworkManager.getInstance().createConnection(url.toString(), "POST", proxy);

        connection.setDoOutput(true);
        final String requestBody = headerParam.toString();

        OutputStream output = null;

        try {
            output = connection.getOutputStream();
            output.write(requestBody.getBytes("UTF-8"));
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        final int responseCode = connection.getResponseCode();
        System.out.println("\n       HTTP response code: " + responseCode);
        InputStream is;
        if (connection.getResponseCode() >= 400) {
            is = connection.getErrorStream();
        } else {
            is = connection.getInputStream();
        }

        final StringBuilder result = new StringBuilder();

        final BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }
        in.close();

        final String resultString = result.toString();
        System.out.println("       Response body: " + resultString);

        final String oauthToken = resultString.split("\"")[3];
        System.out.println("       OAuthToken: " + oauthToken);

        return oauthToken;
    }

    // Get a token for a specific member using SAML Assertion info
    public String getTokenForMember(
        final String host, /* eg. https://abc.com */
        final Proxy proxy, /* eg. proxy.corp */
        final String userEmail /* eg. blue@cherry.com with password: password123*/,
        final String clientId, /* eg. eXvVdfco4Ih6ZlN5SWAu */
        final String clientSecret, /* eg. gKWU2vwPnb8d3fLQFkJTkFiGPLcx26VvA3EXxeWT */
        final String grantType, /* eg. client_credentials | urn:ietf:params:oauth:grant-type:saml2-bearer */
        final JamConfig.SamlConfigInfo samlConfig) throws Exception {

        // Source JAM => Get Access token from Source JAM
        final String urlString = host + API_ODATA_TOKEN;

        System.out.println("    Member[" + userEmail + "] Token => URL: " + urlString);

        // If token hashMap does not exist yet for the current host, create it
        if (!hostTokenHashMap.containsKey(host)) {
            final Map<String, String> tokenMap = new HashMap<String, String>();
            hostTokenHashMap.put(host, tokenMap);
        }

        // Check if token exist in Hash map, if so return it or make the url request with SAML assertion to retrieve it
        final Map<String, String> tokens = hostTokenHashMap.get(host);
        if (tokens.containsKey(userEmail)) {
            final String tokenString = tokens.get(userEmail);
            System.out.print("       Retrieving oAuth via cache: " + tokenString);
            return tokens.get(userEmail);
        }

        // Set up header for getting token
        final JamNetworkParam param = new JamNetworkParam();
        //param.add("grant_type", URLEncoder.encode(JamConfig.getInstance().getFromGrantType(), "UTF-8"));
        param.add("client_id", URLEncoder.encode(clientId, "UTF-8"));
        param.add("client_secret", URLEncoder.encode(clientSecret, "UTF-8"));
        param.add("grant_type", URLEncoder.encode(grantType, "UTF-8"));

        if (samlConfig != null && samlConfig.idpId != null) {
            final PrivateKey IDPPrivateKey = SignatureUtil.makePrivateKey(samlConfig.idpPrivateKey);

            // Set up attributes and assertion
            Map<String, List<Object>> attributes = null;
            attributes = new HashMap<String, List<Object>>();
            attributes.put("client_id", Collections.singletonList((Object)clientId));

            // Generate an assertion for a particular user
            final String samlAssertion = JamNetworkManager.getInstance().buildSignedSAML2Assertion(samlConfig.idpId,
                urlString,
                userEmail,
                "email",
                null,
                IDPPrivateKey,
                null,
                samlConfig.spId,
                attributes);
            System.out.println("       assertion:" + samlAssertion);

            String base64SamlAssertion = new String(Base64.encodeBytes(samlAssertion.getBytes(), Base64.DONT_BREAK_LINES));
            param.add("assertion", URLEncoder.encode(base64SamlAssertion, "UTF-8"));
        }

        // Get Token from URL request
        final String tokenString = retrieveFromOAuthToken(host, proxy, param);

        // Store the token in our token hash map cache for this host url call
        if (tokenString != null) {
            tokens.put(userEmail, tokenString);
        }

        return tokenString;
    }

}
