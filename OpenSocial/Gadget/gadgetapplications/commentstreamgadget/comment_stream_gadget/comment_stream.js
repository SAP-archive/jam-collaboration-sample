var commentStreamController = function() {

  return {
	init: function() {
		
		/** 
		 * Clears all of the current user's drafts.
		 * - Uses wave.getPrivateState().submitDelta(map) to update the private state object (wave) with
		 *   a passed in map of key-values for the current user. This user-specific information is
		 *   private and can only be accessed by that user.
		 */
		function clearDrafts() {
			// Initializes the 'drafts' array for the current user in the private state object (wave)
			wave.getPrivateState().submitDelta({'drafts': []});
			
			$('#drafts').empty();
		}
		
		/** 
		 * Edits a drafted comment and remove it from the list of drafts.
		 * - Uses wave.getPrivateState().submitDelta(map) to update the private state object (wave) with
		 *   a passed in map of key-values for the current user. This user-specific information is
		 *   private and can only be accessed by that user.
		 */
		function editDraft(draft) {
			$('#drafts').empty();
			$('#comment-text').val(draft);
			var currentDrafts = getDrafts();
			var updatedDrafts = [];
			$.each(currentDrafts, function(i, val) {
				if (val !== draft) {
					updatedDrafts.push(val);
				}
			});
			
			// Updates the 'drafts' array with the current user's drafts in the private state object (wave)
			wave.getPrivateState().submitDelta({'drafts': updatedDrafts});
		}
		
		/** Returns the HTML for a comment. */
		function getCommentHtml(comment) {
			return "<div>" + comment + "</div>";
		}
		
		/** Returns the HTML for a draft. */		
		function getDraftHtml(draft, id) {
			return "<div class=\"input-group list-group-item\">" + 
					draft + 
				   "<span class=\"input-group-btn\"><button type=\"button\" id=\"" +
					id +
				   "\"" +
				   "class=\"btn btn-default complete-task\">Edit</button></span></div>";
		}
		
		/**
		 * Renders comments in the comment stream output box.
		 * - Uses gadgets.window.adjustHeight() to resize the gadget's window height to fit added/removed content.
		 */		
		function renderComments(commentsToRender) {
		  $('#output-box').empty();
		  if (commentsToRender.length > 0) {
		  	$.each(commentsToRender, function(i, val) {
		  		var htmlString = getCommentHtml(val);
		  		$('#output-box').append(htmlString);
		  		
		  		// Resizes the gadget's window height to fit content
		  		gadgets.window.adjustHeight();
		  	});
		  }
		}
		
		/**
		 * Renders drafts in the drafts box.
		 * - Uses gadgets.window.adjustHeight() to resize the gadget's window height to fit added/removed content.
		 */ 
		function renderDrafts(draftsToRender) {
		  $('#drafts').empty();
		  if (draftsToRender.length > 0) {
	  		var id = 1;
			$.each(draftsToRender, function(i, val) {
			  var htmlString = getDraftHtml(val, id);
			  $('#drafts').append(htmlString);
			  
			  // Resizes the gadget's window height to fit content
			  gadgets.window.adjustHeight();

			  $('#' + id).click(function() {
				editDraft(val);
			  });
			  id++;
			});
		  }
		}
		
		/**
		 * Gets all drafts for the current user.
		 * - Uses wave.getPrivateState().get(map) to get a map of key-values from the private state object (wave) for the
		 *   current user. This user-specific information is private and can only be accessed by that user.
		 */ 
		function getDrafts() {
			// Gets an array of all drafts for the current user from the private state object (wave)
			var draftsToRender = wave.getPrivateState().get('drafts') === null ? [] : wave.getPrivateState().get('drafts');

			return draftsToRender;
		}
		
		/**
		 * Gets all comments in the shared state object (wave), which applies to all users.
		 * - Uses wave.getState().get(map) to get a map of key-values from the shared state object (wave). All
		 *   information is public and can be accessed by all users.
		 */
		function getComments() {
			// Gets an array of all user comments from the public shared object (wave) 
			var commentsToRender = wave.getState().get('comments') === null ? [] : wave.getState().get('comments');

			return commentsToRender;
		}
		
		/**
		 * 1. Publishes a comment to the shared state object (wave) comment stream.
		 *	- Uses wave.getState().submitDelta(map) to update the shared state object (wave) with a
		 *	  passed in map of key-values. All information is public and can be accessed by all users. 
		 * 2. Removes any draft that was published, as it is no longer a draft.
		 *	- Uses wave.getPrivateState().submitDelta(map) to update the private state object (wave) with
		 *	  a passed in map of key-values for the current user. This user-specific information is
		 *	  private and can only be accessed by that user.
		 * 3. Creates a new comment in the feed of the gadget's Jam group.
		 *	- Uses osapi.activitystreams.create({
		 *			 activity: {
		 *			   title: "titleText", 
		 *			   object: {
		 *				 displayName: "commentText"
		 *			   }
		 *			 }
		 *		   }).execute(callback)
		 *	  to create a new comment in the feed of the gadget's Jam group with a callback from a passed
		 *	  in object (activity).
		 */	 	
		function publishComment() {
			var newComment = $('#comment-text').val();
			if (newComment !== "") {
				var currentComments = getComments();
				currentComments.push(newComment);
				var currentDrafts = getDrafts();
				var updatedDrafts = [];
				$.each(currentDrafts, function(i, val) {
					if (val !== newComment) {
						updatedDrafts.push(val);
					}
				});
				
				// Updates the map with an array of drafts for the current user in the private state object (wave)
				wave.getPrivateState().submitDelta({'drafts': updatedDrafts});
				
				// Updates the map with an array of all user comments from the public shared object (wave) 
				wave.getState().submitDelta({'comments': currentComments});

				// Creates a new feed item from a passed in object (activity).
				// - #{feed_add} gets the message from the English language locale in spec.xml (line 12).
				// - 'newComment' contains the new comment text
				osapi.activitystreams.create({
				  activity: {
					title: "#{feed_add}",
					object: {
					  displayName: newComment
					}
				  }
				}).execute(function(result) {});
			}
		}
		
		/**
		 * Saves a draft for the user.
		 * - Uses wave.getPrivateState().submitDelta(map) to update the private state object (wave) with
		 *   a passed in map of key-values for the current user. This user-specific information is
		 *   private and can only be accessed by that user.
		 */
		function saveDraft() {
			var currentDrafts = getDrafts();
			currentDrafts.push($('#comment-text').val());
			
			// Updates the map with an array of drafts for the current user in the private state object (wave)
			wave.getPrivateState().submitDelta({'drafts': currentDrafts});
		}
		
		/**
		 * Renders updates to 'comments' in the shared state object (wave).
		 * - Uses wave.getState().get(map) to get a map of key-values from the shared state object (wave). All
		 *   information is public and can be accessed by all users.
		 */ 
		function publicStateUpdated() {
			// Gets an array of all user comments from the public shared object (wave) 
			if(wave.getState().get('comments')) {
				var comments = getComments();
				if (comments) {
					renderComments(comments);
				}
			}
		}
		
		/**
		 * Renders updates to 'drafts' in the private state object (wave).
		 * - Uses wave.getPrivateState().get(map) to get a map of key-values from the private state object (wave) for the
		 *   current user. This user-specific information is private and can only be accessed by that user.
		 */ 		
		function privateStateUpdated() {
			// Gets an array of all drafts for the current user from the private state object (wave)
			if(wave.getPrivateState().get('drafts')) {
				var drafts = getDrafts();
				if (drafts) {
					renderDrafts(drafts);
				}
			}
		}
		
		/**
		 * Initializes the gadget by:
		 * 1. Setting up a callback that executes when the shared state object (wave) changes.
		 *	- Uses wave.setStateCallback(publicCallbackFunction) to setup the callback for the shared state
		 *	  object (wave) with a passed-in callback.
		 * 2. Setting up a callback that executes when the private state object (wave) changes.
		 *	- Uses wave.setPrivateStateCallback(privateCallbackFunction) to setup the callback for the private state
		 *	  object (wave) with a passed-in callback.
		 */
		function initGadget() {
		  // Checks if the state object (wave) exists and if the gadget is running in a wave container.
		  if (wave && wave.isInWaveContainer()) {
			  
			// Sets up a callback that executes when the shared state object (wave) changes.
			wave.setStateCallback(publicStateUpdated);
			
			// Sets up a callback that executes when the private state object (wave) changes.
			wave.setPrivateStateCallback(privateStateUpdated);
		  }
		  
		  $('#publish-comment').click(function() {
		  	publishComment();
		  	$('#comment-text').val("");
		  });
		  $('#cancel-comment').click(function() {
		  	$('#comment-text').val("");
		  });
		  $('#save-draft').click(function() {
		  	saveDraft();
		  	$('#comment-text').val("");
		  });
		  $('#clear-drafts').click(function() {
		  	clearDrafts();
		  });
		}
		
		/**
		 * Initializes gadget after receiving a notification that the page is loaded and the DOM is ready.
		 */ 	
		gadgets.util.registerOnLoadHandler(initGadget);
	}
  };
}();