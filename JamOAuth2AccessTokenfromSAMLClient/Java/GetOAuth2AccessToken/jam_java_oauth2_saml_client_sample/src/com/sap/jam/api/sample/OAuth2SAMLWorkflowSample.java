package com.sap.jam.api.sample;

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
import java.util.List;
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


/**
 * Purpose:
 * - Illustrates authentication of the SAP Jam API using an OAuth2 access token obtained from a SAML2 bearer assertion.
 * - More information at https://help.sap.com/viewer/u_collaboration_dev_help/c6813927839541a19e4703c3a2564f1b.html
 * 
 * Prerequisite:
 * - Jam deployment with an OAuth client application and a SAML Trusted IDP configured for the company.
 * 
 * Result:
 * - Prints the OAuth access token to the console. 
 * - With this access token, you'll be able to make authenticated requests to the SAP Jam API.
 * 
 * Procedure:
 * 1. Configure the REQUIRED and OPTIONAL private static final variables to your SAP Jam Collaboration instance.
 * 2. Run as a "Java Application". An OAuth access token will appear in the console.
 * 3. Copy the access token and use it to make authenticated requests to the SAP Jam API:
 *    - If you are using Cloud Platform, try it at https://developer.sapjam.com/ODataDocs/ui
 *      - e.g. Select GROUP > GET/GROUPS, scroll down to GET/GROUPS, click TRY.
 *    - Try using Postman or cURL as well:
 *      - e.g. (GET /GROUPS)
 *        - curl https://{jam_instance}.sapjam.com/api/v1/OData/Groups -H "Authorization: OAuth {OAuth2 Access Token}" -H "Accept: application/json"
 *    - Try using it in your own programs.
 */
public class OAuth2SAMLWorkflowSample {
	/** 
	 * REQUIRED: Configure to your SAP Jam Collaboration instance.
	 */
	// The base url of your Jam instance (https://{jam_instance}.sapjam.com).
	private static final String BASE_URL = "";
	
	// The OAuth client key. Used as the the client_id parameter in the POST /api/v1/auth/token call
	private static final String CLIENT_KEY = "";
	
	// Identifier for the SAML trusted IDP (https://{SAML_trusted_IDP_URL}).
	private static final String IDP_ID = "";
	
	// The identifier for the user.
	// Valid values: email address or a unique identifier.
	private static final String SUBJECT_NAME_ID = "";
	
	// Valid values: "email" or "unspecified".
	private static final String SUBJECT_NAME_ID_FORMAT = "";
	
	// Base64 encoded IDP private key.
	private static final String IDP_PRIVATE_KEY_STRING = "";
	
	/** 
	 * OPTIONAL: Configure to your SAP Jam Collaboration instance.
	 */	
	// The OAuth client secret (not recommended).
	// - If supplied the CLIENT_KEY is not included in the SAML assertion as an attribute.
	// - It is recommended to not supply the clientSecret, but it is included to show how
	//   SAML IDPs that cannot add assertion attributes can work with the OAuth SAML2 bearer flow.
	private static final String clientSecret = null;
	
	// For SuccessFactors integrated companies when using the "unspecified" name id format.
	// Valid value: "www.successfactors.com".
	private static final String subjectNameIdQualifier = null;
	
	/**
	 *  DO NOT CHANGE
	 */ 
    private static final String SP_ID_JAM = "cubetree.com";
    private static final String ACCESS_TOKEN_URL_PATH = "/api/v1/auth/token"; 
    private static final String SAML2_BEARER_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:saml2-bearer";
    
    public static void main (String[] args) throws Exception {
        
        if (!SUBJECT_NAME_ID_FORMAT.equals("email") && !SUBJECT_NAME_ID_FORMAT.equals("unspecified")) {
            throw new IllegalArgumentException("The value of SUBJECT_NAME_ID_FORMAT must be 'email' or 'unspecified'.");
        }
        
        PrivateKey idpPrivateKey = SignatureUtil.makePrivateKey(IDP_PRIVATE_KEY_STRING);
            
        postOAuth2AccessToken(idpPrivateKey);
    }
       
    /**
     * Creates an OAuth2 access token from a SAML bearer assertion
     * POST /api/v1/auth/token
     */
    private static String postOAuth2AccessToken(PrivateKey idpPrivateKey) throws Exception {
        
        System.out.println("\n***************************************************************");
        String urlString = BASE_URL + "/api/v1/auth/token";
        System.out.println("POST " + urlString);
  
        URL requestUrl = new URL(urlString);
        
        Assertion assertion = buildSAML2Assertion(clientSecret == null);
        String signedAssertion = signAssertion(assertion, idpPrivateKey);
        System.out.println("Signed assertion: " + signedAssertion);
        
        List<Pair<String,String>> postParams = new ArrayList<Pair<String,String>>();
        postParams.add(new Pair<String,String>("client_id", URLEncoder.encode(CLIENT_KEY, "UTF-8")));
        if (clientSecret != null) {
            postParams.add(new Pair<String,String>("client_secret", URLEncoder.encode(clientSecret, "UTF-8")));
        }
        postParams.add(new Pair<String,String>("grant_type", URLEncoder.encode(SAML2_BEARER_GRANT_TYPE, "UTF-8")));
        String base64SamlAssertion = new String(Base64.encodeBytes(signedAssertion.getBytes(), Base64.DONT_BREAK_LINES));
   
        postParams.add(new Pair<String,String>("assertion", URLEncoder.encode(base64SamlAssertion, "UTF-8")));   
       
        String requestBody = joinPostBodyParams(postParams);
        System.out.println("Request body: " + requestBody);
         
        return postOAuth2AccessTokenHelper(requestUrl,requestBody);
    }
    
    private static Assertion buildSAML2Assertion(boolean includeClientKeyAttribute)
    {
        // Bootstrap the OpenSAML library
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
        }

        DateTime issueInstant = new DateTime();
        DateTime notOnOrAfter = issueInstant.plusMinutes(10);
        DateTime notBefore = issueInstant.minusMinutes(10);
        
        NameID nameID = (new NameIDBuilder().buildObject());
        if (SUBJECT_NAME_ID_FORMAT.equals("email")) {
            nameID.setFormat(NameIDType.EMAIL);
        } else if (SUBJECT_NAME_ID_FORMAT.equals("unspecified")) {
            nameID.setFormat(NameIDType.UNSPECIFIED);
        } else {
            throw new IllegalArgumentException("SUBJECT_NAME_ID_FORMAT must be 'email' or 'unspecified'.");
        }
        if (subjectNameIdQualifier != null) {
            nameID.setNameQualifier(subjectNameIdQualifier);
        }
        nameID.setValue(SUBJECT_NAME_ID);
        
        SubjectConfirmationData subjectConfirmationData = (new SubjectConfirmationDataBuilder().buildObject());
        subjectConfirmationData.setRecipient(BASE_URL + ACCESS_TOKEN_URL_PATH);
        subjectConfirmationData.setNotOnOrAfter(notOnOrAfter);
        
        SubjectConfirmation subjectConfirmation = (new SubjectConfirmationBuilder().buildObject());
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

        Subject subject = (new SubjectBuilder().buildObject());
        subject.setNameID(nameID);
        subject.getSubjectConfirmations().add(subjectConfirmation);
        
        Issuer issuer = (new IssuerBuilder().buildObject());
        issuer.setValue(IDP_ID);
        
        Audience audience = (new AudienceBuilder().buildObject());
        audience.setAudienceURI(SP_ID_JAM);
        
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
        
        if (includeClientKeyAttribute) {
            XSString attributeValue = (XSString)Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME).buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
            attributeValue.setValue(CLIENT_KEY);
    
            Attribute attribute = (new AttributeBuilder().buildObject());
            attribute.setName("client_id");
            attribute.getAttributeValues().add(attributeValue);
    
            AttributeStatement attributeStatement = (new AttributeStatementBuilder().buildObject());
            attributeStatement.getAttributes().add(attribute);
            assertion.getAttributeStatements().add(attributeStatement);
        }

        return assertion;
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

    private static String postOAuth2AccessTokenHelper(
            URL requestUrl, String requestBody) throws Exception {
        
        HttpURLConnection connection = createConnection(requestUrl);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        connection.setDoOutput(true);
  
        OutputStream output = null;
        try {
             output = connection.getOutputStream();
             output.write(requestBody.getBytes("UTF-8"));
        } finally {
             if (output != null) try { output.close(); } catch (IOException e) {
                 e.printStackTrace();
             }
        }
           
        int responseCode = connection.getResponseCode();
        System.out.println("HTTP response code: " + responseCode);
        InputStream is;
        if (connection.getResponseCode() >= 400) {
            is = connection.getErrorStream();            
        } else {
            is = connection.getInputStream();
        }
        
        StringBuilder result = new StringBuilder();

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        while ((inputLine = in.readLine()) != null) { 
            result.append(inputLine);
        }
        in.close();
        
        String resultString = result.toString();
        System.out.println ("Response body: " + resultString);
        
        String oauthToken = null;
        if (responseCode == 200) {
            //The OAuth2 spec actually requires a token_type parameter.
            //{"token_type":"bearer","access_token":"As3UvIaYEvDXoeREtmSz3qeCpnNvrrHZhVMswcBV"} 
            int tokenStartIndex = resultString.indexOf("access_token") + "access_token".length() + 3;
            int tokenEndIndex = resultString.indexOf('"', tokenStartIndex);
            oauthToken = resultString.substring(tokenStartIndex, tokenEndIndex);
            System.out.println ("OAuth access token: " + oauthToken);
        }
        
        return oauthToken;
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
 
    private static String joinPostBodyParams(List<Pair<String,String>> postParams)
    {
       StringBuilder sb = new StringBuilder();
       boolean first = true;
       for (Pair<String,String> item : postParams)
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



