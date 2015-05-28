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
import java.net.URLEncoder;

/**
 * The purpose of this Servlet class is to provide some high level examples of sample calls to the SAP Jam OData API (api/v1/OData) 
 * 
 * This API uses the OData v2.0 specifications. For more details, visit:
 * http://www.odata.org/documentation/odata-version-2-0/
 * 
 * You will need an account on a SAP Jam instance and an associated HCP trial account to run these samples.
 * 
 *  This Java HCP project requires a Java destination called sap_jam_odata. This file can be found in the root of this project. 
 * 
 * *  For more information about the SAP Jam API see: http://help.sap.com/download/documentation/sapjam/developer/index.html
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
			// Code to retrieve the Java HCP Destination.
			// The HCP Destination handles the SAML2OAuthBearer Assertion workflow.
			
			Context ctx = new InitialContext();
			HttpDestination destination = (HttpDestination)ctx.lookup("java:comp/env/sap_jam_odata");
			HttpClient client = destination.createHttpClient();
			
			ArrayList<Commands> allCommands = new ArrayList<Commands>();
			
			// Do not require Id
			allCommands.add(new Commands("Get the currently logged-in user from Jam", "GET", "Self", "Self"));
			allCommands.add(new Commands("Return the list of groups the current logged in user belongs to", "GET", "Groups", "Groups"));
			
			// Require Id - return a URL parameter of "Id=ENTER_YOUR_ID_HERE" when groupId is null
			allCommands.add(new Commands("Return the OData for a specific group", 
					"GET", "Groups" + "('" + groupId + "')", "Groups", "groupId:"+groupId));
			allCommands.add(new Commands("Returns the primary external object for the specified group", 
					"GET", "Groups" + "('" + groupId + "')/" + "PrimaryExternalObject", "PrimaryExternalObject", "groupId:"+groupId));
			allCommands.add(new Commands("Returns the list of featured external objects for the group", 
					"GET", "Groups" + "('" + groupId + "')/" + "FeaturedExternalObjects", "FeaturedExternalObjects", "groupId:"+groupId));
			allCommands.add(new Commands("Returns the list of participants or members of the group", 
					"GET", "Groups" + "('" + groupId + "')/" + "Memberships", "Memberships", "groupId:"+groupId));
			allCommands.add(new Commands("Returns all the content items of the group", 
					"GET", "Groups" + "('" + groupId + "')/" + "AllContentItems", "AllContentItems", "groupId:"+groupId));
			
			int allCommands_Total = allCommands.size();
			for (int i = 0; i < allCommands_Total; i++){
				Commands currentCommand = allCommands.get(i);
				if (command.compareToIgnoreCase( currentCommand._command ) == 0 ){
					this.displayODataXML(client, out, "/" + currentCommand._odata_call);
					break;
				}
				else if (i == (allCommands_Total - 1) ){
					out.println("<html>");
					out.println("<head>");
					out.println("<h1>SAP Jam OData Explorer</h1>");
					out.println("<title>SAP Jam OData Explorer</title>");
					out.println("</head>");
					out.println("<body>");
					for (int j = 0; j < allCommands.size(); j++){
						Commands printCommand = allCommands.get(j);
						if (j == 0){
							out.println("<h2>GET general information (Id, etc.) from SAP Jam:</h2>");
							out.println("<ul>");
						}
						else if (j == 2){
							out.println("</ul>");
							out.println("<h2>GET specific information from SAP Jam (requires Id):</h2>");
							out.println("<ul>");
						}
						out.println("<li><a target=\"_blank\" href=\"" + theBaseURL + "?command=" + printCommand._command + "\">" 
						+ "[" + printCommand._odata_call_type + "] - " + printCommand._description + "</a></li>");
						
						if (j == (allCommands.size() - 1) ){
							out.println("</ul>");
						}
					}
					out.println("</body>");
					out.println("</html>");
					break;
				}
			}
			
			/*
			//OData calls
			if ( command.compareToIgnoreCase( OData_GET + "_" + Jam_Self ) == 0 ) {
				// Get the currently logged-in user from Jam
				this.displayODataXML(client, out, "/" + Jam_Self);
			} else if ( command.compareToIgnoreCase( OData_GET + "_" + Jam_Groups ) == 0 ) {						
				//Return the list of groups the current logged in user belongs to
				this.displayODataXML(client, out, "/" + Jam_Groups);				
			} else if ( command.compareToIgnoreCase( OData_GET + "_" + Jam_Groups + "_" + groupId ) == 0 ) {			
				//Return the OData for a specific group
				this.displayODataXML(client, out, "/" + Jam_Groups + "('" + groupId + "')");
			} else if ( command.compareToIgnoreCase( OData_GET + "_" + Jam_Groups + "_" + groupId + Jam_PrimaryExternalObject ) == 0 ) {
				//Returns the primary external object for the specified group
				this.displayODataXML(client, out, "/" + Jam_Groups + "('" + groupId + "')/" + Jam_PrimaryExternalObject);
			} else if ( command.compareToIgnoreCase( OData_GET + "_" + Jam_Groups + "_" + groupId + Jam_FeaturedExternalObjects ) == 0 ) {
				//Returns the list of featured external objects for the group
				this.displayODataXML(client, out, "/" + Jam_Groups + "('" + groupId + "')/" + Jam_FeaturedExternalObjects);
			} else if ( command.compareToIgnoreCase( OData_GET + "_" + Jam_Groups + "_" + groupId + Jam_Memberships ) == 0) {
				//Returns the list of participants or members of the group
				this.displayODataXML(client, out, "/" + Jam_Groups + "('" + groupId + "')/" + Jam_Memberships);
			} else if ( command.compareToIgnoreCase( OData_GET + "_" + Jam_Groups + "_" + groupId + Jam_AllContentItems ) == 0 ) {
				//Returns all the content items of the group
				this.displayODataXML(client, out, "/" + Jam_Groups + "('" + groupId + "')/" + Jam_AllContentItems);
			} else {
				out.println("<html>");
				out.println("<body>");
				out.println("<a href=\"" + theBaseURL + "?command=" + OData_GET + Jam_Self + "\">Get the currently logged-in user from Jam</a>");
				out.println("</body>");
				out.println("</html>");
				
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=self");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=groups");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=single_use_tokens");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=group&group=<GroupID>");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=primary&group=<GroupID>");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=featured&group=<GroupID>");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=members&group=<GroupID>");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=content&group=<GroupID>");
				out.println(" ");
				out.println("Missing URL Parameter. Please use ?command=groups, group, primary, featured, members, content, template");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=self");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=groups");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=single_use_tokens");
				out.println(" ");
				out.println("When requesting information for a specific group please add the parameter group=<GroupID>");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=group&group=<GroupID>");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=primary&group=<GroupID>");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=featured&group=<GroupID>");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=members&group=<GroupID>");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=content&group=<GroupID>");
			
			}*/
					
		} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}
			
	}
	
	private String convertNullToString(String input){
		if (input == null){
			input = "null";
		}
		return input;
	}
	
/** Simple method to call SAP Jam via HTTP GET, and display the output to the response
 * 
 * @param client
 * @param out
 * @param url
 * @throws Exception
 */
	private void displayODataXML(HttpClient client, PrintWriter out, String url ) throws Exception {
		HttpGet jamRequest = new HttpGet( url );
		HttpEntity responseEntity = client.execute(jamRequest).getEntity();
		if ( responseEntity != null )
			out.println(EntityUtils.toString(responseEntity));
		else
			out.println( "There was a problem with the connection");
	}
	
		
}
