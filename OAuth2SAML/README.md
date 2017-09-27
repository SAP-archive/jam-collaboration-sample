# Jam OAuth2 Access Token from SAML Bearer Assertion Sample

# Introduction
This sample provides some working Java sample client code to illustrate authentication of the SAP Jam API using an OAuth2 access token obtained from a SAML2 bearer assertion as described here:
* https://help.sap.com/viewer/u_collaboration_dev_help/c6813927839541a19e4703c3a2564f1b.html


# Setup
1. Download the source code from:
    * https://github.com/SAP/SAPJamSampleCode
2. Extract the zip file and navigate to the project folder ( ../OAuth2SAML/Java/GetOAuth2AccessToken/jam_java_oauth2_saml_client_sample )
3. Import this folder into Eclipse
4. Download the OpenSAML dependencies from:
    * https://build.shibboleth.net/nexus/content/repositories/releases/org/opensaml/opensaml/2.6.6/opensaml-2.6.6-bin.zip
5. Extract the zip file and place the files in the project folder ( ../OAuth2SAML/Java/GetOAuth2AccessToken/jam_java_oauth2_saml_client_sample )
6. Setup an OAuth client in SAP Jam:
    * https://help.sap.com/viewer/u_collaboration_dev_help/9f86d35bb9594635bd505063bbe76830.html


### Configure Required Keys
* baseUrl - The base url of the Jam site:
  * SAP Jam instance (SAP Cloud Platform account):
    * https://developer.sapjam.com
  * SAP Jam instance (Your own):
    * https://\{YOUR_SAP_JAM_URL\}
* clientKey - The SAP Jam OAuth client key:
  * SAP Jam instance (Your own or SAP Cloud Platform account):
    * https://help.sap.com/viewer/u_collaboration_dev_help/9f86d35bb9594635bd505063bbe76830.html
* idpId - Identifier for the SAML trusted IDP:
  * SAP Cloud Platform account:
    1. Select "Trust" > "Trust Management"
    2. Copy all content in the "Local Provider Name" text field
  * SAP Jam instance (Your own or SAP Cloud Platform account): 
    1. Select "Admin" > "Integrations" > "SAML Local Identity Provider"
    2. Copy all content in the "Issuer" text field
* subjectNameId - The identifier for the user. Can be an email address or a unique identifier, depending on the company type:
  * SAP Jam instance (Your own or SAP Cloud Platform account):
    1. Select "Admin" > "Users" > "Users & Member Lists"
    2. Copy an email address in the "Email" column
* subjectNameIdFormat - "email" or "unspecified"
* idpPrivateKey - Generate a Base64 encoded IDP private key:
  * SAP Cloud Platform account:
    1. Select "Trust" > "Local Service Provider"
    2. Click "Edit"
    3. Select "Configuration Type" > "Custom"
    4. Copy all content in the "Signing Key" text field
      * If a private/public key has not been generated:
        1. Click "Generate Key Pair"
        2. Copy all content in the "Signing Key" text field
        3. Click "Save"
  * SAP Jam instance (Your own or SAP Cloud Platform account):
    1. **Warning:** Do not use this if SAP Jam already has integrations that SAP Jam as a "SAML Local Identity Provider". This will break existing integrations
    2. Select "Admin" > "Integrations" > "SAML Local Identity Provider"
    3. Click "Generate Key Pair"
    4. Copy all content in the "Signing Private Key (Base64)" text field
    5. Click "Save changes"


### Configure Optional Keys
* clientSecret - The OAuth client secret:
  * Not recommended.
  * If supplied the clientKey is not included in the SAML assertion as an attribute.
  * It is recommended to not supply the clientSecret, but it is included to show how SAML IDPs that cannot add assertion attributes can work with the OAuth SAML2 bearer flow.
* subjectNameIdQualifier:
  * Use the value "www.successfactors.com" for SuccessFactors integrated companies when using the 'unspecified' name id format.


### Setup the Eclipse Run Configuration
Run this project by invoking the "main" method of the OAuth2SAMLWorkflowSample class and supplying command line arguments of the form "key1=value1", "key2=value2", "key3=value3" ... in the "Argument's" tab of Eclipse's "Run Configuration":
  1. Right Click on the "main" method of the OAuth2SAMLWorkflowSample class
  2. Select "Run Configuration"
  3. Click the "Argument's" tab
  4. Configure the "Program Arguments" Text Field:
     * Use a SAP Cloud Platform account as your SAML Trusted IDP:
       * baseUrl=https://developer.sapjam.com clientKey=\{SAP JAM OAUTH CLIENT KEY\} idpId=\{LOCAL PROVIDER NAME\} subjectNameId=\{USERNAME EMAIL ADDRESS\} subjectNameIdFormat=email idpPrivateKey=\{SIGNING KEY\}
     * Use your own Base64 encoded IDP private key and use SAP Jam as your SAML Trusted IDP:
       * baseUrl=https://\{YOUR SAP JAM URL\} clientKey=\{SAP JAM OAUTH CLIENT KEY\} idpId=\{SAP JAM SAML TRUSTED IDP\} subjectNameId=\{USERNAME EMAIL ADDRESS\} subjectNameIdFormat=email idpPrivateKey=\{BASE64 ENCODED IDP PRIVATE KEY\}
  5. Configure the VM Arguments Text Field (Optional Proxy configuration):
     * -Dhttp.proxyHost=\{HTTP PROXY HOST\}
     * -Dhttp.proxyPort=\{HTTP PROXY PORT\}
     * -Dhttps.proxyHost=\{HTTPS PROXY HOST\}
     * -Dhttps.proxyPort=\{HTTPS PROXY PORT\}


### Run this Project
Run this project as a Java Application using the Run Configuration you just setup.


### Result
The program will print the OAuth access token to the console. This OAuth access token can then be used to easily make calls to the OData API.

Example 1:
1. Program creates an OAuth access token of "987sSDL8cxbm323LKJ091Mkjdvy820sdcvKbCD2q" from https://developer.sapjam.com
2. Use the following curl command to get the profile of the user authenticated by this OAuth access token in JSON format:
   * curl https://developer.sapjam.com/api/v1/OData/Self -H "Authorization: OAuth 987sSDL8cxbm323LKJ091Mkjdvy820sdcvKbCD2q" -H "Accept: application/json"


### Sample
Here is a sample run configuration for the user blue@berry.com on a test company (berry.com) on developer.sapjam.com that will create an OAuth2 access token for the user blue@berry.com with the value of "SAurLKj98s7fdOHFljdshfs98dsjdfhDFSDFAksq". This test company has:
* An OAuth Client setup in SAP Jam
* A user named blue@berry.com setup in SAP Jam 


### Arguments
* Program Arguments:
  * baseUrl=https://developer.sapjam.com clientKey=\{CLIENT KEY\} idpId=\{IDP ID\} subjectNameId=blue@berry.com subjectNameIdFormat=email idpPrivateKey=\{IDP PRIVATE KEY\}
* VM Arguments
  * -Dhttps.proxyHost=\{HTTPS PROXY HOST\} -Dhttps.proxyPort=\{HTTPS PROXY PORT\}

Screenshot from Eclipse run configuration:
![alt text](https://raw.githubusercontent.com/SAP/SAPJamSampleCode/master/OAuth2SAML/RunConfiguration.png "Run Configuration")


### Program output
Proxy settings:

http.proxyHost=null

http.proxyPort=null

https.proxyHost=\{HTTPS PROXY HOST\}

https.proxyPort=\{HTTPS PROXY PORT\}

Command line arguments: {baseUrl=https://developer.sapjam.com, clientKey=\{CLIENT KEY\}, idpId=\{IDP ID\}, subjectNameId=blue@berry.com, subjectNameIdFormat=email, idpPrivateKey=\{IDP PRIVATE KEY\}

***************************************************************

POST https://developer.sapjam.com/api/v1/auth/token

SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".

SLF4J: Defaulting to no-operation (NOP) logger implementation

SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.

Signed assertion: <?xml version="1.0" encoding="UTF-8"?>

<saml2:Assertion ID="S9mbIjd9-7391-a2j4-0492-m83KldMp06G3" IssueInstant="2016-05-24T21:30:29.238Z" Version="2.0" xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" xmlns:xs="http://www.w3.org/2001/XMLSchema"><saml2:Issuer>bo.ilic.test.idp</saml2:Issuer><ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#"><ds:SignedInfo><ds:CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/><ds:SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><ds:Reference URI="#S9mbIjd9-7391-a2j4-0492-m83KldMp06G3"><ds:Transforms><ds:Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/><ds:Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"><ec:InclusiveNamespaces PrefixList="xs" xmlns:ec="http://www.w3.org/2001/10/xml-exc-c14n#"/></ds:Transform></ds:Transforms><ds:DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><ds:DigestValue>JK3wsqeaWOYrB+Nwmq9gmQJK4E0=</ds:DigestValue></ds:Reference></ds:SignedInfo><ds:SignatureValue>kjh2werVBSWe456409scxkljsldfSDFJ3Vlkn+lkjsdflkj/LKjslkjl909asdz/kizcFDSOInhjhsdf+lkjlbxkjvcx98dbxcbc/sdflkjIDUFSUQWE+xcvjlxerEWRaLKSJVSLKJSV3432/SDFLKJXL092809809fsbj+lkjcblkjxwu32nlvjnds09u09cuvi890u78+lhsblhlk231423/Slkvchxlvk234lkjslkfdspoibv+098XCXZCdbvcxbc+sdflkjhlkj3wrnvs/987xbzoisufmnwDVDSFDSFD+89oiuhwac09809LJLKJx09KJLKJpwqexcv3Zsdf==</ds:SignatureValue></ds:Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress">blue@berry.com</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer"><saml2:SubjectConfirmationData NotOnOrAfter="2016-05-24T21:40:29.238Z" Recipient="https://developer.sapjam.com/api/v1/auth/token"/></saml2:SubjectConfirmation></saml2:Subject><saml2:Conditions NotBefore="2016-05-24T21:20:29.238Z" NotOnOrAfter="2016-05-24T21:40:29.238Z"><saml2:AudienceRestriction><saml2:Audience>cubetree.com</saml2:Audience></saml2:AudienceRestriction></saml2:Conditions><saml2:AttributeStatement><saml2:Attribute Name="client_id"><saml2:AttributeValue xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">j23DSF65dkj402DS3nz1</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>

Request body: client_id=\{CLIENT ID\}&grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Asaml2-bearer&assertion=\{ASSERTION\}

HTTP response code: 200

Response body: {"access_token":\{ACCESS TOKEN\}}

OAuth access token: \{OAUTH ACCESS TOKEN\}


# License
Copyright 2014, SAP AG

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


