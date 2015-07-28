<%@ page trimDirectiveWhitespaces="true" 
%><%@page contentType="text/html" 
%><%@page import = "org.apache.http.client.methods.HttpPost"
%><%@page import = "org.apache.http.client.methods.HttpGet"
%><%@page import = "org.apache.http.client.HttpClient"
%><%@page import = "org.apache.http.HttpEntity" %><%

String singleUseToken= request.getParameter("singleUseTokenJSON");

if ( singleUseToken == null ){
	out.println( "There is a missing token");
}

%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Create Feed Widget</title>
<style>
html, body {height:100%;}
</style>
</head>
<body>
<div id="myDiv" style="width:100%; height:100%"></div>
<script type="text/javascript" src="https://developer.sapjam.com/assets/feed_widget_v1.js"></script>
<script type="text/javascript">
sapjam.feedWidget.init("https://developer.sapjam.com/widget/v1/feed", "single_use_token");
var w = sapjam.feedWidget.create("myDiv", {type: "follows", avatar: false, post_mode: "inline", reply_mode: "inline", single_use_token: "<%= singleUseToken %>"});
</script>
</body>
</html>

