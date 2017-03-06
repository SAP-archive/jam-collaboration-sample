### SAP_Jam_OData_HCP
* Source code for the "Using the SAP Jam API to access data in Jam via OData" tutorial in the "SAP Jam Developer Guide".
  * http://help.sap.com/download/documentation/sapjam/developer/index.html#033db47cbaa6404cbb8c2e53a220964d.html
  * Requirements - An account on a SAP Jam instance and an associated SAP Cloud Platform trial account.
* This Java servlet uses your SAP Cloud Platform and SAP Jam instance via OAuth2SAMLBearerAssertion to:
  * Get general information from SAP Jam about the currently logged-in user and the list of groups that user belongs to.
  * Get group specific information from SAP Jam.
  * Create a single-use token for SAP Jam div-embedded widget authentication.


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