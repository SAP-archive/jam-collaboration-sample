package com.sap.jam.oauth.client;

import static com.sap.jam.oauth.client.OAuthUtils.OAUTH_CALLBACK;
import static com.sap.jam.oauth.client.OAuthUtils.OAUTH_CONSUMER_KEY;
import static com.sap.jam.oauth.client.OAuthUtils.OAUTH_NONCE;
import static com.sap.jam.oauth.client.OAuthUtils.OAUTH_SIGNATURE;
import static com.sap.jam.oauth.client.OAuthUtils.OAUTH_SIGNATURE_METHOD;
import static com.sap.jam.oauth.client.OAuthUtils.OAUTH_TIMESTAMP;
import static com.sap.jam.oauth.client.OAuthUtils.OAUTH_TOKEN;
import static com.sap.jam.oauth.client.OAuthUtils.OAUTH_VERIFIER;
import static com.sap.jam.oauth.client.OAuthUtils.OAUTH_VERSION;
import static com.sap.jam.oauth.client.OAuthUtils.REALM_PARAM;

import java.net.URL;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


/**
 * Helper class to allow clients using any web framework to make calls to OAuth1.0a providers.
 * It is especially designed to be compatible with the OAuth provider provided by Jam.
 * 
 * Supports:
 * <ol>
 *   <li> regular three-legged OAuth calls 
 *   <li> two-legged OAuth calls
 *   <li> obtaining a request token POST /oauth/request_token
 *   <li> obtaining an access token POST /oauth/access_token
 * </ol>
 * 
 * To use this class, create a new OAuthClientHelper object. Then call the builder methods of this class to set the properties required for the call you
 * are trying to make, and then call the generateAuthorizationHeader method to generate the text to set as the Authorization header
 * value in making your actual OAuth request. The JUnit tests in OAuthClientHelperTest show you how to set up the calls in each of the
 * supported cases.
 * 
 * The PLAINTEXT signature method is not recommended for production use.
 * Where the client can keep the consumer secret a true secret then the RSA-SHA1 signature method is recommended.
 * Where the client cannot keep the consumer secret a true secret (but can keep the token secret a true secret per user) then the HMAC-SHA1 signature method is recommended.
 * 
 * It is also a good idea to not explicitly set the timestamp and nonce (i.e. don't call setTimestamp or setNonce) and use the defaults provided.
 * 
 * This library also supports setting the OAuth parameters in the request body or query string, but these are not recommended by the OAuth spec.
 */
public final class OAuthClientHelper {
    
    private HttpMethod httpMethod;
    
    /** 
     * The targeted endpoint, including any query parameters but not including any OAuth protocol parameters
     * if the OAuth request parameters are generated for the query string.
     * For example, https://example.com/v1/activities?page_size=10
     */
    private URL requestUrl;  
    
    /** Optional. */
    private String realm;
    
    /** Required. Identifies the OAuth client to the OAuth provider. */
    private String consumerKey;
    
    /** Only required and used for PLAINTEXT and HMAC-SHA1 signature types. Otherwise can leave null. */
    private String consumerSecret;
    
    /** Only required and used for RSA-SHA1 signature types. */
    private PrivateKey consumerPrivateKey;
    
    /** In the case of two-legged OAuth, should be set to the empty String "", and not null.*/
    private String token;
    
    /** Not used in the case of the RSA-SHA1 signature type, even if the token is used. */
    private String tokenSecret;
    
    private SignatureMethod signatureMethod;
    
    /** Unix time in seconds. If unset or <=0, OAuth parameter generation will use the current Unix time.*/
    private long timestamp;
    
    /** If null, OAuth parameter generation will use a randomly generated uuid. */
    private String nonce;
    
    /** 
     * The default behavior is to include the oauth_version parameter with its only legal value "1.0". Set to null
     * to omit the oauth_version parameter.
     */
    private String oauthVersion = "1.0";
    
    /**  Only used for POST /oauth/access_token requests. Otherwise leave null. */
    private String verifier;
    
    /** Only used for POST /oauth/request_token requests. A URL or "oob" for out of band. Otherwise leave null. */
    private String callback;
    
    /** 
     * Extra key-value pairs to be added to the Authorization header along with the OAuth parameters. Do not encode the keys or values.
     * For use only when the OAuth parameters are to be included in the Authorization header.
     */
    private List<Pair<String, String>> extraAuthorizationHeaderParams; 
    
    /** 
     * For use only when request has Content-Type application/x-www-form-urlencoded. In this case, the request body parameters
     * are included in forming the OAuth signature when using the HMAC-SHA1 or RSA-SHA1 signature types. In particular,
     * this field does not need to be set for the PLAINTEXT signature type, or when the request does not have the above
     * mentioned Content-Type header value.
     * Do not encode the keys or values.
     * Do not include OAuth protocol parameters.
     */
    private List<Pair<String, String>> extraRequestBodyParams;
    
    public OAuthClientHelper(){
        
    }
    
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }
    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public URL getRequestUrl() {
        return requestUrl;
    }
    /** 
     * @param requestUrl The targeted endpoint, including any query parameters but not including any OAuth protocol parameters
     * if the OAuth request parameters are generated for the query string.
     * For example, https://example.com/v1/activities?page_size=10
     */    
    public void setRequestUrl(URL requestUrl) {
        this.requestUrl = requestUrl;
    }
    
    public String getRealm() {
        return realm;
    }
    /**
     * @param realm Optional.
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }
    
    public String getConsumerKey() {
        return consumerKey;
    }
    /**
     * @param consumerKey Required. Identifies the OAuth client to the OAuth provider.
     */
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }    

    public String getConsumerSecret() {
        return consumerSecret;
    }
    /**
     * @param consumerSecret Only required and used for PLAINTEXT and HMAC-SHA1 signature types. Otherwise can leave null.
     */
    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public PrivateKey getConsumerPrivateKey() {
        return consumerPrivateKey;
    }
    /**
     * @param consumerPrivateKey Only required and used for RSA-SHA1 signature types.
     */
    public void setConsumerPrivateKey(PrivateKey consumerPrivateKey) {
        this.consumerPrivateKey = consumerPrivateKey;
    }
    
    public String getToken() {
        return token;
    }
    /**
     * @param token In the case of two-legged OAuth, should be set to the empty String "", and not null.
     */
    public void setToken(String token) {
        this.token = token;
    }
    public void setTokenForTwoLeggedOAuth() {
        setToken("");
    }    

    public String getTokenSecret() {
        return tokenSecret;
    }
    /**
     * @param tokenSecret Not used in the case of the RSA-SHA1 signature type, even if the token is used.
     */
    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }     

    public SignatureMethod getSignatureMethod() {
        return signatureMethod;
    }
    public void setSignatureMethod(SignatureMethod signatureMethod) {
        this.signatureMethod = signatureMethod;
    }

    public long getTimestamp() {
        return timestamp;
    }
    /**
     * @param timestamp Unix time in seconds. If unset or <=0, OAuth parameter generation will use the current Unix time.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonce() {
        return nonce;
    }
    /**
     * @param nonce If null, OAuth parameter generation will use a randomly generated uuid. 
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getOauthVersion() {
        return oauthVersion;
    } 
    /**
     * @param oauthVersion The default behavior is to include the oauth_version parameter with its only legal value "1.0". Set to null
     * to omit the oauth_version parameter.
     */
    public void setOauthVersion(String oauthVersion) {
        this.oauthVersion = oauthVersion;
    }
    public void omitOAuthVersion() {
        setOauthVersion(null);
    }
 
    public String getVerifier() {
        return verifier;
    }
    /**  Only used for POST /oauth/access_token requests. Otherwise leave null. */
    public void setVerifier(String verifier) {
        this.verifier = verifier;
    }

    public String getCallback() {
        return callback;
    }
    /**
     * @param callback Only used for POST /oauth/request_token requests. A URL or "oob" for out of band. Otherwise leave null.
     */
    public void setCallback(String callback) {
        this.callback = callback;
    }
    public void setCallbackUrl(URL callbackUrl) {
        setCallback(callbackUrl.toString());
    }
    public void setOutOfBandCallback(){
        setCallback("oob");
    }

    public List<Pair<String, String>> getExtraAuthorizationHeaderParams() {
        return extraAuthorizationHeaderParams;
    }
    /**
     * @param extraAuthorizationHeaderParams Extra key-value pairs to be added to the Authorization header along with
     * the OAuth parameters. Do not encode the keys or values.
     * For use only when the OAuth parameters are to be included in the Authorization header.
     */
    public void setExtraAuthorizationHeaderParams(List<Pair<String, String>> extraAuthorizationHeaderParams) {
        this.extraAuthorizationHeaderParams = extraAuthorizationHeaderParams;
    }
    
    public List<Pair<String, String>> getExtraRequestBodyParams() {
        return extraRequestBodyParams;
    }
    /**
     * @param extraRequestBodyParams For use only when request has Content-Type application/x-www-form-urlencoded. 
     * In this case, the request body parameters
     * are included in forming the OAuth signature when using the HMAC-SHA1 or RSA-SHA1 signature types. In particular,
     * this field does not need to be set for the PLAINTEXT signature type, or when the request does not have the above
     * mentioned Content-Type header value.
     * Do not encode the keys or values.
     * Do not include OAuth protocol parameters.
     */
    public void setExtraRequestBodyParams(List<Pair<String, String>> extraRequestBodyParams) {
        this.extraRequestBodyParams = extraRequestBodyParams;
    }    
    
    /**
     * Generate info for the OAuth request in the case where the OAuth parameters are to be included in the Authorization headers.
     * This is the recommended location for OAuth parameters. (http://tools.ietf.org/html/rfc5849#section-3.5)
     * 
     * @return text to set as the Authorization header value in making your actual OAuth request. The JUnit tests in OAuthClientHelperTest show you
     *     how to set up the calls in each of the supported cases.
     */
    public String generateAuthorizationHeader() {
        
        verifyPreconditions();
       
        String nonce = this.nonce == null ? UUID.randomUUID().toString() : this.nonce;
        long timestamp = this.timestamp <= 0 ? System.currentTimeMillis()/1000L : this.timestamp;
      
        String signature = generateSignature(nonce, timestamp);
        
        StringBuilder sb = new StringBuilder();
        sb.append("OAuth ");
        if (realm != null) {
            sb.append(REALM_PARAM).append("=\"").append(OAuthUtils.oauthEncode(realm)).append("\", ");
        }
        sb.append(OAUTH_CONSUMER_KEY).append("=\"").append(OAuthUtils.oauthEncode(consumerKey)).append("\", ");
        if (token != null) {
            sb.append(OAUTH_TOKEN).append("=\"").append(OAuthUtils.oauthEncode(token)).append("\", ");
        }         
        sb.append(OAUTH_SIGNATURE_METHOD).append("=\"").append(signatureMethod.toString()).append("\", ");
        sb.append(OAUTH_SIGNATURE).append("=\"").append(OAuthUtils.oauthEncode(signature)).append("\", ");
        sb.append(OAUTH_TIMESTAMP).append("=\"").append(timestamp).append("\", ");
        sb.append(OAUTH_NONCE).append("=\"").append(OAuthUtils.oauthEncode(nonce)).append('"');
        if (oauthVersion != null) { 
            sb.append(", ").append(OAUTH_VERSION).append("=\"").append(OAuthUtils.oauthEncode(oauthVersion)).append('"');
        }
        if (callback != null) {
            //must be a POST /oauth/request_token request
            sb.append(", ").append(OAUTH_CALLBACK).append("=\"").append(OAuthUtils.oauthEncode(callback)).append('"');
        }
        if (verifier != null) {
            sb.append(", ").append(OAUTH_VERIFIER).append("=\"").append(OAuthUtils.oauthEncode(verifier)).append('"');
        }
        if (extraAuthorizationHeaderParams != null) {
            for (Pair<String, String> entry : extraAuthorizationHeaderParams) {
                sb.append(", ").append(OAuthUtils.oauthEncode(entry.fst())).append("=\"").append(OAuthUtils.oauthEncode(entry.snd())).append('"');
            }      
        }
        
        return sb.toString();
    }   
    
    /**
     * Generate info for the OAuth request in the case where the OAuth parameters are to be included in the request body.
     * This is the second-favored location for OAuth parameters. (http://tools.ietf.org/html/rfc5849#section-3.5)
     * 
     * @return escaped text to set as the request body value in making your actual OAuth request.
     */
    public String generateRequestBody() {
        return generateRequestQueryOrBody(false);       
    }             
    
    /**
     * Generate info for the OAuth request in the case where the OAuth parameters are to be included in the query string.
     * This is the least-favored location for OAuth parameters. (http://tools.ietf.org/html/rfc5849#section-3.5)
     * 
     * @return escaped text to set as the query string value in making your actual OAuth request.
     */
    public String generateRequestQuery() {
        return generateRequestQueryOrBody(true);
    }
    
    private String generateRequestQueryOrBody(boolean isQuery) {
        verifyPreconditions();
        
        String nonce = this.nonce == null ? UUID.randomUUID().toString() : this.nonce;
        long timestamp = this.timestamp <= 0 ? System.currentTimeMillis()/1000L : this.timestamp;
      
        String signature = generateSignature(nonce, timestamp);
        
        StringBuilder sb = new StringBuilder(); 
        if (isQuery) {
            String query = requestUrl.getQuery();
            if (query != null) {
                sb.append(query).append('&');
            }
        } else {
            if (extraRequestBodyParams != null) {
                for (Pair<String, String> entry : extraRequestBodyParams) {
                    sb.append(OAuthUtils.urlEncode(entry.fst())).append('=').append(OAuthUtils.urlEncode(entry.snd())).append('&');
                }      
            }           
        }
        
        sb.append(OAUTH_CONSUMER_KEY).append('=').append(OAuthUtils.urlEncode(consumerKey)).append('&');
        if (token != null) {
            sb.append(OAUTH_TOKEN).append('=').append(OAuthUtils.urlEncode(token)).append('&');
        }         
        sb.append(OAUTH_SIGNATURE_METHOD).append('=').append(signatureMethod.toString()).append('&');
        sb.append(OAUTH_SIGNATURE).append('=').append(OAuthUtils.urlEncode(signature)).append('&');
        sb.append(OAUTH_TIMESTAMP).append('=').append(timestamp).append('&');
        sb.append(OAUTH_NONCE).append('=').append(OAuthUtils.urlEncode(nonce));
        if (oauthVersion != null) { 
            sb.append('&').append(OAUTH_VERSION).append('=').append(OAuthUtils.urlEncode(oauthVersion));
        }
        if (callback != null) {
            //must be a POST /oauth/request_token request
            sb.append('&').append(OAUTH_CALLBACK).append('=').append(OAuthUtils.urlEncode(callback));
        }
        if (verifier != null) {
            sb.append('&').append(OAUTH_VERIFIER).append('=').append(OAuthUtils.urlEncode(verifier));
        }       
        
        return sb.toString();
    }    
    
    private void verifyPreconditions() {
        if (consumerKey == null) {
            throw new IllegalStateException("consumerKey should not be null.");
        }
        if (signatureMethod == null) {
            throw new IllegalStateException("signatureMethod should not be null.");
        }      
    }    
    
    /**
     * This method is used as part of the implementation of generateAuthorizationHeader,  generateRequestBody, and
     * generateRequestQuery that clients would normally call. 
     * It is useful as a public method mainly for debugging OAuth calls in the situation where the client developer
     * has debug access to the provider. In that case, a useful technique is to compare the signature base string at the 
     * client and provider side and see where they differ.
     */
    public String generateSignatureBaseString(String nonce, long timestamp) {
        
        if (signatureMethod == SignatureMethod.PLAINTEXT) {
            return OAuthUtils.baseOAuthSignature(consumerSecret, tokenSecret);        
        }
        
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Argument timestamp must be > 0.");
        }
        
        List<Pair<String, String>> signatureBaseParams = new ArrayList<Pair<String, String>>();
        signatureBaseParams.add(new Pair<String, String>(OAUTH_CONSUMER_KEY, OAuthUtils.oauthEncode(consumerKey)));
        signatureBaseParams.add(new Pair<String, String>(OAUTH_NONCE, OAuthUtils.oauthEncode(nonce)));
        signatureBaseParams.add(new Pair<String, String>(OAUTH_SIGNATURE_METHOD, signatureMethod.toString()));
        signatureBaseParams.add(new Pair<String, String>(OAUTH_TIMESTAMP, Long.toString(timestamp)));
        if (oauthVersion != null) {
            signatureBaseParams.add(new Pair<String, String>(OAUTH_VERSION, OAuthUtils.oauthEncode(oauthVersion)));
        }
        if (token != null) {
            signatureBaseParams.add(new Pair<String, String>(OAUTH_TOKEN, OAuthUtils.oauthEncode(token)));
        }
        if (callback != null) {
            signatureBaseParams.add(new Pair<String, String>(OAUTH_CALLBACK, OAuthUtils.oauthEncode(callback)));
        }
        if (verifier != null) {
            signatureBaseParams.add(new Pair<String, String>(OAUTH_VERIFIER, OAuthUtils.oauthEncode(verifier)));
        }
        String queryString = requestUrl.getQuery();
        if (queryString != null) {
            String[] queryParams = queryString.split("&");
            for (int i = 0, nQueryParams = queryParams.length; i < nQueryParams; ++i) {
                String queryParam = queryParams[i];
                int equalPos = queryParam.indexOf("=");
                Pair<String, String> encodedQueryParam;                
                if (equalPos == -1) {
                    encodedQueryParam = new Pair<String, String>(OAuthUtils.oauthEncode(OAuthUtils.urlDecode(queryParam)), "");
                } else {
                    encodedQueryParam = new Pair<String, String>(OAuthUtils.oauthEncode(OAuthUtils.urlDecode(queryParam.substring(0, equalPos))),
                        OAuthUtils.oauthEncode(OAuthUtils.urlDecode(queryParam.substring(equalPos + 1, queryParam.length()))));
                }
                signatureBaseParams.add(encodedQueryParam);                
            }
        }
        
        if (extraAuthorizationHeaderParams != null) {
            for (Pair<String, String> entry : extraAuthorizationHeaderParams) {
                signatureBaseParams.add(new Pair<String, String>(OAuthUtils.oauthEncode(entry.fst()), OAuthUtils.oauthEncode(entry.snd())));
            }
        }
        
        if (extraRequestBodyParams != null) {
            for (Pair<String, String> entry : extraRequestBodyParams) {
                signatureBaseParams.add(new Pair<String, String>(OAuthUtils.oauthEncode(entry.fst()), OAuthUtils.oauthEncode(entry.snd())));
            }
        }        
        
        //sort according to lexicographic order of the pairs. Note it is important to sort the pairs and not just the Strings
        //joined with an '=' as the sort order can differ. For example,
        //a1=3, a=2 sorts in OAuth order as a=2, a1=3, but as Strings it is a1=3, a=2 since '1' < '='.
        Collections.sort(signatureBaseParams, OAuthUtils.STRING_PAIR_COMPARATOR);
        
        return httpMethod.toString() + "&" + OAuthUtils.oauthEncode(OAuthUtils.baseStringUrl(requestUrl)) + "&" + OAuthUtils.oauthEncode(OAuthUtils.joinPostBodyParams(signatureBaseParams));
    }
     
    /**
     * This method is used as part of the implementation of generateAuthorizationHeader,  generateRequestBody, and
     * generateRequestQuery that clients would normally call. 
     * It is useful as a public method mainly for debugging OAuth calls in the situation where the client developer
     * has debug access to the provider.
     */
    public String generateSignature(String nonce, long timestamp) {
        
        String signatureBaseString = generateSignatureBaseString(nonce, timestamp);
        
        switch (signatureMethod) {
        case PLAINTEXT:
            return signatureBaseString;
        case HMAC_SHA1:                  
            return OAuthUtils.calculateHmacSha1Signature(signatureBaseString, OAuthUtils.baseOAuthSignature(consumerSecret, tokenSecret));
        case RSA_SHA1:
            return OAuthUtils.calculateRsaSha1Signature(signatureBaseString, consumerPrivateKey);
        default:
            throw new IllegalStateException("Unknown signatureMethod.");
        }
    }


}
