# SAPJamSampleCode
A collection of simple sample code containing examples that demonstrate simple API and integration workflows.

### SAP Cloud Platform Web IDE and Jam Widget Samples
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/hcp_samples
* These samples demonstrate how to use SAP Cloud Platform Web IDE with Jam Widgets (Feed, Forum and Group).

### Jam OAuth2 Access Token from SAML Bearer Assertion Sample
* Source and documentation are located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/OAuth2SAML
* This sample provides some working Java sample client code to illustrate authentication of the SAP Jam API using an OAuth2 access token obtained from a SAML2 bearer assertion as described here:
  * http://help.sap.com/download/documentation/sapjam/developer/index.html#c6813927839541a19e4703c3a2564f1b.html

### OpenSAP2015 - Unit 3
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/OpenSAP2015
* Source code for the Open SAP Extending your SAP Cloud Platform with Jam Week, unit 3 demo.
  * Requirements – an account on SAP Cloud Platform Trial Account with the SAP Jam developer edition enabled.
* These Java server pages uses your SAP Cloud Platform and SAP Jam instance via OAuth2SAMLBearerAssertion to:
  * Get general information from SAP Jam about the currently logged-in user and the list of groups that user belongs to.
  * Get group specific information from SAP Jam
  * Create a single-use token for SAP Jam div-embedded widget authentication
  * Create a group
  * Create a group with a Primary External Object

### OAuth_1_HMAC-SHA1
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/OAuth_1_HMAC-SHA1
* Provides authentication of the SAP Jam API with OAuth1.0 and the HMAC-SHA1 signature type.
* This example assumes a Jam deployment with an OAuth client application configured for a company, and a user that can successfully log into the company via the Web UI to authorize the OAuth client to make API requests on behalf of the user.
* Consists of the following Eclipse Projects:
  * jam_java_oauth1_client
    * Client library that provides authentication of the SAP Jam API with OAuth1.0 and the HMAC-SHA1 signature type.
    * Dependencies:
      * junit.jar - http://junit.org/
  * jam_java_oauth1_hmac_sha1_client_sample
     * Sample client code that uses the jam_java_oauth1_client library to illustrate authentication of the SAP Jam API with OAuth1.0 and the HMAC-SHA1 signature type.

### ODATA API Integration with Android
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/Android_Integrations/mobile-sdk-android
* Source code for the "ODATA API Integration with Android" tutorial in the "SAP Jam Collaboration Mobile Developer Guide".
* The related documentation is located at: http://help.sap.com/download/documentation/sapjam/sap_jam_mobile_developer_help/index.html#f4ab0401001746eba19c6934e937685f.html

### ODATA API Integration with iOS Swift 2.x
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/iOS_Integrations/mobile-sdk-ios/Swift%202.x
* Source code for the "ODATA API Integration with iOS Swift 2.x" tutorial in the "SAP Jam Collaboration Mobile Developer Guide".
* The related documentation is located at: http://help.sap.com/download/documentation/sapjam/sap_jam_mobile_developer_help/index.html#3283220b3f7d4287a1baf34f8ac433de.html

### OpenSocial/Gadget/HCP_Lumira
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/OpenSocial/Gadget/HCP_Lumira
* Source code for the OpenSocial gadget in "Integrate OpenSocial Gadgets into SAP Jam Collaboration" in the "SAP Jam Developer Guide".
* The related documentation is located at: http://help.sap.com/download/documentation/sapjam/developer/index.html#1c2351040fe04d59b54d079256b20dbe.html

### OpenSocial/Gadget/Localization
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/OpenSocial/Gadget/Localization
* Source code for the OpenSocial gadget in "Applying String Localization to OpenSocial Gadgets" in the "SAP Jam Developer Guide".
* The related documentation is located at: http://help.sap.com/download/documentation/sapjam/developer/index.html#447fafa76c6d478fb38052a590455038.html

### OpenSocial/Gadget/Search/Search_Jam_Appdata
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/OpenSocial/Gadget/Search/Search_Jam_Appdata
* Source code for the OpenSocial gadget in "Add Gadget Data to Jam Search" in the "SAP Jam Developer Guide".
* The related documentation is located at: http://help.sap.com/download/documentation/sapjam/developer/index.html#bf846daa778445a0a1ddd5698ce2a147.html

### OpenSocial/Gadget/Tutorial
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/OpenSocial/Gadget/Tutorial
* Source code required for completing the "SAP Jam Collaboration OpenSocial Tutorials" in the "SAP Jam Developer Guide".
* The "SAP Jam OpenSocial Tutorial" is a set of 7 lessons for developing a simple SAP Jam OpenSocial gadget.
* This tutorial is located at: http://help.sap.com/download/documentation/sapjam/developer/index.html#9d304bf736cd4514bba2b42393a09ec6.html

### SAP Jam Java Data Api Sync Client Sample
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/jam_java_odata_api_sync_client_sample
* The servlet demonstrates how to download content from one instance of SAP JAM and upload the content to another instance of SAP JAM.

### SAP_Jam_OData_HCP
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/SAP_Jam_OData_HCP
* Source code for the "Integrations using SAP Jam API for applications on SAP Cloud Platform" tutorial in the "SAP Jam Developer Guide".
  * http://help.sap.com/download/documentation/sapjam/developer/index.html#033db47cbaa6404cbb8c2e53a220964d.html
  * Requirements - An account on a SAP Jam instance and an associated SAP Cloud Platform trial account.
* This Java servlet uses your SAP Cloud Platform and SAP Jam instance via OAuth2SAMLBearerAssertion to:
  * Get general information from SAP Jam about the currently logged-in user and the list of groups that user belongs to.
  * Get group specific information from SAP Jam.
  * Create a single-use token for SAP Jam div-embedded widget authentication.

### Widgets/Div/Feed
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/Widgets/Div/Feed
* Source code for the SAP Jam div-embedded feed widgets in "Integrate Embeddable Widgets into SAP Jam Collaboration" in the "SAP Jam Developer Guide". These feed widgets demonstrate the following authentication types:
  * SuccessFactors IdentityProvider
  * Single Use Token
  * Pre-existing SAP Jam session
  * SAP Jam session with Sign In
  * SAP Jam session with Sign In pop-up
* The related documentation is located at: http://help.sap.com/download/documentation/sapjam/developer/index.html#5666158135d749218f6a0cdf82bcf5b1.html

### Webhooks Sample
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/Webhooks/Java/JamWebHooksTester4j
* Source code for the "Webhooks - Alias Users" and "Webhooks - Groups" tutorial in the "SAP Jam Developer Guide".
  * Requirements - An account on a SAP Jam instance and an associated SAP Cloud Platform trial account.
* This Java EE server implements a simple endpoint for SAP Jam Push Notifications to use as a callback URL.
* The related documentation is located at:
  * http://help.sap.com/download/documentation/sapjam/developer/index.html#a711035f7d824819a38764b530e0b5a9.html
  * http://help.sap.com/download/documentation/sapjam/developer/index.html#2e53b94ae1af43ca97c343a2bba684eb.html

### Webhooks WAR File Sample
* WAR file is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/Webhooks/Java/SAPJamSampleWebhooksServer
* WAR file for the "Webhooks - Alias Users - WAR file" and "Webhooks - Groups - WAR file" tutorial in the "SAP Jam Developer Guide".
  * Requirements - An account on a SAP Jam instance and an associated SAP Cloud Platform trial account.
* This Java EE server implements a simple endpoint for SAP Jam Push Notifications to use as a callback URL.
* The related documentation is located at:
  * http://help.sap.com/download/documentation/sapjam/developer/index.html#9ad5bea5f6884699a4f1a07be9b07e2c.html
  * http://help.sap.com/download/documentation/sapjam/developer/index.html#c35773a76b7846e0aa9f432919374b63.html

### WebView HTML5 Client Integration with Android
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/Android_Integrations/mobile-sdk-android
* Source code for the "WebView HTML5 Client Integration with Android" tutorial in the "SAP Jam Collaboration Mobile Developer Guide".
* The related documentation is located at: http://help.sap.com/download/documentation/sapjam/sap_jam_mobile_developer_help/index.html#d52503114d4d4cfb92e40443e312ba23.html

### WebView HTML5 Client Integration with iOS Swift 2.x
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/iOS_Integrations/mobile-sdk-ios/Swift%202.x
* Source code for the "WebView HTML5 Client Integration with iOS Swift 2.x" tutorial in the "SAP Jam Collaboration Mobile Developer Guide".
* The related documentation is located at: http://help.sap.com/download/documentation/sapjam/sap_jam_mobile_developer_help/index.html#2b06ee38ed704834a1cf4fac4a2732c4.html


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
