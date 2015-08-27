<%@page import="org.apache.http.util.EntityUtils"%>
<%@page import="org.apache.http.client.methods.HttpPost"%>
<%@page import="org.apache.http.client.methods.HttpGet"%>
<%@page import="org.apache.http.client.HttpClient"%>
<%@page import="org.apache.http.HttpEntity"%>
<%@page import="org.apache.http.entity.StringEntity"%>
<%@page import="org.apache.http.entity.ByteArrayEntity"%>
<%@page import="com.sap.core.connectivity.api.http.HttpDestination"%>
<%@page import="javax.naming.Context"%>
<%@page import="javax.naming.InitialContext"%>
<%
try
{
	//This utilizes the destination configuration (with the passed name, "sap_jam_odata") on HANA Cloud Portal  
	//to easily connect to your SAP Jam system.
	Context ctx = new InitialContext();
	HttpDestination destination = (HttpDestination)ctx.lookup("java:comp/env/sap_jam_odata");
	HttpClient client = destination.createHttpClient();

	//Get the parameter values we will use for this request.
	String externalObjectID = request.getParameter("ExternalObject");
	String groupName = request.getParameter("groupName");
	String templateID = request.getParameter("TemplateID");
	
	//This is the end point that we are sending the request to
	HttpPost jamRequest = new HttpPost("/api/v1/OData/Groups" );
	
	//Setting the request and response formats to json
	jamRequest.addHeader("Content-Type", "application/json");
	jamRequest.addHeader("Accept", "application/json");
	
	String jsonToSend = "";
	jsonToSend += "{";
	
	//Creating the json message as a large string to pass in the request
	//This sets the name of the group to create
	jsonToSend += "\"Name\": \"" + groupName + "\",";
	
	//This sets the Group Template to use
	jsonToSend += "\"Template\":";
	jsonToSend += "{";
	jsonToSend += "\"__metadata\":{\"uri\":\"https://developer.sapjam.com/api/v1/OData/";
	jsonToSend += "GroupTemplates(Id='" + templateID + "',GroupTemplateType='custom')\"}";
	jsonToSend += "},";
	
	//This sets the Primary External Object ID
	jsonToSend += "\"PrimaryExternalObject\":";
	jsonToSend += "{";
	jsonToSend += "\"__metadata\":{\"uri\":\"https://developer.sapjam.com/api/v1/OData/";
	jsonToSend += "ExternalObjects('" + externalObjectID + "')\"}";
	jsonToSend += "}";
	jsonToSend += "}";
	
	StringEntity entity = new StringEntity(jsonToSend);
	jamRequest.setEntity(entity);

	HttpEntity responseEntity = client.execute(jamRequest).getEntity();
	if ( responseEntity != null )
	{
		//output the json response		
		String responseString = EntityUtils.toString(responseEntity);
		out.println(responseString);
	}
	else
	{
		//This is an unexpected response
		out.println( "No value returned");
	}
}
catch(Exception ex)
{
	//something went wrong unexpectedly
	//displaying the exception in both the browser and in the system logs as occasionally the 
	//failure will not let us write it to the browser
	System.err.println(ex.toString());
	out.println(ex.toString());
}
%>