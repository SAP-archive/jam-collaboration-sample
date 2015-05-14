package com.sap.jam.oauth.client;

import java.util.Locale;

public enum SignatureMethod {
    PLAINTEXT("PLAINTEXT"),
    HMAC_SHA1("HMAC-SHA1"),
    RSA_SHA1("RSA-SHA1");
    
    private final String description;
    
    private SignatureMethod(String description) {
        this.description = description;
    }
    
    public final String toString(){
        return description;
    }
    
    public static SignatureMethod fromOAuthProtocolString(String oauthSignatureMethod)  {
        if (oauthSignatureMethod.equals(SignatureMethod.HMAC_SHA1.toString())) {
            return SignatureMethod.HMAC_SHA1;
        }
        if (oauthSignatureMethod.equals(SignatureMethod.RSA_SHA1.toString())) {
            return SignatureMethod.RSA_SHA1;
        }
        if (oauthSignatureMethod.equals(SignatureMethod.PLAINTEXT.toString())) {
            return SignatureMethod.PLAINTEXT;
        }
        throw new IllegalArgumentException("Unknown OAuth signature method.");
    }
    
    public static SignatureMethod fromString(String signatureMethod) {
        signatureMethod = signatureMethod.toUpperCase(Locale.ROOT);
        if (signatureMethod.equals(PLAINTEXT.toString())) {
            return PLAINTEXT;
        } else if (signatureMethod.equals(HMAC_SHA1.toString())) {
            return HMAC_SHA1;
        } else if (signatureMethod.equals(RSA_SHA1.toString())) {
            return RSA_SHA1;
        } 
        throw new IllegalArgumentException("Unrecognized signature method " + signatureMethod + ".");
    }    
    
}
