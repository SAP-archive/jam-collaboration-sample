package com.sap.sapjam.samples;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import com.sap.core.connectivity.api.http.HttpDestination;

import java.util.ArrayList;

/**
 * The purpose of this Servlet class is to provide some high level examples of sample calls to the SAP Jam OData API (api/v1/OData) 
 * 
 * This API uses the OData v2.0 specifications. For more details, visit:
 * http://www.odata.org/documentation/odata-version-2-0/
 * 
 * You will need an account on a SAP Jam instance and an associated SAP Cloud Platform trial account to run these samples.
 * 
 *  This Java SAP Cloud Platform project requires a Java destination called sap_jam_odata. This file can be found in the root of this project. 
 * 
 * *  For more information about the SAP Jam API see: https://help.sap.com/viewer/u_collaboration_dev_help
 *  
 */


/**
 * Servlet implementation class Connect
 */
public class Connect extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Connect() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, java.io.PrintWriter out)
	 * This code provides a simple demonstration of how to call the SAP Jam OData API with GET requests.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Create an HttpClient off the Jam destination
		
		String command = ""; 
		String groupId;
		command = request.getParameter("command");
		if ( command == null )
			command = "";
		groupId = request.getParameter("Id");
		groupId = convertNullToString(groupId);
		
		// Allocate a output writer to write the response message into the network socket
		PrintWriter out = response.getWriter();
		
		// Create the base URL
		StringBuffer theBaseURL = request.getRequestURL();
		
		try {
			// Code to retrieve the Java SAP Cloud Platform Destination.
			// The SAP Cloud Platform Destination handles the SAML2OAuthBearer Assertion workflow.
			
			Context ctx = new InitialContext();
			HttpDestination destination = (HttpDestination)ctx.lookup("java:comp/env/sap_jam_odata");
			HttpClient client = destination.createHttpClient();
			
			// Create a commands object - Holds all data required to create and run an OData call
			ArrayList<Commands> allCommands = new ArrayList<Commands>();
			
			// Tracks when a new command category is added
			ArrayList<Integer> allCommandsTracker = new ArrayList<Integer>();
			
			// [GET] - Do not require Id - returns user Id and group Ids
			allCommands.add(new Commands("Get the currently logged-in user from Jam", "GET", "api/v1/OData/Self", "Self"));
			allCommands.add(new Commands("Return the list of groups the current logged in user belongs to", "GET", "api/v1/OData/Groups", "Groups"));
			allCommandsTracker.add(allCommands.size());
			
			// [GET] - Require group Id - returns a URL parameter of "Id=ENTER_YOUR_ID_HERE" when groupId is null
			allCommands.add(new Commands("Return the OData for a specific group", 
					"GET", "api/v1/OData/Groups" + "('" + groupId + "')", "GroupsGroupId", "groupId:"+groupId));
			allCommands.add(new Commands("Returns the primary external object for the specified group", 
					"GET", "api/v1/OData/Groups" + "('" + groupId + "')/" + "PrimaryExternalObject", "PrimaryExternalObject", "groupId:"+groupId));
			allCommands.add(new Commands("Returns the list of featured external objects for the group", 
					"GET", "api/v1/OData/Groups" + "('" + groupId + "')/" + "FeaturedExternalObjects", "FeaturedExternalObjects", "groupId:"+groupId));
			allCommands.add(new Commands("Returns the list of participants or members of the group", 
					"GET", "api/v1/OData/Groups" + "('" + groupId + "')/" + "Memberships", "Memberships", "groupId:"+groupId));
			allCommands.add(new Commands("Returns all the content items of the group", 
					"GET", "api/v1/OData/Groups" + "('" + groupId + "')/" + "AllContentItems", "AllContentItems", "groupId:"+groupId));
			allCommandsTracker.add(allCommands.size());
			
			// [POST] - Does not require Id - returns a single-use token 
			allCommands.add(new Commands("Create a single-use token used for widget authentication", 
					"POST", "v1/single_use_tokens", "single_use_tokens"));
			allCommandsTracker.add(allCommands.size());
			
			// [POST] - Require Id - returns a URL parameter of ...
			
			
			// Processes all command objects
			int allCommands_Total = allCommands.size();
			for (int i = 0; i < allCommands_Total; i++){
				Commands currentCommand = allCommands.get(i);
				if (command.equalsIgnoreCase(currentCommand._command)){
					// Runs the OData call to SAP Jam
					if (currentCommand._odata_call_type == "GET"){
						this.displayGetODataXML(client, out, "/" + currentCommand._odata_call);
					}
					else if (currentCommand._odata_call_type == "POST"){
						this.displayPostODataXML(client, out, "/" + currentCommand._odata_call);
					}
					break;
				}
				else if (i == (allCommands_Total - 1) ){
					// Creates the html page with the OData links
					generateLandingPage(theBaseURL, allCommands, allCommandsTracker, out);
					break;
				}
			}
		} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}
	}
	
	private void generateLandingPage(StringBuffer theBaseURL, ArrayList<Commands> allCommands, ArrayList<Integer> allCommandsTracker, PrintWriter out){
		out.println("<html>");
		out.println("<head>");
		out.println("<h1>SAP Jam OData Explorer</h1>");
		out.println("<title>SAP Jam OData Explorer</title>");
		out.println("</head>");
		out.println("<body>");
		for (int j = 0; j < allCommands.size(); j++){
			Commands printCommand = allCommands.get(j);
			if (j == 0){
				out.println("<h2>[GET] - general information (Id, etc.) from SAP Jam:</h2>");
				out.println("<ul>");
			}
			else if (j == allCommandsTracker.get(0)){
				out.println("</ul>");
				out.println("<h2>[GET] - group specific information from SAP Jam (requires group Id):</h2>");
				out.println("<ul>");
			}
			else if (j == allCommandsTracker.get(1)){
				out.println("</ul>");
				out.println("<h2>[POST] - single-use token:</h2>");
				out.println("<ul>");
			}
			// Creates a hyperlink with description using the following URL structure:
			// - https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=<command>&Id=<Id>
			out.println("<li><a target=\"_blank\" href=\"" + theBaseURL + "?command=" + printCommand._command + "\">" 
					+ "[" + printCommand._odata_call_type + "] - " + printCommand._description + "</a></li>");
			
			if (j == (allCommands.size() - 1) ){
				out.println("</ul>");
			}
		}
		out.println("</body>");
		out.println("</html>");
	}
	
	private String convertNullToString(String input){
		if (input == null){
			input = "null";
		}
		return input;
	}
	
	private void displayPostODataXML(HttpClient client, PrintWriter out, String url ) throws Exception {
		HttpPost jamRequest = new HttpPost( url );
		HttpEntity responseEntity = client.execute(jamRequest).getEntity();
		if ( responseEntity != null )
			out.println(EntityUtils.toString(responseEntity));
		else
			out.println( "There was a problem with the connection");
	}
	
	/** Simple method to call SAP Jam via HTTP GET, and display the output to the response
	 * 
	 * @param client
	 * @param out
	 * @param url
	 * @throws Exception
	 */
	private void displayGetODataXML(HttpClient client, PrintWriter out, String url ) throws Exception {
		HttpGet jamRequest = new HttpGet( url );
		HttpEntity responseEntity = client.execute(jamRequest).getEntity();
		if ( responseEntity != null )
			out.println(EntityUtils.toString(responseEntity));
		else
			out.println( "There was a problem with the connection");
	}
	
}
