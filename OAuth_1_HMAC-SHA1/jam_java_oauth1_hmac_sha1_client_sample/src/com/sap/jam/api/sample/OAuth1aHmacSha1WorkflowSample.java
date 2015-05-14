package com.sap.jam.api.sample;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.sap.jam.oauth.client.HttpMethod;
import com.sap.jam.oauth.client.OAuthClientHelper;
import com.sap.jam.oauth.client.Pair;
import com.sap.jam.oauth.client.SignatureMethod;


/**
 * This class provides some working Java sample client code to illustrate authentication of the SAP Jam API
 * with OAuth1.0 and the HMAC-SHA1 signature type.
 * 
 * This example assumes a Jam deployment with an OAuth client application configured for a company,
 * and a user that can successfully log into the company via the Web UI to authorize the OAuth client to make
 * API requests on behalf of the user.
 * 
 * It is run by invoking the "main" method.
 * There are 3 command line arguments: <baseUrl> <consumerKey> <consumerSecret>
 * For example: https://integration3.sapjam.com Ywkxz2QhRQiTBhAJihFW OUaMDZOxmRC1s0i23e3R00y0zoag1WEtN6WJuB7w
 * 
 * Depending on the environment, a proxy can be configured using VM arguments supplied to the jvm. For example,
 * for the environment where this program was developed these are:
 * -Dhttp.proxyHost=proxy.van.sap.corp
 * -Dhttp.proxyPort=8080
 * -Dhttps.proxyHost=proxy.van.sap.corp
 * -Dhttps.proxyPort=8080
 * 
 */

public class OAuth1aHmacSha1WorkflowSample {
	

    public static void main (String[] args) throws Exception {
    	
    	if (args.length != 3) {
    		System.out.println("Command line arguments: <baseUrl> <consumerKey> <consumerSecret>");
    		System.out.println("For example: https://integration3.sapjam.com Ywkxz2QhRQiTBhAJihFW OUaMDZOxmRC1s0i23e3R00y0zoag1WEtN6WJuB7w");
    		System.exit(1);
    	}
    	
    	System.out.println("Proxy settings:");
    	System.out.println("http.proxyHost="+System.getProperty("http.proxyHost"));
    	System.out.println("http.proxyPort="+System.getProperty("http.proxyPort"));
    	System.out.println("https.proxyHost="+System.getProperty("https.proxyHost"));
    	System.out.println("https.proxyPort="+System.getProperty("https.proxyPort"));
 	        	
    	String baseUrl = args[0];
    	String consumerKey = args[1];
    	String consumerSecret = args[2];
    	
    	runOAuth1aFlows(baseUrl, consumerKey, consumerSecret);
    }
    
    
    private static void runOAuth1aFlows(String baseUrl, String consumerKey, String consumerSecret) throws Exception {
    	
    	System.out.println("\n***************************************************************");
    	
    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    	Date date = new Date();
    	
    	System.out.println("SAP Jam OAuth1a flows run: " + dateFormat.format(date));
    	     
    	Pair<String, String> requestTokenPair = postOAuth1aRequestToken(baseUrl, consumerKey, consumerSecret);
    	String requestToken = requestTokenPair.fst();
    	String requestTokenSecret = requestTokenPair.snd();
    	
    	String verifier = authorizeRequestToken(baseUrl, requestToken);
    	
    	Pair<String, String> accessTokenPair = postOAuth1aAccessToken(baseUrl, consumerKey, consumerSecret, requestToken, requestTokenSecret, verifier);
    	String accessToken = accessTokenPair.fst();
    	String accessTokenSecret = accessTokenPair.snd();
    	
    	getCurrentUser(baseUrl, consumerKey, consumerSecret, accessToken, accessTokenSecret);
    	
    	postOAuth1aRevokeAccessToken(baseUrl, consumerKey, consumerSecret, accessToken, accessTokenSecret);
    	
    	//after the access token is revoked, calling an API with it will result in an authentication error
    	getCurrentUser(baseUrl, consumerKey, consumerSecret, accessToken, accessTokenSecret);  
    }
    
    /**
     * Creates the OAuth1.0 request token
     * POST /oauth/request_token
     */
    private static Pair<String,String> postOAuth1aRequestToken(String baseUrl, String consumerKey, String consumerSecret) throws Exception {
    		
    	System.out.println("\n***************************************************************");
    	String urlString = baseUrl + "/oauth/request_token";  
    	System.out.println("POST " + urlString);
    	
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.POST);
        och.setRequestUrl(new URL(urlString));
        och.setConsumerKey(consumerKey);
        och.setConsumerSecret(consumerSecret);
        //must set either a callback URL or indicate that the callback is out of band
        och.setOutOfBandCallback();
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
       
        StringBuilder result = new StringBuilder();
        
        HttpURLConnection connection = createConnection(och.getRequestUrl());
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
   
        String authorizationHeader = och.generateAuthorizationHeader ();                
    	connection.setRequestProperty("Authorization", authorizationHeader);
    	System.out.println("Authorization: " + authorizationHeader);
   

        int responseCode = connection.getResponseCode();
        System.out.println("HTTP response code: " + responseCode);
        InputStream is;
        if (connection.getResponseCode() >= 400) {
        	is = connection.getErrorStream();            
        } else {
        	is = connection.getInputStream();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        while ((inputLine = in.readLine()) != null) { 
            result.append(inputLine);
        }
        in.close();
        
        String resultString = result.toString();
        System.out.println("Response body: " + resultString);
        
        String[] tokenAndSecret = resultString.split("&");
        String requestToken = tokenAndSecret[0].split("=")[1];
        String requestTokenSecret = tokenAndSecret[1].split("=")[1];
        System.out.println("requestToken: " + requestToken);
        System.out.println("requestTokenSecret: " + requestTokenSecret);
        return new Pair<String, String>(requestToken, requestTokenSecret);            
    }
    
    private static String authorizeRequestToken(String baseUrl, String requestToken) throws Exception {
   	    System.out.println("\n***************************************************************");
    	
    	System.out.println("Paste the following link into your browser: " + baseUrl + "/oauth/authorize?oauth_token=" + requestToken);
    	System.out.println("Authorize the token, then enter the verification token here: ");
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	String verifier = br.readLine().trim(); 	
    	System.out.println("verifier: " + verifier);
    	return verifier;    	
    }
    
    /**
     * Creates the OAuth1.0 access token from a request token and verifier
     * POST /oauth/access_token
     */
    private static Pair<String, String> postOAuth1aAccessToken(String baseUrl, String consumerKey, String consumerSecret, String requestToken, String requestTokenSecret, String verifier) throws Exception {
    	
    	System.out.println("\n***************************************************************");
    	String urlString = baseUrl + "/oauth/access_token";
    	System.out.println("POST " + urlString);
    	
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.POST);
        och.setRequestUrl(new URL(urlString));
        och.setConsumerKey(consumerKey);
        och.setConsumerSecret(consumerSecret);
        och.setToken(requestToken);
        och.setTokenSecret(requestTokenSecret);
        och.setVerifier(verifier);
        och.setOutOfBandCallback();
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
       
        StringBuilder result = new StringBuilder();
        
        HttpURLConnection connection = createConnection(och.getRequestUrl());
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
   
        String authorizationHeader = och.generateAuthorizationHeader ();                
    	connection.setRequestProperty("Authorization", authorizationHeader);
    	System.out.println("Authorization: " + authorizationHeader);
    	

        int responseCode = connection.getResponseCode();
        System.out.println("HTTP response code: " + responseCode);
        InputStream is;
        if (connection.getResponseCode() >= 400) {
        	is = connection.getErrorStream();            
        } else {
        	is = connection.getInputStream();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        while ((inputLine = in.readLine()) != null) { 
            result.append(inputLine);
        }
        in.close();
        
        String resultString = result.toString();
        System.out.println ("Response body: " + resultString);
        
        String[] tokenAndSecret = resultString.split("&");
        String accessToken = tokenAndSecret[0].split("=")[1];
        String accessTokenSecret = tokenAndSecret[1].split("=")[1];
        System.out.println("accessToken: " + accessToken);
        System.out.println("accessTokenSecret: " + accessTokenSecret);
        return new Pair<String, String>(accessToken, accessTokenSecret);                 
    } 
    
    /**
     * Revokes the access token so that it cannot be used for further API calls.
     * POST /oauth/revoke_token
     */    
    private static void postOAuth1aRevokeAccessToken(String baseUrl, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) throws Exception {
    	
    	System.out.println("\n***************************************************************");
    	String urlString = baseUrl + "/oauth/revoke_token";
    	System.out.println("POST " + urlString);
    	
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.POST);
        och.setRequestUrl(new URL(urlString));
        och.setConsumerKey(consumerKey);
        och.setConsumerSecret(consumerSecret);
        och.setToken(accessToken);
        och.setTokenSecret(accessTokenSecret);
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
       
        StringBuilder result = new StringBuilder();
        
        HttpURLConnection connection = createConnection(och.getRequestUrl());
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
   
        String authorizationHeader = och.generateAuthorizationHeader ();                
    	connection.setRequestProperty("Authorization", authorizationHeader);
    	System.out.println("Authorization: " + authorizationHeader);
    	

        int responseCode = connection.getResponseCode();
        System.out.println("HTTP response code: " + responseCode);
        InputStream is;
        if (connection.getResponseCode() >= 400) {
        	is = connection.getErrorStream();            
        } else {
        	is = connection.getInputStream();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        while ((inputLine = in.readLine()) != null) { 
            result.append(inputLine);
        }
        in.close();
        
        String resultString = result.toString();
        System.out.println ("Response body: " + resultString);                
    }     
    
    /**
     * Calls the GET /api/v1/OData/Self endpoint using OAuth 1.0 with HMAC-SHA1 requesting a JSON response.
     * This endpoint provides information about the current user.
     */
    private static String getCurrentUser(String baseUrl, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) throws Exception {
    	
    	System.out.println("\n***************************************************************");
    	String urlString = baseUrl + "/api/v1/OData/Self";
    	System.out.println("GET " + urlString);
    	
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        och.setRequestUrl(new URL(urlString));
        och.setConsumerKey(consumerKey);
        och.setConsumerSecret(consumerSecret);
        och.setToken(accessToken);
        och.setTokenSecret(accessTokenSecret);
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
           
        StringBuilder result = new StringBuilder();
        
        HttpURLConnection connection = createConnection(och.getRequestUrl());
        connection.setRequestMethod("GET");
       
        String authorizationHeader = och.generateAuthorizationHeader ();               
        connection.setRequestProperty("Authorization", authorizationHeader);
        System.out.println("Authorization: " + authorizationHeader);
        connection.setRequestProperty("Accept", "application/json");   
        
        int responseCode = connection.getResponseCode();
        System.out.println("HTTP response code: " + responseCode);
        InputStream is;
        if (connection.getResponseCode() >= 400) {
        	is = connection.getErrorStream();            
        } else {
        	is = connection.getInputStream();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        while ((inputLine = in.readLine()) != null) { 
            result.append(inputLine);
        }
        in.close();
        
        String resultString = result.toString();
        System.out.println ("Response body: " + resultString);
        
        return resultString;        
    }    
    
    
    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }
    
    private static HttpURLConnection createConnection(URL requestUrl) throws Exception {
	    HttpURLConnection connection;	    
	    if (requestUrl.getProtocol().equals("https")) {
	        //http://stackoverflow.com/questions/1828775/httpclient-and-ssl
	        //Nice trick (for non-production code) to create a SSL Context that accepts any cert.
	        //This lets us avoid configuring any self-signed certificate from a test system.
	        SSLContext ctx = SSLContext.getInstance("TLS");
		        ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
		        SSLContext.setDefault(ctx);
		        
		        connection = (HttpsURLConnection)requestUrl.openConnection();		        
		        ((HttpsURLConnection )connection).setHostnameVerifier(new HostnameVerifier() {
		            @Override
		            public boolean verify(String arg0, SSLSession arg1) {
		                return true;
		            }
		        });
	    } else {
	    	connection = (HttpURLConnection)requestUrl.openConnection();
	    }
		return connection;
	}    
    
}



