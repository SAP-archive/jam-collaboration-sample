/*
License
Copyright 2014, SAP AG
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/
// API Reference to gadgets.io:
// http://help.sap.com/download/documentation/sapjam/developer/index.html#opensocial/references/JSRef-gadgets_io.html
var URL = "https://CALL_TO_YOUR_ODATA_SERVICE_PROVIDER"
var REQUEST_OPTIONS = {
        AUTHORIZATION: 'OAUTH2',
        OAUTH_SERVICE_NAME: "YOUR_OAUTH_SERVICE_NAME",
        CONTENT_TYPE: gadgets.io.ContentType.JSON
    };
var GADGET_PREFS = new gadgets.Prefs();

var Groups = {
    data: m.prop([]),
    fetch: function() {
        gadgets.io.makeRequest(URL,
            function (result) {
                console.log(result);
                Groups.data(result.data.d.results);
                m.redraw();
                gadgets.window.adjustHeight();
            }, REQUEST_OPTIONS);
        return this.data;
    }
};

var GroupList = {
    controller: function() {
        return {
            title: m.prop(GADGET_PREFS.getMsg("title")),
            groups: Groups.fetch()
        };
    },
    view: function(ctrl) {
        return m("div", [
                m("div", ctrl.title()),
                m("ul", ctrl.groups().map(function (group) {
                    return m("li", {key: group.Id}, group.Name);
                }))
            ]);
    }
};

gadgets.util.registerOnLoadHandler(function() {
    m.mount(document.body, GroupList);
});