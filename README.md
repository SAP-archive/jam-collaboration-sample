# SAPJamSampleCode
A collection of simple sample code containing examples that demonstrate simple API and integration workflows.


### Jam OAuth2 Access Token from SAML Bearer Assertion Sample
* Source and documentation are located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/OAuth2SAML
* This sample provides some working Java sample client code to illustrate authentication of the SAP Jam API using an OAuth2 access token obtained from a SAML2 bearer assertion as described here:
  * http://help.sap.com/download/documentation/sapjam/developer/#odata/concepts/Auth-OAuth2andSAMLBearerAssertions.html

### OpenSAP2015 - Unit 3
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/OpenSAP2015
* Source code for the Open SAP Extending your HANA Cloud Platform with Jam Week, unit 3 demo.
  * Requirements – an account on SAP HANA Cloud Platform Trial Account with the SAP Jam developer edition enabled.
* These Java server pages uses your SAP HANA Cloud Platform and SAP Jam instance via OAuth2SAMLBearerAssertion to:
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

### ODATA API Integration with iOS Swift 2.x
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/iOS_Integrations/mobile-sdk-ios/Swift 2.x
* Source code for the "ODATA API Integration with iOS Swift 2.x" tutorial in the "SAP Jam Collaboration Mobile Developer Guide".

### OpenSocial/Gadget/HCP_Lumira
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/OpenSocial/Gadget/HCP_Lumira
* Source code for the OpenSocial gadget in "Using OpenSocial gadgets with SAP Jam on SAP HANA Cloud Platform" in the "SAP Jam Developer Guide".
* The related documentation is located at: http://help.sap.com/download/documentation/sapjam/developer/index.html#hcp/concepts/INTRO-OpenSocial_HCP.html

### OpenSocial/Gadget/Localization
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/OpenSocial/Gadget/Localization
* Source code for the OpenSocial gadget in "Applying String Localization to OpenSocial Gadgets" in the "SAP Jam Developer Guide".
* The related documentation is located at: http://help.sap.com/download/documentation/sapjam/developer/index.html#opensocial/concepts/LOCALE-StringLocalization.html

### OpenSocial/Gadget/Search/Search_Jam_Appdata
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/OpenSocial/Gadget/Search/Search_Jam_Appdata
* Source code for the OpenSocial gadget in "Adding OpenSocial Gadget Data to SAP Jam Search" in the "SAP Jam Developer Guide".
* The related documentation is located at: http://help.sap.com/download/documentation/sapjam/developer/index.html#opensocial/concepts/JAM_SEARCH-OpenSocial_Gadget_Data.html

### OpenSocial/Gadget/Tutorial
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/OpenSocial/Gadget/Tutorial
* Source code required for completing the "SAP Jam OpenSocial Tutorial" in the "SAP Jam Developer Guide".
* The "SAP Jam OpenSocial Tutorial" is a set of 7 lessons for developing a simple SAP Jam OpenSocial gadget.
* This tutorial is located at: http://help.sap.com/download/documentation/sapjam/developer/index.html#opensocial/concepts/tutorial-Intro.html

### SAP Jam Java Data Api Sync Client Sample
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/jam_java_odata_api_sync_client_sample
* The servlet demonstrates how to download content from one instance of SAP JAM and upload the content to another instance of SAP JAM.

### SAP_Jam_OData_HCP
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/SAP_Jam_OData_HCP
* Source code for the "Using the SAP Jam API to access data in Jam via OData" tutorial in the "SAP Jam Developer Guide".
  * http://help.sap.com/download/documentation/sapjam/developer/index.html#hcp/concepts/ADVANCED_TOPICS-API_integrate_features_data.html
  * Requirements - An account on a SAP Jam instance and an associated SAP HANA Cloud Platform trial account.
* This Java servlet uses your SAP HANA Cloud Platform and SAP Jam instance via OAuth2SAMLBearerAssertion to:
  * Get general information from SAP Jam about the currently logged-in user and the list of groups that user belongs to.
  * Get group specific information from SAP Jam.
  * Create a single-use token for SAP Jam div-embedded widget authentication.

### Widgets/Div/Feed
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/Widgets/Div/Feed
* Source code for the SAP Jam div-embedded feed widgets in "Using SAP Jam with embeddable widgets" in the "SAP Jam Developer Guide". These feed widgets demonstrate the following authentication types:
  * SuccessFactors IdentityProvider
  * Single Use Token
  * Pre-existing SAP Jam session
  * SAP Jam session with Sign In
  * SAP Jam session with Sign In pop-up
* The related documentation is located at: http://help.sap.com/download/documentation/sapjam/developer/index.html#hcp/concepts/INTRO-embeddable_widgets.html

### Webhooks Sample
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/Webhooks/Java/JamWebHooksTester4j
* Source code for the "Webhooks - Alias Users" and "Webhooks - Groups" tutorial in the "SAP Jam Developer Guide".
  * Requirements - An account on a SAP Jam instance and an associated SAP HANA Cloud Platform trial account.
* This Java EE server implements a simple endpoint for SAP Jam Push Notifications to use as a callback URL.

### Webhooks WAR File Sample
* WAR file is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/Webhooks/Java/SAPJamSampleWebhooksServer
* WAR file for the "Webhooks - Alias Users - WAR file" and "Webhooks - Groups - WAR file" tutorial in the "SAP Jam Developer Guide".
  * Requirements - An account on a SAP Jam instance and an associated SAP HANA Cloud Platform trial account.
* This Java EE server implements a simple endpoint for SAP Jam Push Notifications to use as a callback URL.

### WebView HTML5 Client Integration with Android
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/Android_Integrations/mobile-sdk-android
* Source code for the "WebView HTML5 Client Integration with Android" tutorial in the "SAP Jam Collaboration Mobile Developer Guide".

### WebView HTML5 Client Integration with iOS Swift 2.x
* Source is located here:
  * https://github.com/SAP/SAPJamSampleCode/tree/master/iOS_Integrations/mobile-sdk-ios/Swift 2.x
* Source code for the "WebView HTML5 Client Integration with iOS Swift 2.x" tutorial in the "SAP Jam Collaboration Mobile Developer Guide".


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
