<%@page import="org.apache.http.util.EntityUtils"%>
<%@page import="org.apache.http.client.methods.HttpPost"%>
<%@page import="org.apache.http.client.methods.HttpGet"%>
<%@page import="org.apache.http.client.HttpClient"%>
<%@page import="org.apache.http.HttpEntity"%>
<%@page import="org.apache.http.entity.StringEntity"%>
<%@page import="com.sap.core.connectivity.api.http.HttpDestination"%>
<%@page import="javax.naming.Context"%>
<%@page import="javax.naming.InitialContext"%>
<%@page import="java.net.URL"%>
<%
try
{
	//This utilizes the destination configuration (with the passed name, "sap_jam_odata") on HANA Cloud Portal  
	//to easily connect to your SAP Jam system.
	Context ctx = new InitialContext();
	HttpDestination destination = (HttpDestination)ctx.lookup("java:comp/env/sap_jam_odata");
	HttpClient client = destination.createHttpClient();

	//Get the parameter values we will use for this request.
	String groupID = request.getParameter("groupID");
	String contentName = request.getParameter("contentName"); 
	
	//This is the end point that we are sending the request to
	//Description is set as a querystring parameter and the value must be wrapped in single quotes as seen here
	HttpPost jamRequest = new HttpPost("/api/v1/OData/Groups('" + groupID + "')/ContentItems?Description='MyDescription'");
	
	//Setting the response format to json, the request as text, and slug is the file name to use
	jamRequest.addHeader("Content-Type", "text/plain");
	jamRequest.addHeader("Accept", "application/json");
	jamRequest.addHeader("Slug", contentName);
	
	//sending just a simple string and setting the content type as text/plain will actually act like uploading 
	//a text file with this string as the contents.
	String testToSend = "This is a text file";
		
	StringEntity entity = new StringEntity(testToSend);
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