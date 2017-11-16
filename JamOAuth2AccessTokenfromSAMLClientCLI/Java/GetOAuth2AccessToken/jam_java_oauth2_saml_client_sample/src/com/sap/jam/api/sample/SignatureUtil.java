package com.sap.jam.api.sample;

import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

import org.opensaml.xml.util.Base64;

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
        return Base64.encodeBytes(generateRawSignature(makePrivateKey(privateKeyBase64), signatureBase));
    }

    public static String generateBase64Signature(final PrivateKey privateKey, final byte[] signatureBase) {
        return Base64.encodeBytes(generateRawSignature(privateKey, signatureBase));
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
        return Base64.decode(signature);       
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
            final Certificate certificate = cf.generateCertificate(new ByteArrayInputStream(Base64.decode(certificateBase64)));
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
            final PKCS8EncodedKeySpec private_key_spec = new PKCS8EncodedKeySpec(Base64.decode(privateKeyBase64));
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
            byte[] certRaw = Base64.decode(certificateBase64);           
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
    
}



