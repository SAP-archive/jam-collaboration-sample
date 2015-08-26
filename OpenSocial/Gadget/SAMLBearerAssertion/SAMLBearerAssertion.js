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
    // Jam interacting with target servers
    // via OpenSocial OAuth 2.0 SAML 2.0 Bearer Assertion Flow
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