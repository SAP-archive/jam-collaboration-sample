/**
 * STEP 2 - Add Javascript that sets callbacks and renders the gadget.
 */
// Renders the gadget
function renderInfo() {
    }

// Sets callbacks
function init() {
    if (wave && wave.isInWaveContainer()) {
        // Loads the gadget's initial state and the subsequent changes to it
        wave.setStateCallback(renderInfo);
        // Loads participants and any changes to them
        wave.setParticipantCallback(renderInfo);
    }
}

/**
 * STEP 3
 * Initialize our gadget by passing init().
 */
// Initializes gadget after receiving a notification that the page is loaded and the DOM is ready.
gadgets.util.registerOnLoadHandler(init); 