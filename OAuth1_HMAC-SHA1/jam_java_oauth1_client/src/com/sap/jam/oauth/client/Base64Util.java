package com.sap.jam.oauth.client;


import java.io.IOException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public final class Base64Util {
    
    private static final BASE64Encoder ENCODER = new BASE64Encoder();
    private static final BASE64Decoder DECODER = new BASE64Decoder();
    
    
    /**
     * Encode byte array to base64 encoded String with newlines stripped
     */
    public static String encode(final byte[] rawData) {
        return ENCODER.encodeBuffer(rawData).replaceAll("\r\n", "").replaceAll("\n", "");
    }

    /**
     * Decode base64 encoded String to a byte array.
     */
    public static byte[] decode(final String encodedData) {
        try {
            return DECODER.decodeBuffer(encodedData);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }    

}
