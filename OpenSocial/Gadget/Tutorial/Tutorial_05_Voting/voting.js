/**
 * STEP 2 (Implement a voting button)
 * - Get state and the viewer ID
 * - Retrieve votes from storage and convert JSON to an array of votes
 * - Push the viewer to the array of votes for the topic specified in the function parameter
 * - Convert the array to JSON and submit it to storage
 */

function vote(topicId){
    /** Get state and the viewer ID */
    var state = wave.getState();
    var viewerId = wave.getViewer().getId();
    
    /** Retrieve votes from storage and convert JSON to an array of votes */
    var votes = toObject(state.get('votes'));
    
    /** Push the viewer to the array of votes for the topic specified in the function parameter */
    votes[topicId].push(viewerId);
    
    /** Convert the array to JSON and submit it to storage */
    state.submitDelta({'votes' : toJSON(votes)});
}

// Encode object as JSON string
function toJSON(obj) { 
	return gadgets.json.stringify(obj); 
}

// Decode JSON string into an object
function toObject(str) {
    return gadgets.json.parse(str);
}

// Stores data in the OpenSocial gadget
function addInput(){
	// Getting the state
	var state = wave.getState();
	
	// Retrieves topics from storage.
	var jsonString = state.get('topics','[]');
	
	// Converts JSON to an array of topics
	var topics = toObject(jsonString);
	
	// Push textbox value into the array and set the textbox to blank
	topics.push(document.getElementById('textBox').value);
	document.getElementById('textBox').value = '';
	
	// Create an array for the topic and add it to the "master" array.
	var votes = toObject(state.get('votes','[]'));
	votes.push(new Array());
	
	// Submit everything to storage
	state.submitDelta({'topics' : toJSON(topics), 'votes' : toJSON(votes)});
}

/**
 * STEP 3
 * - Add thumbnail rendering
 * - Add button rendering
 */

// Renders the gadget
function renderInfo() {
    // Get state
    if (!wave.getState()) {
        return;
    }
    var state = wave.getState();
    
    // Retrieve topics
    var topics = toObject(state.get('topics','[]'));
    var votes = toObject(state.get('votes','[]'));
    
    // Add topics to the canvas
    var html = "";
    for (var i = 0; i < topics.length; i++){
        var id = "topic"+i;
        html += '<div class="topic"><h4> ' + topics[i] + '</h4></div>';
        
        /** Add thumbnail rendering */
        for (var j = 0; !(typeof votes[i] === 'undefined') && j < votes[i].length; j++){
            var voter = wave.getParticipantById(votes[i][j]);
            var thumbnail = voter.getThumbnailUrl();
            var name = voter.getDisplayName();
            html += '<img id = "thumbnail" title="'+name+'" src="'+thumbnail+'"/>';
        }
        
        /** Add button rendering */
        html += '<button id="voteButton" onclick="vote('+i+')">+</button>';
    }
    document.getElementById('body').innerHTML = html;
    
    // Create "Add topic" button to the footer
    html += '<input type="text" id="textBox" value=""/><button id="addInput" onclick="addInput()">Add Topic</button>';
    document.getElementById('footer').innerHTML = html;
    
    // Adjust window size dynamically
    gadgets.window.adjustHeight();
}

// Initializes gadget, sets callbacks
function init() {
    if (wave && wave.isInWaveContainer()) {
    	// Loads the gadget's initial state and the subsequent changes to it
        wave.setStateCallback(renderInfo);
        
        // Loads participants and any changes to them
        wave.setParticipantCallback(renderInfo);
    }
}

// Initializes gadget after receiving a notification that the page is loaded and the DOM is ready.
gadgets.util.registerOnLoadHandler(init);