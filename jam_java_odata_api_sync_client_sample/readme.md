# SAP Jam Java Data Api Sync Client Sample

The jam_java_odata_api_sync_client_sample app demonstrates how to download content from one instance of SAP JAM and upload the content to another instance of SAP JAM.

### Supported Content
The types of content which this sample app accounts for are:
- Documents, images, and various user content
- Content comments and replies
- Forum Questions, answers, and best answers
- Forum Ideas, comments and status
- Forum Discussion, comment, and replies

### Assumptions:
1. Both instances of SAP Jam have the same users provisioned with the same credentials and permissions
2. Apache Maven is installed on your machine

### Configuration: 
To configure the sample app to sync from one SAP Jam instance to another SAP Jam instance, please use the following steps:

1. As a company admin in SAP Jam you need to:
   - create a "SAML Trusted IDP" under the "Admin" > "SAML Trusted IDPs" admin page. You will need the X509 Certificate (Base64) parameter for the idp_private_key configuration parameter.
   - create an "OAuth client" under the "Admin" > "OAuth clients" admin page. You will need the key and secret for the client_id and client_secret configuration parameters.
2. Fill out the "config.json" file with the configuration information for the from and to (source and destination) SAP Jam instances. The following conditions apply:
   - the user used in the source configuration must be a group administrator
   - the user used in both the source and destination configurations must be company administrators 
3. Install the maven dependencies using ```mvn clean install``` from the jam_java_odata_api_sync_client_sample/JamSyncApp directory.
4. The servlet can be run from an IDE or the command line as follows:
	- IDE: Launch and run JamSyncApp.java
	- Command line:
		1. Package the file as a jar using mvn package
		2. Run the jar from the JamSyncApp/target directory using "java -jar jam_java_odata_api_sync_client_sample-0.0.1-SNAPSHOT"
    
You should see members and groups sync with the destination JAM instance. 

### Limitations:
1. Subgroups are not handled.
2. Contents that do not have API endpoints, such as overview layout pages and polls, will be skipped and not handled.
3. Only content that is your own uploaded or created content can be synced. Groups with auto generated contents like "Pro-con tables", etc. will not work.
4. Replies to contents are synced when their target content is synced and not in the absolute order they were posted in. Since the Feed Updates wall is sorted by latest activity (ie. last commented on, liked), content on this wall may be out of order for groups in the destination JAM instance. 



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
