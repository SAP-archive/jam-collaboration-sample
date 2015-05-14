package com.sap.jam.oauth.client;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Comparator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Various static utility functions and constants for OAuth.
 */
public final class OAuthUtils {
    
    private OAuthUtils() {}    
    
    //the OAuth protocol parameters
    public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    public static final String OAUTH_TOKEN = "oauth_token";
    public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
    public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    public static final String OAUTH_SIGNATURE = "oauth_signature";
    public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    public static final String OAUTH_NONCE = "oauth_nonce";
    public static final String OAUTH_VERSION = "oauth_version";
    public static final String OAUTH_CALLBACK = "oauth_callback";
    public static final String OAUTH_VERIFIER = "oauth_verifier";  
    public static final String X_AUTH_MODE = "x_auth_mode";
    public static final String X_AUTH_USERNAME = "x_auth_username";
    public static final String X_AUTH_PASSWORD = "x_auth_password";
    public static final String X_AUTH_EXPIRES = "x_auth_expires";
    public static final String CLIENT_AUTH = "client_auth";
    
    //special param that may occur in Authorization headers that is treated specially in forming OAuth signatures
    public static final String REALM_PARAM = "realm";
    
    public static final Set<String> OAUTH_PROTOCOL_PARAMS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(OAUTH_CALLBACK, OAUTH_CONSUMER_KEY, OAUTH_TOKEN, 
        OAUTH_SIGNATURE_METHOD, OAUTH_TIMESTAMP, OAUTH_NONCE, OAUTH_VERIFIER, OAUTH_VERSION, OAUTH_SIGNATURE))); 
    
    
    public static final Comparator<Pair<String, String>> STRING_PAIR_COMPARATOR = new Comparator<Pair<String, String>>() {

        @Override
        public int compare(Pair<String, String> o1, Pair<String, String> o2) {
            int c1 = o1.fst().compareTo(o2.fst());
            if (c1 != 0) {
                return c1;
            }
            return o1.snd().compareTo(o2.snd());                      
        }
        
    };

    public static String calculateHmacSha1Signature(String message, String key) {
       try { 
           SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA1");
           Mac messageAuthnCode = Mac.getInstance("HmacSHA1");
           messageAuthnCode.init(secretKey);
           byte[] rawEncodedMessage = messageAuthnCode.doFinal(message.getBytes("UTF-8"));
           return Base64Util.encode(rawEncodedMessage);
       } catch(UnsupportedEncodingException e) {
           throw new IllegalStateException("UTF-8 encoding not supported", e); 
       } catch (InvalidKeyException e) {
           throw new IllegalStateException("Invalid secret key", e); 
       } catch (NoSuchAlgorithmException e) {
           throw new IllegalStateException("HmacSHA1 not supported", e); 
       }
    }

    public static String calculateRsaSha1Signature(String message, PrivateKey privateKey) {
        try {
            return SignatureUtil.generateBase64Signature(privateKey, message.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 charset supported for decoding the message argument.");
        }
    }
    
    public static boolean verifyRsaSha1Signature(PublicKey publicKey, String signatureBase64, String signatureBaseString) {
        try {
            return SignatureUtil.verifyBase64Signature(publicKey, signatureBase64, signatureBaseString.getBytes("UTF-8"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Encode according to the OAuth spec http://tools.ietf.org/html/rfc5849#section-3.6
     * Convert to UTF-8 octets and then % escape all characters other than a-z, A-Z, 0-9, -, ., _, ~.
     * This is slightly different from url-encoding.
     */
    public static String oauthEncode(String s) {
        if (s == null) {
            return "";
        }
        try {
            //URLEncoder doesn't encode *, encodes ~, and encodes spaces as + instead of %20
            return URLEncoder.encode(s, "UTF-8").replace("*", "%2A").replace("%7E", "~").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not supported", e);
        }
    }

    public static String oauthDecode(String s) {
        try {
            return URLDecoder.decode(s.replace("+", "%2B"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not supported", e);
        }
    }
    
    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not supported", e);
        }
    }    
    
    public static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not supported", e);
        }
    }
    
    public static String baseStringUrl(URL url) {
        boolean showPort = url.getPort() != -1 && (url.getProtocol().equals("http") && url.getPort() != 80 || url.getProtocol().equals("https") && url.getPort() != 443);
        return url.getProtocol() + "://" + url.getHost().toLowerCase(Locale.ROOT) + (showPort ? ":" + url.getPort() : "") +
            (url.getPath().isEmpty() ? "/" : url.getPath());
    }    

    public static String baseOAuthSignature(String consumerSecret, String tokenSecret) {
        return tokenSecret != null ? oauthEncode(consumerSecret) + "&" + oauthEncode(tokenSecret) : oauthEncode(consumerSecret) + "&";        
    }
    
    public static String joinPostBodyParams(List<Pair<String, String>> postParams)
    {
       StringBuilder sb = new StringBuilder();
       boolean first = true;
       for (Pair<String, String> item : postParams)
       {
          if (first) {
             first = false;
          } else {
             sb.append("&");
          }
          sb.append(item.fst()).append("=").append(item.snd());
       }
       return sb.toString();
    }    
}
