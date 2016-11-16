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
import java.util.Arrays;
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


/**
 * This class provides some working Java sample client code to illustrate authentication of the SAP Jam API
 * using an OAuth2 access token obtained from a SAML2 bearer assertion as described here:
 * http://help.sap.com/download/documentation/sapjam/developer/index.html#c6813927839541a19e4703c3a2564f1b.html
 * 
 * This example assumes a Jam deployment with an OAuth client application and a SAML Trusted IDP configured 
 * for the company.
 * 
 * It is run by invoking the "main" method and supplying command line arguments of the form 
 * key1=value1 key2=value2 key3=value3 ...
 * 
 * The keys are:
 * baseUrl - Required. The base url of the Jam site e.g. https://jam4.sapjam.com.
 * clientKey - Required. The OAuth client key. Used as the the client_id parameter in the POST /api/v1/auth/token call
 * clientSecret - Optional and not recommended. The OAuth client secret. If supplied the clientKey is not included
 *    in the SAML assertion as an attribute. It is recommended to not supply the clientSecret, but it is included
 *    to show how SAML IDPs that cannot add assertion attributes can work with the OAuth SAML2 bearer flow.
 * idpId - Required. Identifier for the SAML trusted IDP.
 * subjectNameId - Required. The identifier for the user. Can be an email address or a unique identifier, depending on the
 *    company type.
 * subjectNameIdFormat - Required. Either 'email' or 'unspecified' (without quotes).
 * subjectNameIdQualifier - Optional. For SuccessFactors integrated companies when using the 'unspecified' name id format,
 *   use the value 'www.successfactors.com' (without quotes). For all else, should omit.
 * idpPrivateKey - Required. Base64 encoded IDP private key.
 * 
 * As an example (where the idpPrivateKey is shortened)
 * baseUrl=https://developer.sapjam.com clientKey=i7Gb7qe9hzD4ix8D3vZ4 idpId=bo.ilic.test.idp subjectNameId=blue@berry.com subjectNameIdFormat=email idpPrivateKey=MIIEv...MsR7 
 * 
 * Depending on the environment, a proxy can be configured using VM arguments supplied to the jvm. For example,
 * for the environment where this program was developed these are:
 * -Dhttp.proxyHost=proxy.van.sap.corp
 * -Dhttp.proxyPort=8080
 * -Dhttps.proxyHost=proxy.van.sap.corp
 * -Dhttps.proxyPort=8080
 * 
 * The program will print to the console the OAuth access token. This can then be used easily for subsequent calls
 * e.g. for baseUrl=https://jam4.sapjam.com if the program produced an OAuth access token of As3UvmSz3qeCpnNvrrHZhIaYEvDXoeREtVMswcBV then
 *      the following curl command gets the profile of the user authenticated by the access token in JSON format:
 *      curl https://jam4.sapjam.com/api/v1/OData/Self -H "Authorization: OAuth As3UvmSz3qeCpnNvrrHZhIaYEvDXoeREtVMswcBV" -H "Accept: application/json"
 */
public class OAuth2SAMLWorkflowSample {
    
    //as obtained from SAP Jam docs    
    private static final String SP_ID_JAM = "cubetree.com";
    private static final String ACCESS_TOKEN_URL_PATH = "/api/v1/auth/token"; 
    private static final String SAML2_BEARER_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:saml2-bearer";
    
    public static void main (String[] args) throws Exception {
        
        System.out.println("Proxy settings:");
        System.out.println("http.proxyHost=" + System.getProperty("http.proxyHost"));
        System.out.println("http.proxyPort=" + System.getProperty("http.proxyPort"));
        System.out.println("https.proxyHost=" + System.getProperty("https.proxyHost"));
        System.out.println("https.proxyPort=" + System.getProperty("https.proxyPort"));
        
        Set<String> allowedKeys = new HashSet<String>(Arrays.asList(
                "baseUrl", "clientKey", "clientSecret",
                "idpId", "subjectNameId", "subjectNameIdQualifier", "subjectNameIdFormat",
                "idpPrivateKey"));
        
        Map<String,String> params = new LinkedHashMap<String,String>();
        for (String arg : args) {
            String[] keyValue = arg.split("=",2);
            if (keyValue.length != 2) {
                throw new IllegalArgumentException("Command line arguments must be of the form 'key=value' with distinct arguments separated by spaces e.g. client_id=a0m client_secret=oRMFr.");
            }
            
            String key = keyValue[0];
            if (!allowedKeys.contains(key)) {
                throw new IllegalArgumentException("Command line arguments: invalid key '" + key + "'. Allowed keys are: " + allowedKeys.toString());
            }
            
            if (params.put(key, keyValue[1]) != null) {
                throw new IllegalArgumentException("Command line arugments: duplicate key '" + key + "'.");
            }    
        }
        
        System.out.println("Command line arguments: " + params);
              
        String baseUrl = getRequiredParam(params, "baseUrl");
        String clientKey = getRequiredParam(params, "clientKey");
        String clientSecret = params.get("clientSecret");
        String idpId = getRequiredParam(params, "idpId");
        String subjectNameId = getRequiredParam(params, "subjectNameId");
        String subjectNameIdFormat = getRequiredParam(params, "subjectNameIdFormat");
        if (!subjectNameIdFormat.equals("email") && !subjectNameIdFormat.equals("unspecified")) {
            throw new IllegalArgumentException("Command line arguments: the value of subjectNameIdFormat must be 'email' or 'unspecified'.");
        }
        String subjectNameIdQualifier = params.get("subjectNameIdQualifier");
        String idpPrivateKeyString = getRequiredParam(params, "idpPrivateKey");
        PrivateKey idpPrivateKey = SignatureUtil.makePrivateKey(idpPrivateKeyString);
            
        postOAuth2AccessToken(baseUrl, clientKey, clientSecret,
            idpId, subjectNameId, subjectNameIdFormat, subjectNameIdQualifier, idpPrivateKey);
    }
    
    private static String getRequiredParam(Map<String,String> params, String key) {
        String value = params.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Required command line argument key '" + key + "' is missing.");
        }
        return value;
    }
       
    /**
     * Creates an OAuth2 access token from a SAML bearer assertion
     * POST /api/v1/auth/token
     */
    private static String postOAuth2AccessToken(
            String baseUrl,
            String clientKey,
            String clientSecret,
            String idpId,
            String subjectNameId,
            String subjectNameIdFormat,
            String subjectNameIdQualifier,
            PrivateKey idpPrivateKey) throws Exception {
        
        System.out.println("\n***************************************************************");
        String urlString = baseUrl + "/api/v1/auth/token";
        System.out.println("POST " + urlString);
  
        URL requestUrl = new URL(urlString);
        
        Assertion assertion = buildSAML2Assertion(baseUrl, subjectNameId, subjectNameIdFormat, subjectNameIdQualifier, idpId, clientKey, clientSecret == null);
        String signedAssertion = signAssertion(assertion, idpPrivateKey);
        System.out.println("Signed assertion: " + signedAssertion);
        
        List<Pair<String,String>> postParams = new ArrayList<Pair<String,String>>();
        postParams.add(new Pair<String,String>("client_id", URLEncoder.encode(clientKey, "UTF-8")));
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
    
    private static Assertion buildSAML2Assertion(
            String baseUrl,
            String subjectNameId,
            String subjectNameIdFormat,
            String subjectNameIdQualifier,
            String idpId,
            String clientKey,
            boolean includeClientKeyAttribute)
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
        
        SubjectConfirmationData subjectConfirmationData = (new SubjectConfirmationDataBuilder().buildObject());
        subjectConfirmationData.setRecipient(baseUrl + ACCESS_TOKEN_URL_PATH);
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
            attributeValue.setValue(clientKey);
    
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



