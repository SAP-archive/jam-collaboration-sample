<%@page import = "org.apache.http.util.EntityUtils" %>
<%@page import = "org.apache.http.client.methods.HttpPost" %>
<%@page import = "org.apache.http.client.methods.HttpGet" %>
<%@page import = "org.apache.http.client.HttpClient" %>
<%@page import = "org.apache.http.HttpEntity" %>
<%@page import = "com.sap.core.connectivity.api.http.HttpDestination" %>
<%@page import = "javax.naming.Context" %>
<%@page import = "javax.naming.InitialContext" %><%
try
{
	Context ctx = new InitialContext();
	HttpDestination destination = (HttpDestination)ctx.lookup("java:comp/env/sap_jam_odata");
	HttpClient client = destination.createHttpClient();

	String functionName = request.getParameter("function");
	String GroupID = request.getParameter("groupID");
	HttpGet jamRequest = new HttpGet("api/v1/OData/Groups('" + GroupID + "')" + functionName);
	
	
	HttpEntity responseEntity = client.execute(jamRequest).getEntity();
	if ( responseEntity != null )
	{
		String responseString = EntityUtils.toString(responseEntity);
		out.println(responseString);
	}
	else
		out.println( "No value returned");
}
catch(Exception ex)
{
	System.err.println(ex.toString());
	out.println(ex.toString());
}
%>