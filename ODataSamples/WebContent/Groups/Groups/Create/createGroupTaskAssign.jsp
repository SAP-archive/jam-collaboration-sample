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
	String taskName = request.getParameter("taskName");
	String groupID = request.getParameter("groupID");
	String memberID = request.getParameter("memberID");
	
	//This is the end point that we are sending the request to
	HttpPost jamRequest = new HttpPost("/api/v1/OData/Groups('" + groupID + "')/Tasks" );
	
	//Setting the response format to json
	jamRequest.addHeader("Content-Type", "application/json");
	jamRequest.addHeader("Accept", "application/json");
	
	//Creating the json message as a large string to pass in the request
	//In this case, we are setting the task name and assigning a specific member to
	//do this task.  the commented section below shows how to add multiple assignees.
	String jsonToSend = "";
	jsonToSend += "{";
	jsonToSend += "\"Title\" : \"" + taskName + "\",";
	 
	jsonToSend += "\"Assignments\":["; 
	jsonToSend += "{";
	jsonToSend += "\"__metadata\": {";
	jsonToSend += "\"uri\": \"TaskAssignments(AssigneeId='0',TaskId='0')\",";
	jsonToSend += "\"type\": \"SAPJam.TaskAssignment\"";
	jsonToSend += "},";
	jsonToSend += "\"Assignee\": {\"__metadata\": {\"uri\": \"Members('" + memberID + "')\",\"type\": \"SAPJam.Member\"},\"Id\":\"" + memberID + "\"},";
	jsonToSend += "\"Status\": \"open\"";
	jsonToSend += "}";

	
/**
Assign the task to a second member
	jsonToSend += ",{";
	jsonToSend += "\"__metadata\": {";
	jsonToSend += "\"uri\": \"TaskAssignments(AssigneeId='0',TaskId='0')\",";
	jsonToSend += "\"type\": \"SAPJam.TaskAssignment\"";
	jsonToSend += "},";
	jsonToSend += "\"Assignee\": {\"__metadata\": {\"uri\": \"Members('ScGTaG5KZ9i1Qasv1ucJbs')\",\"type\": \"SAPJam.Member\"},\"Id\":\"ScGTaG5KZ9i1Qasv1ucJbs\"},";
	jsonToSend += "\"Status\": \"open\"";
	jsonToSend += "}";
*/

	jsonToSend += "]";
	jsonToSend += "}";
	
	
	System.err.println(jsonToSend);
	
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