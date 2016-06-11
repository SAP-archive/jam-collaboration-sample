/*
License
Copyright 2014, SAP AG
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/

// Calls and displays all endpoints for all osapi.people API calls:
// - osapi.people.getViewer()
// - osapi.people.getOwner()
// - osapi.people.getViewerFriends()
// - osapi.people.getOwnerFriends()
// - osapi.people.get()
function makeOSAPIpeopleCall(){
  
  // Content and Profile Gadgets:
  //  - Creates an object (data) that contains Person information about the user currently
  //    viewing the gadget
  osapi.people.getViewer().execute(function(data) {
    
    // Shows the "data" object in the console
    console.log(data);

    // Creates HTML that shows all osapi.people.getViewer endpoints for the "data" object
    var osapiOutput = "";
    osapiOutput += "<p></p>";
    osapiOutput += "<p>------------------------------------------------------------------------</p>";
    osapiOutput += "<p><h2>osapi.people.getViewer</h2></p>";
    osapiOutput += "<ul>";
    osapiOutput += "<li>data.<b>id</b> = " + data.id + "</li>";
    osapiOutput += "<li>data.<b>displayName</b> = " + data.displayName + "</li>";
    osapiOutput += "<li>data.<b>name.givenName</b> = " + data.name.givenName + "</li>";
    osapiOutput += "<li>data.<b>name.familyName</b> = " + data.name.familyName + "</li>";
    osapiOutput += "<li>data.<b>name.formatted</b> = " + data.name.formatted + "</li>";
    osapiOutput += "<li>data.<b>emails[0].type</b> = " + data.emails[0].type + "</li>";
    osapiOutput += "<li>data.<b>emails[0].value</b> = " + data.emails[0].value + "</li>";
    osapiOutput += "<li>data.<b>thumbnailUrl</b> = " + data.thumbnailUrl + "</li>";
    osapiOutput += "<li>data.<b>photos[0].type</b> = " + data.photos[0].type + "</li>";
    osapiOutput += "<li>data.<b>photos[0].value</b> = " + data.photos[0].value + "</li>";
    osapiOutput += "</ul>";
    osapiOutput += "<p>------------------------------------------------------------------------</p>";
    osapiOutput += "<p></p>";

    // Appends HTML to the <body> element
    document.body.insertAdjacentHTML("beforeend", osapiOutput);
  });

  // Content Gadgets:
  //  - Creates an object (data) that contains Person information about the user who is 
  //    the owner of the gadget
  // Profile Gadgets:
  //  - Creates an object (data) that contains Person information about the user whose 
  //    profile is hosting the gadget
  osapi.people.getOwner().execute(function(data) {
    
    // Shows the "data" object in the console
    console.log(data);

    // Creates HTML that shows all osapi.people.getOwner endpoints for the "data" object
    var osapiOutput = "";
    osapiOutput += "<p></p>";
    osapiOutput += "<p>------------------------------------------------------------------------</p>";
    osapiOutput += "<p><h2>osapi.people.getOwner</h2></p>";
    osapiOutput += "<ul>";
    osapiOutput += "<li>data.<b>id</b> = " + data.id + "</li>";
    osapiOutput += "<li>data.<b>displayName</b> = " + data.displayName + "</li>";
    osapiOutput += "<li>data.<b>name.givenName</b> = " + data.name.givenName + "</li>";
    osapiOutput += "<li>data.<b>name.familyName</b> = " + data.name.familyName + "</li>";
    osapiOutput += "<li>data.<b>name.formatted</b> = " + data.name.formatted + "</li>";
    osapiOutput += "<li>data.<b>emails[0].type</b> = " + data.emails[0].type + "</li>";
    osapiOutput += "<li>data.<b>emails[0].value</b> = " + data.emails[0].value + "</li>";
    osapiOutput += "<li>data.<b>thumbnailUrl</b> = " + data.thumbnailUrl + "</li>";
    osapiOutput += "<li>data.<b>photos[0].type</b> = " + data.photos[0].type + "</li>";
    osapiOutput += "<li>data.<b>photos[0].value</b> = " + data.photos[0].value + "</li>";
    osapiOutput += "</ul>";
    osapiOutput += "<p>------------------------------------------------------------------------</p>";
    osapiOutput += "<p></p>";

    // Appends HTML to the <body> element
    document.body.insertAdjacentHTML("beforeend", osapiOutput);
  });

  // Content Gadgets:
  //  - Creates a collection object (data) that contains Person information about each 
  //    Person in the group in which the gadget has been added, including the viewer
  // Profile Gadgets:
  //  - Creates a collection object (data) that contains Person information about each 
  //    Person that the viewer is following
  osapi.people.getViewerFriends().execute(function(data) {
    
    // Shows the "data" object in the console
    console.log(data);

    // Creates HTML
    var osapiOutput = "";
    osapiOutput += "<p></p>";
    osapiOutput += "<p>------------------------------------------------------------------------</p>";
    osapiOutput += "<p><h2>osapi.people.getViewerFriends:</h2></p>";
    osapiOutput += "<ul>";

    // Creates HTML that shows all osapi.people.getViewerFriends endpoints for each 
    // person in the "data" object
    for (i = 0; i < data.list.length; i++) {
      osapiOutput += "<li>data.list[" + i + "].<b>id</b> = " + data.list[i].id + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>displayName</b> = " + data.list[i].displayName + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>givenName</b> = " + data.list[i].name.givenName + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>familyName</b> = " + data.list[i].name.familyName + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>formatted</b> = " + data.list[i].name.formatted + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>emails[0].type</b> = " + data.list[i].emails[0].type + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>emails[0].value</b> = " + data.list[i].emails[0].value + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>thumbnailUrl</b> = " + data.list[i].thumbnailUrl + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>photos[0].type</b> = " + data.list[i].photos[0].type + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>photos[0].value</b> = " + data.list[i].photos[0].value + "</li>";
    }
    osapiOutput += "</ul>";
    osapiOutput += "<p>------------------------------------------------------------------------</p>";
    osapiOutput += "<p></p>";

    // Appends HTML to the <body> element
    document.body.insertAdjacentHTML("beforeend", osapiOutput);
  });

  // Content Gadgets:
  //  - Creates a collection object (data) that contains Person information about each Person 
  //    that is a member of the group in which the gadget has been added, including the owner
  // Profile Gadgets:
  //  - Creates a collection object (data) that contains Person information about each Person 
  //    that the owner is following
  osapi.people.getOwnerFriends().execute(function(data) {

    // Shows the object (data) in the console
    console.log(data);

    // Creates HTML that shows all osapi.people.getOwnerFriends endpoints
    var osapiOutput = "";
    osapiOutput += "<p></p>";
    osapiOutput += "<p>------------------------------------------------------------------------</p>";
    osapiOutput += "<p><h2>osapi.people.getOwnerFriends:</h2></p>";
    osapiOutput += "<ul>";

    // Creates HTML that shows all osapi.people.getOwnerFriends endpoints for each 
    // person in the "data" object
    for (i = 0; i < data.list.length; i++) {
      osapiOutput += "<li>data.list[" + i + "].<b>id</b> = " + data.list[i].id + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>displayName</b> = " + data.list[i].displayName + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>givenName</b> = " + data.list[i].name.givenName + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>familyName</b> = " + data.list[i].name.familyName + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>formatted</b> = " + data.list[i].name.formatted + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>emails[0].type</b> = " + data.list[i].emails[0].type + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>emails[0].value</b> = " + data.list[i].emails[0].value + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>thumbnailUrl</b> = " + data.list[i].thumbnailUrl + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>photos[0].type</b> = " + data.list[i].photos[0].type + "</li>";
      osapiOutput += "<li>data.list[" + i + "].<b>photos[0].value</b> = " + data.list[i].photos[0].value + "</li>";
    }
    osapiOutput += "</ul>";
    osapiOutput += "<p>------------------------------------------------------------------------</p>";
    osapiOutput += "<p></p>";

    // Appends HTML to the <body> element
    document.body.insertAdjacentHTML("beforeend", osapiOutput);
  });

  // Content and Profile Gadgets:
  //  - Creates an object (dataForID) that contains Person information about the user currently
  //    viewing this gadget
  osapi.people.getViewer().execute(function(dataForID) {
    
    // Shows the "dataForID" object in the console
    console.log(dataForID);

    // Creates an object (data) that contains Person information of the user specified by
    // the "dataForID" object (dataForID.id)
    osapi.people.get({"userId": dataForID.id}).execute(function(data) {
      
      // Shows the "data" object in the console
      console.log(data);

      // Creates HTML that shows all osapi.people.get endpoints for the "data" object
      var osapiOutput = "";
      osapiOutput += "<p></p>";
      osapiOutput += "<p>------------------------------------------------------------------------</p>";
      osapiOutput += "<p><h2>osapi.people.get</h2></p>";
      osapiOutput += "<p><b>using the id from osapi.people.getViewer</b></p>";
      osapiOutput += "<p><b>osapi.people.getViewer.dataForID.<b>id</b> = " + dataForID.id + "</b></p>";
      osapiOutput += "<ul>";
      osapiOutput += "<li>data.<b>id</b> = " + data.id + "</li>";
      osapiOutput += "<li>data.<b>displayName</b> = " + data.displayName + "</li>";
      osapiOutput += "<li>data.<b>name.givenName</b> = " + data.name.givenName + "</li>";
      osapiOutput += "<li>data.<b>name.familyName</b> = " + data.name.familyName + "</li>";
      osapiOutput += "<li>data.<b>name.formatted</b> = " + data.name.formatted + "</li>";
      osapiOutput += "<li>data.<b>emails[0].type</b> = " + data.emails[0].type + "</li>";
      osapiOutput += "<li>data.<b>emails[0].value</b> = " + data.emails[0].value + "</li>";
      osapiOutput += "<li>data.<b>thumbnailUrl</b> = " + data.thumbnailUrl + "</li>";
      osapiOutput += "<li>data.<b>photos[0].type</b> = " + data.photos[0].type + "</li>";
      osapiOutput += "<li>data.<b>photos[0].value</b> = " + data.photos[0].value + "</li>";
      osapiOutput += "</ul>";
      osapiOutput += "<p>------------------------------------------------------------------------</p>";
      osapiOutput += "<p></p>";

      // Appends HTML to the <body> element
        document.body.insertAdjacentHTML("beforeend", osapiOutput);
    });
  });
}