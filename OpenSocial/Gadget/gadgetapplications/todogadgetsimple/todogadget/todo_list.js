var todoController = function() {

  return {
    init: function() {
    	
    	/** Keep track of all the existing 'todo items' that have not been removed */
    	var existingTodos = [];

		/**
		 * Retrieve and display the gadget owner's 'todo items'.
		 * - osapi.people.getOwner retrieves the owner of the gadget.
		 * - osapi.appdata.get retrieves the gadget owner's 'todo items' by specifically calling for the ['todos'] keys.
		 * - osapi.appdata.get retrieves the gadget owner's 'todo items' from the ['todos'] keys.
		 */
	    function getExistingTodos() {

	    	// Retrieves the owner of the gadget
	    	osapi.people.getOwner().execute(function(ownerData) {
              var ownerId = ownerData.id;

              // Retrieves the gadget owner's 'todo items'
	    	  osapi.appdata.get({
	    	  	userId: ownerId,
				keys: ['todos']
			    }).execute(function(todos) {
			      existingTodos = (todos[ownerId] !== undefined && todos[ownerId].todos !== undefined) ? todos[ownerId].todos : [];
			      
			      // Display the gadget owner's 'todo items'
			      displayTodos();
			    });
	    	});
	    }
	    
	    /**
	     * Display each complete/uncomplete 'todo item' in a list of clickable buttons.
	     * - Clicking a button when its button text displays 'Mark as Completed' will:
	     *   - Mark the related 'todo item' as 'Completed'
	     *   - Change the button text to 'Completed'
	     *   - Change the button color to green
	     *   - Change the button text color to white
	     * - Clicking a button when its button text displays 'Completed' will not do anything.
	     */
	    function displayTodos() {

	      // Remove all items from the todo list
	      clearTodos();

		  if (existingTodos.length > 0) {
		  	var id = 1;
			$.each(existingTodos, function(i, val) {
			  var htmlString = null;
			  if (val.completed) {
			  	  htmlString = getCompletedItem(val.name, id);
			  } else {
		          htmlString = getUncompletedItem(val.name, id);		  	
			  }
	          $('#todos').append(htmlString);

			  $('#' + id).click(function() {
				markTodoAsCompleted(val);
			  });
			  id++;
			});
		  }

	      // Adjust the height of the gadget window for added or removed 'todo items'.
		  gadgets.window.adjustHeight();
	    }
	    
	    /**
	     * Return HTML for a completed 'todo item'
	     */
	    function getCompletedItem(name, id) {
	    	return "<div class=\"input-group list-group-item\">" + 
		        	name + 
		           "<span class=\"input-group-btn\"><button type=\"button\" id=\"" +
		        	id +
		           "\"" +
		           "class=\"btn btn-success complete-task\">" +
		           gadgetPrefs.getMsg("completed_item") +
		           "</button></span></div>";
	    }
	    
	    /**
	     * Return HTML for an uncompleted 'todo item'
	     */
	    function getUncompletedItem(name, id) {
	    	return "<div class=\"input-group list-group-item\">" + 
		        	name + 
		           "<span class=\"input-group-btn\"><button type=\"button\" id=\"" +
		        	id +
		           "\"" +
		           "class=\"btn btn-default complete-task\">" +
		           gadgetPrefs.getMsg("uncompleted_item") +
		           "</button></span></div>";	
	    }
	    
	    /**
	     * Remove all items from the todo list
	     */
	    function clearTodos() {
	    	$('#todos').empty();
	    }
	    
	    /**
	     * Open a modal dialog with the HTML rendered for the 'popup-view' open-view.
	     * - gadgets.views.openGadget allows for different content from the gadget defintion to be rendered.
	     *   Refer to <Content type="html" view="popup-view"> in the spec.xml file.
	     */
	    function openView() {
			gadgets.views.openGadget(function(result) {
			  return result ? createTodo(result) : null; 
			}, function(site) {
			  return null;
			}, {
				view: 'popup-view',
				viewTarget: 'MODALDIALOG'
			});
	    }
	    
	    /**
	     * Creates a 'todo item' by updating the existingTodos.
	     * - osapi.appdata.update updates the 'todos' key for the gadget with the new todo added.
	     * - Gets the existing 'todo items' after the update.
	     */
	    function createTodo(todo) {
	    	existingTodos.push(todo);

	    	// Updates the 'todos' key
	        osapi.appdata.update({
	          data: {
	            todos: existingTodos
	          }
	        }).execute(function(updateData) {
	          if (updateData.error) {
	            window.console && console.log(updateData.error.message);
	          }
	        });

	        // Gets the existing 'todo items'
			getExistingTodos();
    	}
    	
    	/**
	     * Removes 'todo items' by updating the existingTodos.
	     * - osapi.appdata.update updates the 'todos' key for the gadget with the remaining gadgets after the
	     *   removal of completed todos.
	     * - Gets the existing 'todo items' after the update.
	     */
    	function removeCompletedTodos() {
    		var newTodos = [];
			$.each(existingTodos, function(i, val) {
				if (!(val.completed)) {
					newTodos.push(val);
				}
			});

			// Updates the 'todos' key
			osapi.appdata.update({
	          data: {
	            todos: newTodos
	          }
	        }).execute(function(updateData) {
	          if (updateData.error) {
	            window.console && console.log(updateData.error.message);
	          }

	          // Gets the existing 'todo items'
			  getExistingTodos();
	        });
    	}
    	
    	
    	/**
	     * Marks a todo item as completed. This marks all tasks with the same description as completed, so
	     * duplicates will be marked as complete.
	     * - Gets the context of the gadget to determine whether the current user has the appropriate
	     *   permissions to complete a task.
	     * - osapi.appdata.update updates the 'todos' key for the gadget with the updated 'todo items'.
	     * - Gets the existing 'todo items' after the update.
	     */
    	function markTodoAsCompleted(todo) {
    		
    		// Gets the context of the gadget
    		gadgets.sapjam.context.get(function(context) {
    			if (!context.readOnly) {
		    		$.each(existingTodos, function(i, val) {
						if (val.name === todo.name) {
							existingTodos[i].completed = true;	
						}
					});

					// Updates the 'todos' key
		    		osapi.appdata.update({
			          data: {
			            todos: existingTodos
			          }
			        }).execute(function(updateData) {
			          if (updateData.error) {
			            window.console && console.log(updateData.error.message);
			          }

			          // Gets the existing 'todo items'
					  getExistingTodos();
			        });    				
    			}	
    		});
    	}
    	
    	/**
	     * Gets all existing 'todo items' and then sets click listeners for the different buttons.
	     */
    	function initGadget() {

    		// Gets all existing 'todo items'
			getExistingTodos();

			// Sets click listeners for the different buttons
			$('#new-todo').click(function() {
				openView();
			});
			$('#remove-completed-todos').click(function() {
				removeCompletedTodos();
			});
    	}
      
        /**
    	 * Establishes the gadget preferences feature
    	 */
    	var gadgetPrefs = new gadgets.Prefs();

        /**
	     * Initializes the gadget after receiving a notification that the page is loaded and the DOM is ready.
	     */
		gadgets.util.registerOnLoadHandler(initGadget);
    }
  };
}();