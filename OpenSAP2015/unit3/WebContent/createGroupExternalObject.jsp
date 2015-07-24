<%@page import = "org.apache.http.util.EntityUtils" %>
<%@page import = "org.apache.http.client.methods.HttpPost" %>
<%@page import = "org.apache.http.client.methods.HttpGet" %>
<%@page import = "org.apache.http.client.HttpClient" %>
<%@page import = "org.apache.http.HttpEntity" %>
<%@page import = "org.apache.http.entity.StringEntity" %>
<%@page import = "org.apache.http.entity.ByteArrayEntity" %>
<%@page import = "com.sap.core.connectivity.api.http.HttpDestination" %>
<%@page import = "javax.naming.Context" %>
<%@page import = "javax.naming.InitialContext" %>
<%
try
{
	Context ctx = new InitialContext();
	HttpDestination destination = (HttpDestination)ctx.lookup("java:comp/env/sap_jam_odata");
	HttpClient client = destination.createHttpClient();

	HttpPost jamRequest = new HttpPost("/api/v1/OData/Groups" );
	String externalObjectID = request.getParameter("ExternalObject");
	String groupName = request.getParameter("groupName");
	
	String jsonToSend = "";
	jsonToSend += "{";
	jsonToSend += "\"Name\": \"" + groupName + "\",";
	jsonToSend += "\"PrimaryExternalObject\":";
	jsonToSend += "{";
	jsonToSend += "\"__metadata\":{\"uri\":\"https://developer.sapjam.com/api/v1/OData/";
	jsonToSend += "ExternalObjects('" + externalObjectID + "')\"}";
	jsonToSend += "}";
	jsonToSend += "}";
	
	
	
	StringEntity entity = new StringEntity(jsonToSend);
	jamRequest.setEntity(entity);
	jamRequest.addHeader("Content-Type", "application/json");
	jamRequest.addHeader("Accept", "application/json");
	
	
	HttpEntity responseEntity = client.execute(jamRequest).getEntity();
	if ( responseEntity != null )
	{
		String responseString = EntityUtils.toString(responseEntity);
		out.println(responseString);
	}
	else
		out.println( "There was a problem with the connection");
}
catch(Exception ex)
{
	System.err.println(ex.toString());
	out.println(ex.toString());
}
%>