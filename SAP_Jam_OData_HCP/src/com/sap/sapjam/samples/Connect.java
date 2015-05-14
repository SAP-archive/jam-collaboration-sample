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
import org.apache.http.util.EntityUtils;

import com.sap.core.connectivity.api.http.HttpDestination;

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
		groupId = request.getParameter("group");
		
		// Allocate a output writer to write the response message into the network socket
		PrintWriter out = response.getWriter();
		
		try {
			// Code to retrieve the Java HCP Destination.
			// The HCP Destination handles the SAML2OAuthBearer Assertion workflow.
			
			Context ctx = new InitialContext();
			HttpDestination destination = (HttpDestination)ctx.lookup("java:comp/env/sap_jam_odata");
			HttpClient client = destination.createHttpClient();
			
			//Try some of the OData navigations off the group object
			if ( command.compareToIgnoreCase( "self" ) == 0 ) {
				// Get the currently logged-in user from Jam
				this.displayODataXML(client, out, "/Self");
			} else if ( command.compareToIgnoreCase( "groups" ) == 0 ) {						
				//Return the list of groups the current logged in user belongs to
				this.displayODataXML(client, out, "/Groups");				
			} else if ( command.compareToIgnoreCase( "group" ) == 0 ) {			
				//Return the OData for a specific group
				this.displayODataXML(client, out, "/Groups('"+ groupId + "')");
			} else if ( command.compareToIgnoreCase( "primary" ) == 0 ) {
				//Returns the primary external object for the specified group
				this.displayODataXML(client, out, "/Groups('" + groupId + "')/PrimaryExternalObject");
			} else if ( command.compareToIgnoreCase( "featured" ) == 0 ) {
				//Returns the list of featured external objects for the group
				this.displayODataXML(client, out, "/Groups('" + groupId + "')/FeaturedExternalObjects");
			} else if ( command.compareToIgnoreCase( "members" ) == 0) {
				//returns the list of participants or members of the group
				this.displayODataXML(client, out, "/Groups('" + groupId + "')/Memberships");
			} else if ( command.compareToIgnoreCase( "content" ) == 0 ) {
				//returns all the content items of the group.
				this.displayODataXML(client, out, "Groups('" + groupId + "')/AllContentItems");				
			} else {
				out.println("Missing URL Parameter. Please use ?command=groups, group, primary, featured, members, content, template");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=self");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=groups");
				out.println(" ");
				out.println("When requesting information for a specific group please add the parameter group=<GroupID>");
				out.println("e.g. https://<your_HCP_Server_URL>/<your_Eclipse_Project_Name>/?command=group&group=<GroupID>");
			}
					
		} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}
			
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
		// Output to screen
		if ( responseEntity != null )
			out.println(EntityUtils.toString(responseEntity));
		else
			out.println( "There was a problem with the connection");
	}
	
		
}
