package com.sap.jam.oauth.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.sap.jam.oauth.client.HttpMethod;
import com.sap.jam.oauth.client.OAuthUtils;
import com.sap.jam.oauth.client.SignatureMethod;
import com.sap.jam.oauth.client.SignatureUtil;
import com.sap.jam.oauth.client.Pair;

/**
 * This class tests OAuthClientHelper, but also gives examples on how you can use OAuthClientHelper to
 * generate OAuth calls to OAuth providers in the supported scenarios:
 * 
 * <ol>
 *   <li> regular three-legged OAuth calls 
 *   <li> two-legged OAuth calls
 *   <li> obtaining a request token POST /oauth/request_token
 *   <li> obtaining an access token POST /oauth/acces_token
 * </ol>
 */
public class OAuthClientHelperTest extends TestCase {
    
    private static String TEST_CONSUMER_PRIVATE_KEY_BASE64 = 
        "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDAHXL/VXv3X3B0"
        + "iK3S6CfUR12nMo60tZ/w2924GVtML5hEVkwSseYNxAI6Wg7edoPGWLAkgxccYYaq"
        + "5Ztl1rW90zLXbnj07/3lMqA0VlNkrU229wgqmcN8rdQhGatKjNgDQwf9EZAqByun"
        + "izpCzx9j8yttigcAJbkyG9SWrXMg+kTFzy8rlvFSE6wvu4b9lM3PDRYZKQEWH9Fx"
        + "70Dsve+rcQwijigpqouY0dNkq1v3DGBnaskO5FkhGcKtgQnIFe16IYsTZWrSa9xO"
        + "Tp5g09+bxmctQyiRo1ZCODquw+0VugPpUKL6n5bXc3pkO/nDupSbHRQDPvLuw5+M"
        + "uOHQMhBRAgMBAAECggEAD3FOkR+5+0R8saFa950ZtoKWEJ6LwhLnbGEADZBcOl+J"
        + "CdU2ADq0l16uHyqmD1PPe94dKpIvBWUBgjM6rpD3wu57I74JkwbHjejn380gnvMf"
        + "UBnDWklJ4XfHmXGKx09S/dAhkEIdKIOzCqCyajRj/ZDKaN+zeFuP62s5R3Nh0cCo"
        + "ntbT50bxUgFYbHQqBm1uG7uCenIw8/G+D1wE7EnWAxDhkKz3Z+tALXQMwXOSObMR"
        + "3xKpvVBdoknhsEDFiLTAeTdY80k2xJJX63awE+2m9vJ/oO1QJ6hS/IlRRVksFBUx"
        + "xuIQX29TDrdg6ttruEXkfNdUU0J7wt5dePKS6jBe0QKBgQDnkj+jgYPBcbwfvN2A"
        + "G6qbDZMSnQYV69vlPSgpsKkbN6R4LFSOU1ldkpm2fxqX/AnAkCoxjZzgC1DrF5Lj"
        + "THaZGPsPYD4+z0S5jyOO2QrbclcZGH60Kac5Ue5xCMju+FDT32Dcg5m2+isV0Hwm"
        + "Lb4qRWIK0M0QH2CZgdW79S5/vwKBgQDUYafngR3ZVKWSYxgGwayNq27TknUryk3u"
        + "37tqBq/VkyX2b/qeEg6yXRi1zUCAd+81BX0zAtRBjraSSECs7ECb0O2eXbbqB8OW"
        + "tFm8A4Weqm7Kg1MT8iqi00+IAyoobdKlcPdkbIqEBG3tHlOgFvoLu5hhIDseOZwn"
        + "tDDoMxpz7wKBgQDUtMtrvr1tZLEuXeb/k2okPlXHlSBP5nP3nzNTCAhtDbEvCVkp"
        + "2nmNr+ktbpbY4BN9I27+UVu1l+9d4it0SXnuqrmBqB/ExdIUt9wrjEWiyOYkERHh"
        + "HxmRVya0ASPADasA0oBATUlWf3gv8272CrJQVab21FBn7MpodpACs/VgNQKBgGpp"
        + "p9xaGF+dhvxLBG3WOqQdQoFlv6m6SsQme9wLD60edoX844CyIYUDGm91MR1teoCB"
        + "iJK3lyxl2lp/M1Cxa0nnLDgOUqj+TOTE5rVKuneeg7aaiFTVIMHGyPGoTEZHiKT2"
        + "bi52KA0vvvhxGot7F7wrOZVotdZWHUTThzHA8T8nAoGBALCstqjzATbt8CCMZhOR"
        + "Lkmr2Z/qmUcqiVooc+gh2+YFiM9r+MQY7R1CWIM99dEYvQBOel8Q7s+DTs0RYiFb"
        + "68FXoiiT8wdWdQiZKcg9LE3P8pudikf4WuhIXALy7ljWXogSFxQ/iZPaJX8K6cEM"
        + "GfhU91pNYJJmvyIuwzxJ7BWK";
    
    public static PrivateKey TEST_CONSUMER_PRIVATE_KEY = SignatureUtil.makePrivateKey(TEST_CONSUMER_PRIVATE_KEY_BASE64);
    
    private static String TEST_CONSUMER_CERTIFICATE_BASE64 = 
        "MIICzDCCAbQCCQClKBfhTDve7jANBgkqhkiG9w0BAQUFADAoMQswCQYDVQQGEwJD"
        + "QTELMAkGA1UECBMCQkMxDDAKBgNVBAoTA1NBUDAeFw0xMTA2MTAyMjM5MDhaFw0y"
        + "MTA2MDcyMjM5MDhaMCgxCzAJBgNVBAYTAkNBMQswCQYDVQQIEwJCQzEMMAoGA1UE"
        + "ChMDU0FQMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwB1y/1V7919w"
        + "dIit0ugn1EddpzKOtLWf8NvduBlbTC+YRFZMErHmDcQCOloO3naDxliwJIMXHGGG"
        + "quWbZda1vdMy12549O/95TKgNFZTZK1NtvcIKpnDfK3UIRmrSozYA0MH/RGQKgcr"
        + "p4s6Qs8fY/MrbYoHACW5MhvUlq1zIPpExc8vK5bxUhOsL7uG/ZTNzw0WGSkBFh/R"
        + "ce9A7L3vq3EMIo4oKaqLmNHTZKtb9wxgZ2rJDuRZIRnCrYEJyBXteiGLE2Vq0mvc"
        + "Tk6eYNPfm8ZnLUMokaNWQjg6rsPtFboD6VCi+p+W13N6ZDv5w7qUmx0UAz7y7sOf"
        + "jLjh0DIQUQIDAQABMA0GCSqGSIb3DQEBBQUAA4IBAQCepKNPU47CfeeY/UPAajTE"
        + "GZCaYh0IztK/lMyFC0tVtI8eB4Tih3d7dlV0/H3twXV11FvTCmr2QTpi6riQHPm8"
        + "OXUhi8uj7+ibJm+mKoh0nJW2BsEERJ/9VX7/HeLehB7EWDIGFnC/13XTol69m3hE"
        + "q1y8mIyU9u4WNTykVoGjge7W4re4ra/uYWz+CD2CuOI+Z8wY2fQqKq5RQ6q3mw4A"
        + "zynu3QO8znNoE3Ns8PVLvv1rb7Ayr4mNVANULWZTR+8jm9p7gRgbSkqKdQiQiygC"
        + "Z8G3X9udS479U0xvCeqLJvJwg5RT+RvNWYwvrKsTSgMXodv9D3D7oIngtURkpAHy";
    
    public static X509Certificate TEST_CONSUMER_CERTIFICATE = SignatureUtil.makeCertificate(TEST_CONSUMER_CERTIFICATE_BASE64);
    
    
    /**
     * Tests a call to a two-legged OAuth endpoint.
     */
    public void testTwoLeggedOAuthCall() throws MalformedURLException {
        
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        och.setRequestUrl(new URL("https://cheetah.dhcp.pgdev.sap.corp/oauth/test_consumer_request")); 
        och.setConsumerKey("1VMzOctCAidMaahS9yJU");
        och.setConsumerSecret("JinGkf4bjzFQhOVUbhxpL3eU1esKgO8qTAGfCXy7");
        och.setTokenForTwoLeggedOAuth();
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
        //under normal circumstances, we would not set the timestamp and nonce, and just let the library
        //use the current time and a generated uuid. However, we're comparing with a known good request
        och.setTimestamp(1307734348L);
        och.setNonce("thZmo2SoWA");
        
        String expectedAuthorizationHeader = "OAuth oauth_consumer_key=\"1VMzOctCAidMaahS9yJU\", oauth_nonce=\"thZmo2SoWA\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1307734348\", oauth_version=\"1.0\", oauth_token=\"\", oauth_signature=\"suUt%2B%2BCRsEp8WhSPApI83BtsBYk%3D\"";
        assertEqualAuthorizationHeaders(expectedAuthorizationHeader, och.generateAuthorizationHeader());
    }
    
    /**
     * Tests a call to the two-legged OAuth endpoint: GET /oauth/test_consumer_request using the RSA-SHA1 signature method.
     */  
    public void testTwoLeggedOAuthCallWithRsaSha1Signature() throws MalformedURLException {
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        och.setRequestUrl(new URL("https://cheetah.dhcp.pgdev.sap.corp/oauth/test_consumer_request")); 
        och.setConsumerKey("1VMzOctCAidMaahS9yJU");
        och.setConsumerPrivateKey(TEST_CONSUMER_PRIVATE_KEY);
        och.setTokenForTwoLeggedOAuth();
        och.setSignatureMethod(SignatureMethod.RSA_SHA1);
        //under normal circumstances, we would not set the timestamp and nonce, and just let the library
        //use the current time and a generated uuid. However, we're comparing with a known good request
        och.setTimestamp(1307748357L);
        och.setNonce("NtX9Gah3Bw");
        
        String expectedAuthorizationHeader = "OAuth oauth_consumer_key=\"1VMzOctCAidMaahS9yJU\", oauth_nonce=\"NtX9Gah3Bw\", oauth_signature_method=\"RSA-SHA1\", oauth_timestamp=\"1307748357\", oauth_version=\"1.0\", oauth_token=\"\", oauth_signature=\"h%2FKJEkVh3GdY%2BLN7G6gQazi625uwKGkxzyN3dcQh4LzyS2z%2FBLCcLiWL5u9Xkk%2FwxvIwvE6FcvWmYlPHxzNUPxjNfXkIo6CgfF2wAqDf09JLPuMlZPAKaj8n%2BFOTiswuOH%2BsxkCatN2ziUKsMqniYWHLxgT3Q9DI1Fve6tdGOuJO0H3Lg%2BzAIC8oWSWw4q6VPPauCbslJaZTA6d6v2yg2oMxBoLCnJ9x1F2C2B9Fqb3w0lkzDm5Vxz%2B%2BWgswLSXpIBQpfoqzZpE5qohBpq%2FT9KGMM8Ewj2hvzf0NSZtMRPvqpE5A4AFBxMnHlIHKnFLTxRqjAn2qgm1MY6wJhDDuoQ%3D%3D\"";
        assertEqualAuthorizationHeaders(expectedAuthorizationHeader, och.generateAuthorizationHeader());        
    }
    
    /**
     * Tests a call to an OAuth endpoint: GET /oauth/test_request.
     * This is a regular 3-legged OAuth call i.e. with an authenticated access token for a user.
     */
    public void testOAuthCall() throws MalformedURLException {
        
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        och.setRequestUrl(new URL("https://cheetah.dhcp.pgdev.sap.corp/oauth/test_request")); 
        och.setConsumerKey("1VMzOctCAidMaahS9yJU");
        och.setConsumerSecret("JinGkf4bjzFQhOVUbhxpL3eU1esKgO8qTAGfCXy7");
        och.setToken("qlbjCFbiBewXcCSAgIB9");
        och.setTokenSecret("fYtRQv54NygyJXUTzebgCopW3a5RTaBruvByh92g");
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
        //under normal circumstances, we would not set the timestamp and nonce, and just let the library
        //use the current time and a generated uuid. However, we're comparing with a known good request
        och.setTimestamp(1302731153L);
        och.setNonce("dd9YXogHSg");
        
        String expectedAuthorizationHeader = "OAuth oauth_consumer_key=\"1VMzOctCAidMaahS9yJU\", oauth_nonce=\"dd9YXogHSg\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1302731153\", oauth_version=\"1.0\", oauth_token=\"qlbjCFbiBewXcCSAgIB9\", oauth_signature=\"gJS7nzUTIbqDTEnel%2FYx8Cw%2B7q4%3D\"";
        assertEqualAuthorizationHeaders(expectedAuthorizationHeader, och.generateAuthorizationHeader());        
    }
    
    public void testOAuthCall2() throws MalformedURLException {  
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        och.setRequestUrl(new URL("https://streamwork.com/v1/feed/users/4q2BimSWiRulA56XLnX22A?page_size=100&page=1&comments=100"));
        och.setConsumerKey("3Ume0eXZm8Q9fpaLsAOZ");
        och.setConsumerSecret("JinGkf4bjzFQhOVUbhxpL3eU1esKgO8qTAGfCXy7");
        och.setToken("OPNy0Tk0EPfMimMOjTFs");
        och.setTokenSecret("fYtRQv54NygyJXUTzebgCopW3a5RTaBruvByh92g");
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
        //under normal circumstances, we would not set the timestamp and nonce, and just let the library
        //use the current time and a generated uuid. However, we're comparing with a known good request
        och.setTimestamp(1312874008L);
        och.setNonce("AlPd4z");

        String expectedAuthorizationHeader = "OAuth oauth_signature_method=\"HMAC-SHA1\",oauth_version=\"1.0\",oauth_nonce=\"AlPd4z\",oauth_timestamp=\"1312874008\",oauth_consumer_key=\"3Ume0eXZm8Q9fpaLsAOZ\",oauth_token=\"OPNy0Tk0EPfMimMOjTFs\",oauth_signature=\"t6d%2F6vLUvDJVvdt1k53PlwPuzko%3D\"";
        String actualAuthorizationHeader = och.generateAuthorizationHeader();
        assertEqualAuthorizationHeaders(expectedAuthorizationHeader, actualAuthorizationHeader);            
    }    

    /**
     * Tests a call to an OAuth endpoint: GET /oauth/test_request using the RSA-SHA1 signature method, which is the most secure one.
     * This is a regular 3-legged OAuth call i.e. with an authenticated access token for a user.
     */    
    public void testOAuthCallWithRsaSha1Signature() throws MalformedURLException {
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        och.setRequestUrl(new URL("https://cheetah.dhcp.pgdev.sap.corp/oauth/test_request")); 
        och.setConsumerKey("1VMzOctCAidMaahS9yJU");            
        och.setConsumerPrivateKey(TEST_CONSUMER_PRIVATE_KEY);
        och.setToken("qlbjCFbiBewXcCSAgIB9");      
        och.setSignatureMethod(SignatureMethod.RSA_SHA1);
        //under normal circumstances, we would not set the timestamp and nonce, and just let the library
        //use the current time and a generated uuid. However, we're comparing with a known good request
        och.setTimestamp(1307745674L);
        och.setNonce("oCaDVVBkIw");
             
        String expectedAuthorizationHeader = "OAuth oauth_consumer_key=\"1VMzOctCAidMaahS9yJU\", oauth_nonce=\"oCaDVVBkIw\", oauth_signature_method=\"RSA-SHA1\", oauth_timestamp=\"1307745674\", oauth_version=\"1.0\", oauth_token=\"qlbjCFbiBewXcCSAgIB9\", oauth_signature=\"U2gpD7SZldInb6JorOkopdKBDlUG2xZikHf92MwKxFH%2FdXxr9J6LSsrg0G8HXPHXGgzm5%2BD7edjz2gl1yss4jtFBCb8AmMxp5VVyehzlZUm6A4rfpkrq9tH7Hdpc%2BLCnFC4c2vqAMzT%2BTf3r2Ki%2FrE9hwtu4Iireb1feN3V3ZQ7rZNRjdPc%2BJpDYSkoo9VTL2KSzUzZYDYJaSRLPoryburLRpam%2BMA3DCvFrCT6pKOXnS6II5H6Uyt%2FOR3GHPDWcb15zZijMKstxPaj8kvv6ziwVPcN1UVm8p12%2FKEQxlmohVwh1YR0lObT%2BKG790u47Em3Gk8Ot%2FDN4cg9ewJqb%2BA%3D%3D\"";
        assertEqualAuthorizationHeaders(expectedAuthorizationHeader, och.generateAuthorizationHeader());        
    }
    
    /**
     * Tests a call to an OAuth endpoint: GET /oauth/test_request using the PLAINTEXT signature method.
     * This is a regular 3-legged OAuth call i.e. with an authenticated access token for a user.
     * 
     * Note: using the PLAINTEXT signature method is not recommended, other than for initial development work.
     * 
     */    
    public void testOAuthCallWithPlaintextSignature() throws MalformedURLException {
        
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        och.setRequestUrl(new URL("https://cheetah.dhcp.pgdev.sap.corp/oauth/test_request")); 
        och.setConsumerKey("1VMzOctCAidMaahS9yJU");
        och.setConsumerSecret("JinGkf4bjzFQhOVUbhxpL3eU1esKgO8qTAGfCXy7");
        och.setToken("qlbjCFbiBewXcCSAgIB9");
        och.setTokenSecret("fYtRQv54NygyJXUTzebgCopW3a5RTaBruvByh92g");
        och.setSignatureMethod(SignatureMethod.PLAINTEXT);
        //under normal circumstances, we would not set the timestamp and nonce, and just let the library
        //use the current time and a generated uuid. However, we're comparing with a known good request
        och.setTimestamp(1307739908);
        och.setNonce("GSBTC7JA5Q");
        
        String expectedAuthorizationHeader = "OAuth oauth_consumer_key=\"1VMzOctCAidMaahS9yJU\", oauth_nonce=\"GSBTC7JA5Q\", oauth_signature_method=\"PLAINTEXT\", oauth_timestamp=\"1307739908\", oauth_version=\"1.0\", oauth_token=\"qlbjCFbiBewXcCSAgIB9\", oauth_signature=\"JinGkf4bjzFQhOVUbhxpL3eU1esKgO8qTAGfCXy7%26fYtRQv54NygyJXUTzebgCopW3a5RTaBruvByh92g\"";
        assertEqualAuthorizationHeaders(expectedAuthorizationHeader, och.generateAuthorizationHeader());
    } 
    
    /**
     * Tests the initial entry point for initiating the process of obtaining an authorized OAuth access token,
     * namely a call to POST /oauth/request_token.
     */    
    public void testOAuthRequestTokenCall() throws MalformedURLException {
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.POST);
        och.setRequestUrl(new URL("https://staging.streamwork.com/oauth/request_token")); 
        och.setConsumerKey("E4dKDaEitNJYqhqKJ2cw");
        och.setConsumerSecret("julSfpM2z2C7YlJya8BcO4lMplvunMzPnQ4iEGWg");
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
        //must set either a callback URL or indicate that the callback is out of band
        och.setOutOfBandCallback();
        //under normal circumstances, we would not set the timestamp and nonce, and just let the library
        //use the current time and a generated uuid. However, we're comparing with a known good request
        och.setTimestamp(1303338556);
        och.setNonce("2c_H_Rwcxg");
        
        String expectedAuthorizationHeader = "OAuth oauth_consumer_key=\"E4dKDaEitNJYqhqKJ2cw\", oauth_nonce=\"2c_H_Rwcxg\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1303338556\", oauth_version=\"1.0\", oauth_callback=\"oob\", oauth_signature=\"qbNrBP02UOFckJUR8ucmZDWIedA%3D\"";
        assertEqualAuthorizationHeaders(expectedAuthorizationHeader, och.generateAuthorizationHeader());       
    }
    
    /**
     * Tests the call to obtain an authorized OAuth access token from a request token,
     * namely a call to POST /oauth/access_token.
     */       
    public void testOAuthAccessTokenCall() throws MalformedURLException {
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.POST);
        och.setRequestUrl(new URL("https://staging.streamwork.com/oauth/access_token")); 
        och.setConsumerKey("E4dKDaEitNJYqhqKJ2cw");
        och.setConsumerSecret("julSfpM2z2C7YlJya8BcO4lMplvunMzPnQ4iEGWg");
        //the request token
        och.setToken("KoRr6aAkMStAjR16ZxFY");
        //the request token secret
        och.setTokenSecret("lgM0DiY9HJB0y9HjP7ps9LXrxdqOgn4FKBH5yFrk");
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
        och.setVerifier("J188mJCSgxzGIbjNmjoi");
        //under normal circumstances, we would not set the timestamp and nonce, and just let the library
        //use the current time and a generated uuid. However, we're comparing with a known good request        
        och.setTimestamp(1303338681);
        och.setNonce("6UXq131D6A");
        
        String expectedAuthorizationHeader = "OAuth oauth_consumer_key=\"E4dKDaEitNJYqhqKJ2cw\", oauth_nonce=\"6UXq131D6A\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1303338681\", oauth_version=\"1.0\", oauth_token=\"KoRr6aAkMStAjR16ZxFY\", oauth_verifier=\"J188mJCSgxzGIbjNmjoi\", oauth_signature=\"9IXeoCfWbtjkt5otYmpeYG0k3jw%3D\"";
        assertEqualAuthorizationHeaders(expectedAuthorizationHeader, och.generateAuthorizationHeader());               
    }      
    
    /**
     * Verify test case from Appendix A.5.1 of OAuth spec
     * http://oauth.net/core/1.0a/#anchor12
     */
    public void testSignatureBaseString() throws MalformedURLException {
        
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        och.setRequestUrl(new URL("http://photos.example.net/photos"));
        och.setConsumerKey("dpf43f3p2l4k3l03");
        och.setToken("nnch734d00sl2jdk");
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
        long timestamp = 1191242096L;
        och.setTimestamp(timestamp);
        String nonce = "kllo9940pd9333jh";
        och.setNonce(nonce);
        List<Pair<String,String>> extraParams = new ArrayList<Pair<String,String>>();
        extraParams.add(new Pair<String,String>("file", "vacation.jpg"));
        extraParams.add(new Pair<String,String>("size", "original"));
        och.setExtraAuthorizationHeaderParams(extraParams);
       
        String expectedSignatureBaseString = "GET&http%3A%2F%2Fphotos.example.net%2Fphotos&file%3Dvacation.jpg%26oauth_consumer_key%3Ddpf43f3p2l4k3l03%26oauth_nonce%3Dkllo9940pd9333jh%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1191242096%26oauth_token%3Dnnch734d00sl2jdk%26oauth_version%3D1.0%26size%3Doriginal";
        assertEquals(expectedSignatureBaseString, och.generateSignatureBaseString(nonce, timestamp));
        
        //parameters in the query string get included in the signature
        och.setRequestUrl(new URL("http://photos.example.net/photos?file=vacation.jpg&size=original"));
        och.setExtraAuthorizationHeaderParams(null);
        assertEquals(expectedSignatureBaseString, och.generateSignatureBaseString(nonce, timestamp));
    }
    
    /**
     * Verify test case from the OAuth spec http://tools.ietf.org/html/rfc5849
     * section-3.4.1.1.
     */
    public void testSignatureBaseString2() throws MalformedURLException {
        
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.POST);
        och.setRequestUrl(new URL("http://example.com/request?b5=%3D%253D&a3=a&c%40=&a2=r%20b"));
        och.setRealm("Example");
        och.setConsumerKey("9djdj82h48djs9d2");
        och.setToken("kkk9d7dh3k39sjv7");
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
        long timestamp = 137131201L;
        och.setTimestamp(timestamp);
        String nonce = "7d8f3e4a";
        och.setNonce(nonce);
        och.omitOAuthVersion();
        List<Pair<String,String>> requestBodyParams = new ArrayList<Pair<String,String>>();
        requestBodyParams.add(new Pair<String,String>("c2", null));
        requestBodyParams.add(new Pair<String,String>("a3", "2 q")); //2+q in the spec, since it is shown in the form of a HTTP request, where it is encoded.
        och.setExtraRequestBodyParams(requestBodyParams);
       
        String expectedSignatureBaseString =
            "POST&http%3A%2F%2Fexample.com%2Frequest&a2%3Dr%2520b%26a3%3D2%2520q"
            + "%26a3%3Da%26b5%3D%253D%25253D%26c%2540%3D%26c2%3D%26oauth_consumer_"
            + "key%3D9djdj82h48djs9d2%26oauth_nonce%3D7d8f3e4a%26oauth_signature_m"
            + "ethod%3DHMAC-SHA1%26oauth_timestamp%3D137131201%26oauth_token%3Dkkk"
            + "9d7dh3k39sjv7";
        
        assertEquals(expectedSignatureBaseString, och.generateSignatureBaseString(nonce, timestamp));
    }
    
    /**
     * This tests that query params are being properly encoded. There was previously a bug in this
     * library that manifested itself when query param values needed escaping, such as the @ in the name query param value.
     * The correct behavior was verified with the OAuth calculator here: http://hueniverse.com/2008/10/beginners-guide-to-oauth-part-iv-signing-requests/
     */
    public void testOAuthCallWithEmailParam() throws MalformedURLException {

        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        och.setRequestUrl(new URL("https://cheetah.dhcp.pgdev.sap.corp/oauth/test_consumer_request?name=" + OAuthUtils.urlEncode("fred@gmail.com")));       
        och.setConsumerKey("1VMzOctCAidMaahS9yJU");
        och.setConsumerSecret("JinGkf4bjzFQhOVUbhxpL3eU1esKgO8qTAGfCXy7");
        och.setToken("kkk9d7dh3k39sjv7");
        och.setTokenSecret("fYtRQv54NygyJXUTzebgCopW3a5RTaBruvByh92g");
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
        long timestamp = 137131201L;
        och.setTimestamp(timestamp);
        String nonce = "7d8f3e4a";
        och.setNonce(nonce);      
        String expectedSignatureBaseString =
            "GET&https%3A%2F%2Fcheetah.dhcp.pgdev.sap.corp%2Foauth%2Ftest_consumer_request&name%3Dfred%2540gmail.com%26oauth_consumer_key%3D1VMzOctCAidMaahS9yJU%26oauth_nonce%3D7d8f3e4a%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D137131201%26oauth_token%3Dkkk9d7dh3k39sjv7%26oauth_version%3D1.0";                    
        assertEquals(expectedSignatureBaseString, och.generateSignatureBaseString(nonce, timestamp));
        
        String expectedAuthorizationHeader = "OAuth oauth_consumer_key=\"1VMzOctCAidMaahS9yJU\", oauth_token=\"kkk9d7dh3k39sjv7\", oauth_nonce=\"7d8f3e4a\", oauth_timestamp=\"137131201\", oauth_signature_method=\"HMAC-SHA1\", oauth_version=\"1.0\", oauth_signature=\"G3tfgEHevQfQ7jNlsWZ4O%2Fo92Xw%3D\"";
        assertEqualAuthorizationHeaders(expectedAuthorizationHeader, och.generateAuthorizationHeader());            
    }
    
    /**
     * Test based on the example at http://hueniverse.com/2008/10/beginners-guide-to-oauth-part-iv-signing-requests/
     * entitled "Non URL-Safe Parameter"
     */
    public void testOAuthCallWithNonUrlSafeParams() throws MalformedURLException {
        
        // http://PHOTOS.example.net:8001/Photos with the parameters photo size=300%, title=Back of $100 Dollars Bill

        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        och.setRequestUrl(new URL("http://PHOTOS.example.net:8001/Photos?" + OAuthUtils.urlEncode("photo size") + "=" + OAuthUtils.urlEncode("300%") +
                "&title=" + OAuthUtils.urlEncode("Back of $100 Dollars Bill")));        
        och.setConsumerKey("dpf43f3++p+#2l4k3l03");
        och.setConsumerSecret("kd9@4h%%4f93k423kf44");
        och.setToken("nnch734d(0)0sl2jdk");
        och.setTokenSecret("pfkkd#hi9_sl-3r=4s00");
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
        long timestamp = 1191242096L;
        och.setTimestamp(timestamp);
        String nonce = "kllo~9940~pd9333jh";
        och.setNonce(nonce);      
        String expectedSignatureBaseString =
            "GET&http%3A%2F%2Fphotos.example.net%3A8001%2FPhotos&oauth_consumer_key%3Ddpf43f3%252B%252Bp%252B%25232l4k3l03%26oauth_nonce%3Dkllo~9940~pd9333jh%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1191242096%26oauth_token%3Dnnch734d%25280%25290sl2jdk%26oauth_version%3D1.0%26photo%2520size%3D300%2525%26title%3DBack%2520of%2520%2524100%2520Dollars%2520Bill";
        assertEquals(expectedSignatureBaseString, och.generateSignatureBaseString(nonce, timestamp));
        
        String expectedAuthorizationHeader =
            "OAuth oauth_consumer_key=\"dpf43f3%2B%2Bp%2B%232l4k3l03\", oauth_token=\"nnch734d%280%290sl2jdk\", oauth_nonce=\"kllo~9940~pd9333jh\", oauth_timestamp=\"1191242096\", oauth_signature_method=\"HMAC-SHA1\", oauth_version=\"1.0\", oauth_signature=\"tTFyqivhutHiglPvmyilZlHm5Uk%3D\"";
        assertEqualAuthorizationHeaders(expectedAuthorizationHeader, och.generateAuthorizationHeader());            
    } 
    
    /**
     * Test based on the example at http://hueniverse.com/2008/10/beginners-guide-to-oauth-part-iv-signing-requests/
     * entitled "Non English Parameters"
     */
    public void testOAuthCallWithNonEnglishParams() throws MalformedURLException {
        
        // http://PHOTOS.example.net:8001/Photos with the type and scenario parameters having unicode parameters

        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        
        //Used the online tool http://www.snible.org/java2/uni2java.html to convert the unicode strings to Java String literals.
        String typeValue = "\u00D7\u0090\u00D7\u2022\u00D7\u02DC\u00D7\u2022\u00D7\u2018\u00D7\u2022\u00D7\u00A1";   
        //Note \u00A0 which encodes as UTF8 as C2A0 is the non-breaking space character.
        //http://stackoverflow.com/questions/2774471/what-is-c2-a0-in-mime-encoded-quoted-printable-text
        //If you just cut and paste from the web page, it will convert to a regular space, so be careful!
        String scenarioValue = "\u00D7\u00AA\u00D7\u0090\u00D7\u2022\u00D7\u00A0\u00D7\u201D";
        och.setRequestUrl(new URL("http://PHOTOS.example.net:8001/Photos?type=" + OAuthUtils.urlEncode(typeValue) +
                "&scenario=" + OAuthUtils.urlEncode(scenarioValue)));        
        och.setConsumerKey("dpf43f3++p+#2l4k3l03");
        och.setConsumerSecret("kd9@4h%%4f93k423kf44");
        och.setToken("nnch734d(0)0sl2jdk");
        och.setTokenSecret("pfkkd#hi9_sl-3r=4s00");
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
        long timestamp = 1191242096L;
        och.setTimestamp(timestamp);
        String nonce = "kllo~9940~pd9333jh";
        och.setNonce(nonce);      
        String expectedSignatureBaseString = "GET&http%3A%2F%2Fphotos.example.net%3A8001%2FPhotos&oauth_consumer_key%3Ddpf43f3%252B%252Bp%252B%25232l4k3l03%26oauth_nonce%3Dkllo~9940~pd9333jh%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1191242096%26oauth_token%3Dnnch734d%25280%25290sl2jdk%26oauth_version%3D1.0%26scenario%3D%25C3%2597%25C2%25AA%25C3%2597%25C2%2590%25C3%2597%25E2%2580%25A2%25C3%2597%25C2%25A0%25C3%2597%25E2%2580%259D%26type%3D%25C3%2597%25C2%2590%25C3%2597%25E2%2580%25A2%25C3%2597%25CB%259C%25C3%2597%25E2%2580%25A2%25C3%2597%25E2%2580%2598%25C3%2597%25E2%2580%25A2%25C3%2597%25C2%25A1";        
        assertEquals(expectedSignatureBaseString, och.generateSignatureBaseString(nonce, timestamp));
        
        String expectedAuthorizationHeader = "OAuth oauth_consumer_key=\"dpf43f3%2B%2Bp%2B%232l4k3l03\", oauth_token=\"nnch734d%280%290sl2jdk\", oauth_nonce=\"kllo~9940~pd9333jh\", oauth_timestamp=\"1191242096\", oauth_signature_method=\"HMAC-SHA1\", oauth_version=\"1.0\", oauth_signature=\"MH9NDodF4I%2FV6GjYYVChGaKCtnk%3D\"";
        assertEqualAuthorizationHeaders(expectedAuthorizationHeader, och.generateAuthorizationHeader());            
    }
    
    /**
     * Test based on a case where OAuth sort is not the same as lexicographic sort of the 'key=value' Strings.
     * For example, a1=3, a=2 sorts in OAuth order as a=2, a1=3, but as Strings it is a1=3, a=2 since '1' < '='.
     * The expected values were computed independently with the calculator at http://hueniverse.com/2008/10/beginners-guide-to-oauth-part-iv-signing-requests/
     * 
     * @throws MalformedURLException
     */
    public void testOAuthCallWithEdgeCasePairParamSortOrder() throws MalformedURLException {
       
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        och.setRequestUrl(new URL("http://PHOTOS.example.net:8001/Photos?a1=3&a=2"));        
        och.setConsumerKey("dpf43f3++p+#2l4k3l03");
        och.setConsumerSecret("kd9@4h%%4f93k423kf44");
        och.setToken("nnch734d(0)0sl2jdk");
        och.setTokenSecret("pfkkd#hi9_sl-3r=4s00");
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
        long timestamp = 1191242096L;
        och.setTimestamp(timestamp);
        String nonce = "kllo~9940~pd9333jh";
        och.setNonce(nonce);        
        String expectedSignatureBaseString =
            "GET&http%3A%2F%2Fphotos.example.net%3A8001%2FPhotos&a%3D2%26a1%3D3%26oauth_consumer_key%3Ddpf43f3%252B%252Bp%252B%25232l4k3l03%26oauth_nonce%3Dkllo~9940~pd9333jh%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1191242096%26oauth_token%3Dnnch734d%25280%25290sl2jdk%26oauth_version%3D1.0";        
        assertEquals(expectedSignatureBaseString, och.generateSignatureBaseString(nonce, timestamp));
                
        String expectedAuthorizationHeader =
            "OAuth oauth_consumer_key=\"dpf43f3%2B%2Bp%2B%232l4k3l03\", oauth_token=\"nnch734d%280%290sl2jdk\", oauth_nonce=\"kllo~9940~pd9333jh\", oauth_timestamp=\"1191242096\", oauth_signature_method=\"HMAC-SHA1\", oauth_version=\"1.0\", oauth_signature=\"XvQXwVym27PgKSIWiElVVSdGIq8%3D\"";
        assertEqualAuthorizationHeaders(expectedAuthorizationHeader, och.generateAuthorizationHeader());            
    }
           
    /**
     * Verify test case from Appendix A.5.2 of OAuth spec 
     * http://oauth.net/core/1.0a/#anchor12
     */    
    public void testHmacSha1Signature() {
        String message = "GET&http%3A%2F%2Fphotos.example.net%2Fphotos&file%3Dvacation.jpg%26oauth_consumer_key%3Ddpf43f3p2l4k3l03%26oauth_nonce%3Dkllo9940pd9333jh%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1191242096%26oauth_token%3Dnnch734d00sl2jdk%26oauth_version%3D1.0%26size%3Doriginal";
        String key = "kd94hf93k423kf44&pfkkdhi9sl3r4s00";
        assertEquals("tR3+Ty81lMeYAr/Fid0kMTYa/WM=", OAuthUtils.calculateHmacSha1Signature(message, key));      
    }    
    
    /**
     * Verifies base string URI encoding with some examples from the OAuth spec http://tools.ietf.org/html/rfc5849#section-3.5
     * @throws MalformedURLException
     */
    public void testBaseStringUrl() throws MalformedURLException {
        assertEquals("https://streamwork.com/", OAuthUtils.baseStringUrl(new URL("https://streamwork.com")));
        assertEquals("http://example.com/r%20v/X", OAuthUtils.baseStringUrl(new URL("HTTP://EXAMPLE.COM:80/r%20v/X?id=123")));
        assertEquals("https://www.example.net:8080/", OAuthUtils.baseStringUrl(new URL("https://www.example.net:8080/?q=1")));
    }
        
    /**
     * Checks that the authorization headers are equal, modulo ordering of the parameters
     */
    private void assertEqualAuthorizationHeaders(String h1, String h2) {
        String[] a1 = h1.split(",?\\s*");
        Arrays.sort(a1);
        String[] a2 = h2.split(",?\\s*");
        Arrays.sort(a2);      
        assertEquals(Arrays.asList(a1), Arrays.asList(a2));
    }
    
    /**
     * Verify test case from Appendix A.5.3 of OAuth spec
     * http://oauth.net/core/1.0a/
     */
    public void testOAuthCallUsingQueryString() throws MalformedURLException {
        
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        och.setRequestUrl(new URL("http://photos.example.net/photos?file=vacation.jpg&size=original"));
        och.setConsumerKey("dpf43f3p2l4k3l03");
        och.setConsumerSecret("kd94hf93k423kf44");
        och.setToken("nnch734d00sl2jdk");
        och.setTokenSecret("pfkkdhi9sl3r4s00");
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
        long timestamp = 1191242096L;
        och.setTimestamp(timestamp);
        String nonce = "kllo9940pd9333jh";
        och.setNonce(nonce);
  
        String expectedQueryString = "file=vacation.jpg&size=original&oauth_consumer_key=dpf43f3p2l4k3l03&oauth_token=nnch734d00sl2jdk&oauth_signature_method=HMAC-SHA1&oauth_signature=tR3%2BTy81lMeYAr%2FFid0kMTYa%2FWM%3D&oauth_timestamp=1191242096&oauth_nonce=kllo9940pd9333jh&oauth_version=1.0";        
        assertEquals(expectedQueryString, och.generateRequestQuery());
    } 
    
    /**
     * Verify test case from Appendix A.5.3 of OAuth spec
     * http://oauth.net/core/1.0a/
     */
    public void testOAuthCallUsingRequestBody() throws MalformedURLException {
        
        OAuthClientHelper och = new OAuthClientHelper();
        och.setHttpMethod(HttpMethod.GET);
        och.setRequestUrl(new URL("http://photos.example.net/photos"));
        och.setConsumerKey("dpf43f3p2l4k3l03");
        och.setConsumerSecret("kd94hf93k423kf44");
        och.setToken("nnch734d00sl2jdk");
        och.setTokenSecret("pfkkdhi9sl3r4s00");
        och.setSignatureMethod(SignatureMethod.HMAC_SHA1);
        long timestamp = 1191242096L;
        och.setTimestamp(timestamp);
        String nonce = "kllo9940pd9333jh";
        och.setNonce(nonce);
        List<Pair<String,String>> extraParams = new ArrayList<Pair<String,String>>();
        extraParams.add(new Pair<String,String>("file", "vacation.jpg"));
        extraParams.add(new Pair<String,String>("size", "original"));
        och.setExtraRequestBodyParams(extraParams);
  
        String expectedQueryString = "file=vacation.jpg&size=original&oauth_consumer_key=dpf43f3p2l4k3l03&oauth_token=nnch734d00sl2jdk&oauth_signature_method=HMAC-SHA1&oauth_signature=tR3%2BTy81lMeYAr%2FFid0kMTYa%2FWM%3D&oauth_timestamp=1191242096&oauth_nonce=kllo9940pd9333jh&oauth_version=1.0";        
        assertEquals(expectedQueryString, och.generateRequestBody());
    }   
    
    /** Verify the private key and certificate are indeed matching. */
    public void testConsumerPrivateKeyCertificateCompatibility() {
        assertTrue(SignatureUtil.validatePrivateKeyCertificateCompatibility(TEST_CONSUMER_PRIVATE_KEY, TEST_CONSUMER_CERTIFICATE));        
    }
    
    public void testSignatureVerificationRoundtrip() throws Exception {
        String signatureBaseString = "GET&https%3A%2F%2Fcheetah.dhcp.pgdev.sap.corp%2Foauth%2Ftest_consumer_request&oauth_consumer_key%3D1VMzOctCAidMaahS9yJU%26oauth_nonce%3DNtX9Gah3Bw%26oauth_signature_method%3DRSA-SHA1%26oauth_timestamp%3D1307748357%26oauth_token%3D%26oauth_version%3D1.0";
        String signature = OAuthUtils.calculateRsaSha1Signature(signatureBaseString, TEST_CONSUMER_PRIVATE_KEY);
        boolean valid = OAuthUtils.verifyRsaSha1Signature(TEST_CONSUMER_CERTIFICATE.getPublicKey(), signature, signatureBaseString);
        assertTrue(valid);       
    }    
}

