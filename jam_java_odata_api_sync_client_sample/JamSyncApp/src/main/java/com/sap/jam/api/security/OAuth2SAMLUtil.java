package com.sap.jam.api.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AssertionMarshaller;
import org.opensaml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationDataBuilder;


import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;


public final class OAuth2SAMLUtil {
    private static final String ACCESS_TOKEN_URL_PATH = "/api/v1/auth/token"; 

    private static Date addMinutesToDate(final Date d, final int minutes) {
        final Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.MINUTE, minutes);
        return c.getTime();
    }
    
    public static NameID makeEmailFormatName(final String subjectNameId, final String subjectNameIdFormat, final String subjectNameIdQualifier) {
        NameID nameID = (new NameIDBuilder().buildObject());

        if (subjectNameIdFormat.equals("email")) {
            nameID.setFormat(NameIDType.EMAIL);
        } else if (subjectNameIdFormat.equals("unspecified")) {
            nameID.setFormat(NameIDType.UNSPECIFIED);
        } else {
            throw new IllegalArgumentException("subjectNameIdFormat must be 'email' or 'unspecified'.");
        }
        
        if (subjectNameIdQualifier != null) {
            nameID.setNameQualifier(subjectNameIdQualifier);
        }
        
        nameID.setValue(subjectNameId);
        
        return nameID;
    }

    /** Signs the assertion and returns the string representation of the signed assertion */
    private static String signAssertion(Assertion assertion, PrivateKey privateKey)
    {
        // Build the signing credentials
        BasicX509Credential signingCredential = new BasicX509Credential();
        
        signingCredential.setPrivateKey(privateKey);
        
        // Build up the signature
        SignatureBuilder signatureBuilder = new SignatureBuilder();
        Signature signature = signatureBuilder.buildObject();
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signature.setSigningCredential(signingCredential);
        
        assertion.setSignature(signature);
        
        String assertionString = null;
        try {
            // Marshal the assertion
            AssertionMarshaller marshaller = new AssertionMarshaller();
            Element element = marshaller.marshall(assertion);
            
            // Finally, sign the assertion - this must be done after marshaling
            Signer.signObject(signature);
            
            assertionString = XMLHelper.nodeToString(element);
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (MarshallingException e) {
            e.printStackTrace();
        }
        
        return assertionString;
    }
    
    public static String buildSignedSAML2Assertion(
        final String idpId,
        final String destinationUri,
        
        final String subjectNameId,
        final String subjectNameIdFormat,
        final String subjectNameIdQualifier,

        final PrivateKey idpPrivateKey,
        final X509Certificate idpCertificate,
        final String spJamId,
        final Map<String, List<Object>> attributes) throws Exception {
                
        // Bootstrap the OpenSAML library
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            
        }

        DateTime issueInstant = new DateTime();
        DateTime notOnOrAfter = issueInstant.plusMinutes(10);
        DateTime notBefore = issueInstant.minusMinutes(10);
        
        NameID nameID = makeEmailFormatName(subjectNameId, subjectNameIdFormat, subjectNameIdQualifier);
        
        SubjectConfirmationData subjectConfirmationData = (new SubjectConfirmationDataBuilder().buildObject());
        subjectConfirmationData.setRecipient(destinationUri);
        subjectConfirmationData.setNotOnOrAfter(notOnOrAfter);
        
        SubjectConfirmation subjectConfirmation = (new SubjectConfirmationBuilder().buildObject());
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

        Subject subject = (new SubjectBuilder().buildObject());
        subject.setNameID(nameID);
        subject.getSubjectConfirmations().add(subjectConfirmation);
        
        Issuer issuer = (new IssuerBuilder().buildObject());
        issuer.setValue(idpId);
        
        Audience audience = (new AudienceBuilder().buildObject());
        audience.setAudienceURI(spJamId);
        
        AudienceRestriction audienceRestriction = (new AudienceRestrictionBuilder().buildObject());
        audienceRestriction.getAudiences().add(audience);
        
        Conditions conditions = (new ConditionsBuilder().buildObject());
        conditions.setNotBefore(notBefore);
        conditions.setNotOnOrAfter(notOnOrAfter);
        conditions.getAudienceRestrictions().add(audienceRestriction);
       
        Assertion assertion = (new AssertionBuilder().buildObject());
        assertion.setID(UUID.randomUUID().toString());
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssueInstant(issueInstant);
        assertion.setIssuer(issuer);
        assertion.setSubject(subject);
        assertion.setConditions(conditions);

        return signAssertion(assertion, idpPrivateKey);
    }
}



