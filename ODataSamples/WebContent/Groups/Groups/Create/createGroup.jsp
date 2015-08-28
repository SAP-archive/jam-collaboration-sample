<%@page import="org.apache.http.util.EntityUtils"%>
<%@page import="org.apache.http.client.methods.HttpPost"%>
<%@page import="org.apache.http.client.methods.HttpGet"%>
<%@page import="org.apache.http.client.HttpClient"%>
<%@page import="org.apache.http.HttpEntity"%>
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

	//This is the end point that we are sending the request to
	HttpPost jamRequest = new HttpPost("/api/v1/OData/Groups" );
	
	//Setting the request format to xml and response format to json
	jamRequest.addHeader("Content-Type", "application/xml");
	jamRequest.addHeader("Accept", "application/json");
	
	//Creating the message to send as xml
	String xmlToSend = "<Name>" +  request.getParameter("groupName") + "</Name>";
	HttpEntity entity = new ByteArrayEntity(xmlToSend.getBytes("UTF-8"));
		
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