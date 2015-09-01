(function($) {
  var GroupList = React.createClass({displayName: "GroupList",
    // Standard React API
    render: function() {
      var self = this;
      var prefs = new gadgets.Prefs();

      return (
        React.createElement("div", null, 
          React.createElement("div", null, prefs.getMsg('title')), 
          React.createElement("ul", null, 
          
            $.map(this.getGroups(), function(group, index) {
              return (React.createElement("li", {key: group.Id}, group.Name));
            })
          
          )
        )
      );
    },
    getInitialState: function() {
      return {data: {d: {results: []}}, users: []};
    },
    componentDidMount: function() {
      this.loadGroups();
      gadgets.window.adjustHeight();
    },
    componentDidUpdate: function(prevProps, prevState) {
      gadgets.window.adjustHeight();
    },
    // Gadget Calling SAP Jam API
    // 1. Request resources (OAuth 2.0  Service Name, OData request)
    // 7. Response (OData - JSON, XML)
    // 
    // gadgets.io.makeRequest Parameters
    // ---------------------------------
    // gadgets.io.makeRequest(url, callback, opt_params);
    // url: OData Call URL
    // callback: Callback function used to process the response.
    // opt_params: Additional OData proxy request parameters (shown below):
    //    AUTHORIZATION: The type of authentication to use when fetching the content.
    //    OAUTH_SERVICE_NAME: The nickname the gadget uses to refer to the OAuth <Service> element from its XML spec.
    //    CONTENT_TYPE: The type of content to retrieve at the specified URL.
    loadGroups: function() {
      var self = this;
      gadgets.io.makeRequest("https://CALL_TO_YOUR_ODATA_SERVICE_PROVIDER",
        function(result) {
          console.log(result);
          self.setState({data: result.data, users: self.state.users});
        },
        {
          AUTHORIZATION: 'OAUTH2',
          OAUTH_SERVICE_NAME: 'YOUR_OAUTH_SERVICE_NAME',
          CONTENT_TYPE: gadgets.io.ContentType.JSON
        });
    },
    getGroups: function() {
      return this.state.data.d.results;
    }
  });

  gadgets.util.registerOnLoadHandler(function() {
    React.render(
      React.createElement(GroupList, null),
      document.body
    );
  });
})(jQuery);