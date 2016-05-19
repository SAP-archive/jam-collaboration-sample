/*
License
Copyright 2014, SAP AG

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/

function make_SAPJAM_context_Call(){
	gadgets.sapjam.context.get(function(data) {
		console.log(JSON.stringify(data, null, 4));

		/* Begin HTML page */
		var osapiOutput = "";
		osapiOutput += "gadgets.sapjam.context.get --> ";

		/* Adds all the properties of "gadgets.sapjam.context.get" with HTML formatting to a string (osapiOutput). */
		osapiOutput += "context = " + data.context;
		osapiOutput += ", id = " + data.id;
		osapiOutput += ", name = " + data.name;
		osapiOutput += ", readOnly = " + data.readOnly;

		/* End HTML page */
		osapiOutput += ". Open your browser console to view the raw JSON object.";

		/* Appends the string (osapiOutput) to the body of the HTML page. */
		$("body").append(osapiOutput);

		/* Demonstrates every gadgets.sapjam.statusbar interaction */
		setTimeout(showStatusBar, 1000);
		setTimeout(clearBadgeText, 4000);
		setTimeout(hideStatusBar, 7000);
		setTimeout(clearBadgeText, 10000);
		setTimeout(clickToExpand, 13000);
		setTimeout(clearHighlight, 15000);
	});
}

function showStatusBar(){
	/* Highlights the gadget in blue and makes it blink 3 times */
	gadgets.sapjam.statusbar.highlight();

	/* Expands the gadget and shows the contents of the body element within it */
	gadgets.sapjam.statusbar.show();

	/* Adds a red badge with text to the gadget */
	gadgets.sapjam.statusbar.setBadgeText("Statusbar Expanded");
}

function clearBadgeText(){
	gadgets.sapjam.statusbar.clearBadgeText();
	gadgets.sapjam.statusbar.clearHighlight();
}

function hideStatusBar(){
	gadgets.sapjam.statusbar.hide();
	gadgets.sapjam.statusbar.highlight();
	gadgets.sapjam.statusbar.setBadgeText("Statusbar collapsed");
}

function clickToExpand(){
	gadgets.sapjam.statusbar.highlight();
	gadgets.sapjam.statusbar.setBadgeText("Click here");
}

function clearHighlight(){
	gadgets.sapjam.statusbar.clearHighlight();
}

// Initializes gadget
function init() {
	make_SAPJAM_context_Call();
}

// Initializes gadget after receiving a notification that the page is loaded and the DOM is ready.
gadgets.util.registerOnLoadHandler(init);