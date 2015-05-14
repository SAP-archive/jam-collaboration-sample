package com.sap.jam.oauth.client;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.io.ByteArrayInputStream;

import javax.crypto.Cipher;

/**
 * This utility class has various helpers for working with signatures, certificates,
 * and private keys.
 * 
 * For example, generation of a signature, ie, signing data with a private key 2) Verification of such
 * signatures, ie, verifying signature with a public key/certificate
 * 
 * Public facing functions fall into one of these two buckets, variations are
 * overloaded based on different input parameters and renamed based on different
 * output values (ie, we like to handle base64 encoding so the caller doesn't
 * need to)
 * 
 * the notion of a "raw signature" is one that is stored as a java byte array,
 * and has not been base64 encoded
 */

public final class SignatureUtil {

    private final static String SIGNATURE_ALGORITHM = "SHA1withRSA";
    private final static String PRIVATE_KEY_ALGORITHM = "RSA";
    private final static String PUBLIC_CERT_ALGORITHM = "X.509";

    /**
     * Signature Generation functions all functions take the form signature =
     * f(private key, signature base)
     * 
     */
    public static String generateBase64Signature( final String privateKeyBase64, final byte[] signatureBase) {
        return Base64Util.encode(generateRawSignature(makePrivateKey(privateKeyBase64), signatureBase));
    }

    public static String generateBase64Signature(final PrivateKey privateKey, final byte[] signatureBase) {
        return Base64Util.encode(generateRawSignature(privateKey, signatureBase));
    }

    public static byte[] generateRawSignature(final String privateKeyBase64, final byte[] signatureBase) {
        return generateRawSignature(makePrivateKey(privateKeyBase64), signatureBase);
    }

    public static byte[] generateRawSignature(final PrivateKey privateKey, final byte[] signatureBase) {
        try {
            final Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initSign(privateKey);
            sig.update(signatureBase);
            return sig.sign();
        } catch (final InvalidKeyException e) {
            throw new RuntimeException("Invalid private key: " + e.getMessage(), e);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("Unknown signature generation algorithm (" + SIGNATURE_ALGORITHM + ") " + e.getMessage(), e);
        } catch (final java.security.SignatureException e) {
            throw new RuntimeException("Error encountered while signing: " + e.getMessage(), e);
        }
    }

    /**
     * Signature Verification functions all functions take the form success =
     * f(public key, signature, signature base)
     * 
     */
    public static boolean verifyBase64Signature(final String base64Certificate, final String base64Signature, final byte[] signatureBase) {
        return verifyRawSignature(makePublicKey(base64Certificate), getRawSignatureFromBase64(base64Signature), signatureBase);
    }

    public static boolean verifyBase64Signature(final PublicKey publicKey, final String base64Signature, final byte[] signatureBase) {
        return verifyRawSignature(publicKey, getRawSignatureFromBase64(base64Signature), signatureBase);
    }

    public static boolean verifyRawSignature(final String base64Certificate, final byte[] rawSignature, final byte[] signatureBase) {
        return verifyRawSignature(makePublicKey(base64Certificate), rawSignature, signatureBase);
    }

    public static boolean verifyRawSignature(final PublicKey publicKey, final byte[] rawSignature, final byte[] signatureBase) {
        try {
            final Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(signatureBase);
            return sig.verify(rawSignature);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("Unknown signature verification algorithm (" + SIGNATURE_ALGORITHM + ") " + e.getMessage(), e);
        } catch (final InvalidKeyException e) {
            throw new RuntimeException("Invalid public key: " + e.getMessage(), e);
        } catch (final java.security.SignatureException e) {
            throw new RuntimeException("Error encountered while verifying signature: " + e.getMessage(), e);
        }
    }

    /**
     * Utility helper functions prevent duplication of work over the various
     * overloads
     */

    /**
     * convert basee64 signature to a raw signature
     */
    private static byte[] getRawSignatureFromBase64(final String signature) {      
        return Base64Util.decode(signature);       
    }

    /**
     * convert a base64 encoded certificate into a java object public key
     */
    public static PublicKey makePublicKey(final String certificateBase64) {

        if (certificateBase64 == null || certificateBase64.isEmpty()) {
            throw new IllegalArgumentException("Supplied 'certificateBase64' argument is null or empty.");
        }

        try {
            final CertificateFactory cf = CertificateFactory.getInstance(PUBLIC_CERT_ALGORITHM);
            final Certificate certificate = cf.generateCertificate(new ByteArrayInputStream(Base64Util.decode(certificateBase64)));
            return certificate.getPublicKey();
        } catch (final CertificateException e) {
            throw new RuntimeException("Unable to generate certificates (" + PUBLIC_CERT_ALGORITHM + ") " + e.getMessage(), e);
        } 
    }

    /**
     * convert a base64 encoded private key into a java object private key
     */
    public static PrivateKey makePrivateKey(final String privateKeyBase64) {

        if (privateKeyBase64 == null || privateKeyBase64.isEmpty()) {
            throw new IllegalArgumentException("Supplied 'privateKeyBase64' argument is null or empty.");
        }

        try {
            final KeyFactory key_factory = KeyFactory.getInstance(PRIVATE_KEY_ALGORITHM);
            final PKCS8EncodedKeySpec private_key_spec = new PKCS8EncodedKeySpec(Base64Util.decode(privateKeyBase64));
            return key_factory.generatePrivate(private_key_spec);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid algorithm (" + PRIVATE_KEY_ALGORITHM + "): " + e.getMessage(), e);
        } catch (final InvalidKeySpecException e) {
            throw new RuntimeException("Invalid KeySpec: " + e.getMessage(), e);
        }
    }

    public static X509Certificate makeCertificate(String certificateBase64) {
        if (certificateBase64 == null || certificateBase64.isEmpty()) {
            throw new IllegalArgumentException("Supplied 'certificateBase64' argument is null or empty.");
        }

        try {
            byte[] certRaw = Base64Util.decode(certificateBase64);           
            CertificateFactory certFactory = CertificateFactory.getInstance(PUBLIC_CERT_ALGORITHM);
            return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certRaw));
        } catch (Exception e) {
            throw new RuntimeException("Unable to deserialize supplied X509 certificate.", e);
        }
    }
    
    /**
     * @return true if encrypting then decrypting a small test string roundtrips.
     */
    public static boolean validatePrivateKeyCertificateCompatibility(PrivateKey privateKey, X509Certificate certificate) {
        return validatePrivateKeyPublicKeyCompatibility(privateKey, certificate.getPublicKey());      
    }
    
    /**
     * @return true if encrypting then decrypting a small test string roundtrips.
     */
    public static boolean validatePrivateKeyPublicKeyCompatibility(PrivateKey privateKey, PublicKey publicKey) {
        String plainData = "Some text for encryption";                    
        try {
            byte[] encryptedData = encrypt(plainData.getBytes("UTF-8"), publicKey);
            byte[] decryptedData = decrypt(encryptedData, privateKey);
            String plainDataRoundTrip = new String(decryptedData, "UTF-8");
            if (plainData.equals(plainDataRoundTrip)) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    
        return false;
    }    
    
    static byte[] encrypt(byte[] plainData, PublicKey pubKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] encryptedData = cipher.doFinal(plainData);
        return  encryptedData;
    }

    static byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedData = cipher.doFinal(encryptedData);  
        return decryptedData;
    }    
    
    /**
     * Returns XML suitable for use in .NET code with the RSACryptoServiceProvider.FromXmlString method.
     * This RSACryptoServiceProvider object can be used in the .NET version of the OAuth libraries in the areas
     * where we use a PrivateKey object in the Java libraries. 
     * An explanation of the XML used for key formats is here: http://msdn.microsoft.com/en-us/library/system.security.cryptography.rsa.toxmlstring.aspx
     * Thanks to http://www.jensign.com/JavaScience/PvkConvert/ for pointing out that leading zeros must be trimmed for .NET.
     */
    public static String privateKeyToDotNetXml(PrivateKey privateKey) {
        try{
            StringBuilder sb = new StringBuilder();
            RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey)privateKey;
            sb.append("<RSAKeyValue>") ;
            sb.append("<Modulus>" + Base64Util.encode(removeLeadingZeros(rsaPrivateKey.getModulus().toByteArray())) + "</Modulus>");
            sb.append("<Exponent>" + Base64Util.encode(removeLeadingZeros(rsaPrivateKey.getPublicExponent().toByteArray())) + "</Exponent>");
            sb.append("<P>" + Base64Util.encode(removeLeadingZeros(rsaPrivateKey.getPrimeP().toByteArray())) + "</P>");
            sb.append("<Q>" + Base64Util.encode(removeLeadingZeros(rsaPrivateKey.getPrimeQ().toByteArray())) + "</Q>");
            sb.append("<DP>" +Base64Util.encode(removeLeadingZeros(rsaPrivateKey.getPrimeExponentP().toByteArray())) + "</DP>");
            sb.append("<DQ>" + Base64Util.encode(removeLeadingZeros(rsaPrivateKey.getPrimeExponentQ().toByteArray())) + "</DQ>");
            sb.append("<InverseQ>" + Base64Util.encode(removeLeadingZeros(rsaPrivateKey.getCrtCoefficient().toByteArray())) + "</InverseQ>");
            sb.append("<D>" + Base64Util.encode(removeLeadingZeros(rsaPrivateKey.getPrivateExponent().toByteArray())) + "</D>");
            sb.append("</RSAKeyValue>") ;
            return sb.toString();
        } catch(Exception e) {
            throw new IllegalArgumentException("Could not convert PrivateKey to Dot Net XML.", e);
        }   
    }
    
    /**
     * Returns XML suitable for use in .NET code with the RSACryptoServiceProvider.FromXmlString method.
     * This RSACryptoServiceProvider object can be used in the .NET version of the OAuth libraries in the areas
     * where we use a PublicKey object in the Java libraries. 
     * An explanation of the XML used for key formats is here: http://msdn.microsoft.com/en-us/library/system.security.cryptography.rsa.toxmlstring.aspx
     * Thanks to http://www.jensign.com/JavaScience/PvkConvert/ for pointing out that leading zeros must be trimmed for .NET.
     */
    public static String publicKeyToDotNetXml(PublicKey publicKey) {
        try{
            StringBuilder sb = new StringBuilder();
            RSAPublicKey rsaPublicKey = (RSAPublicKey)publicKey;
            sb.append("<RSAKeyValue>") ;
            sb.append("<Modulus>" + Base64Util.encode(removeLeadingZeros(rsaPublicKey.getModulus().toByteArray())) + "</Modulus>");
            sb.append("<Exponent>" + Base64Util.encode(removeLeadingZeros(rsaPublicKey.getPublicExponent().toByteArray())) + "</Exponent>");
            sb.append("</RSAKeyValue>") ;
            return sb.toString();
        } catch(Exception e) {
            throw new IllegalArgumentException("Could not convert PublicKey to Dot Net XML.", e);
        }   
    }    
    
    private static byte[] removeLeadingZeros(byte[] data) {
        
        int len = data.length;
        if (len < 1) {
            throw new IllegalArgumentException("non-empty byte array expected.");
        }        
        int pos;
        for (pos = 0; pos < len && data[pos] == 0; ++pos) {
        }

        if (pos >= len) {
            throw new IllegalArgumentException("array of zeros not expected.");
        }

        if (pos > 0) {
            byte[] trimmedData = new byte[len - pos];
            System.arraycopy(data, pos, trimmedData, 0, len - pos);
            return trimmedData;          
        } 

        return data;
    }
  
}


