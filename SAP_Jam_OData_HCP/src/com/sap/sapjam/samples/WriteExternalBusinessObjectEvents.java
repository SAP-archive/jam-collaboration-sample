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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

import com.sap.core.connectivity.api.http.HttpDestination;
/**
 * This simple Java servlet shows how to send feed events to SAP Jam for an external object. This allows the developer 
 * to enable users to monitor the state of the external object from the SAP Jam feed. If the object type is properly
 * registered with SAP Jam as an External Application object, end users will be able to use the feed event to create
 * new groups using the feed hover quick view. It is best to associate the object type to a group template for the 
 * best results. 
 * 
 * Notice this code requires that the External Object is already created, because it uses SAP Jam External Object IDs.
 * You can get the SAP Jam External Object UUIDs from the External Object creation code in CreateExternalBusinessObject.java
 * 
 * This API uses the OData v2.0 specifications. For more details, visit:
 * http://www.odata.org/documentation/odata-version-2-0/
 * 
 * You will need an account on a SAP Jam  instance and an associated HCP trial account to run these samples.
 * 
 *  This Java HCP project requires a Java destination called sap_jam_odata. This file can be found in the root of this project. 
 * 
 * *  For more information about the SAP Jam API see: http://help.sap.com/download/documentation/sapjam/developer/index.html
 *  
 */


public class WriteExternalBusinessObjectEvents extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WriteExternalBusinessObjectEvents() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * 	 * This code demonstrates how to send an update event from an External Object.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	// Create an HttpClient off the Jam destination
	
			String command = ""; 
			command = request.getParameter("command");
			if ( command == null )
				command = "";
			
			PrintWriter out = response.getWriter();
			
			try {
				// Code to retrieve the Java HCP Destination.
				// The HCP Destination handles the SAML2OAuthBearer Assertion workflow.
				Context ctx = new InitialContext();
				HttpDestination destination = (HttpDestination)ctx.lookup("java:comp/env/sap_jam_odata");
				HttpClient client = destination.createHttpClient();
				
				//http://help.sap.com/download/documentation/sapjam/developer/index.html#odata/references/ActivitiesPOSTActivities.html
				String url = "/Activities";
				HttpPost jamRequest = new HttpPost( url );
				
				
				String xmlBody = getXMLRequest( command );
				HttpEntity entity = new ByteArrayEntity(xmlBody.getBytes("UTF-8"));
				jamRequest.setEntity(entity);
				jamRequest.setHeader("Content-Type", "application/xml");
				jamRequest.setHeader("Accept", "application/xml");
				
				HttpResponse jamResponse  = client.execute(jamRequest);
				HttpEntity responseEntity = jamResponse.getEntity();
				
				
				// Output to screen
				if ( responseEntity != null )
					out.println(EntityUtils.toString(responseEntity));
				else
					out.println( "There was a problem with the connection");
					out.println( jamResponse.toString() );
			} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}
	}

	private String getXMLRequest(String id) {
		StringBuilder payload = new StringBuilder();
		
			payload.append("<entry>");
			payload.append("<link rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/Object\" href=\"ExternalObjects('" + id + "')\">ExternalObjects('" + id + "')</link>");
			payload.append("<link rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/Distribution\" href=\"Members('li78H2pPt2VTYM7QgmZKMW')\">Members('li78H2pPt2VTYM7QgmZKMW')</link>");
			payload.append("<link rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/Distribution\" href=\"Members('yrJJMCGwlX6POGUBR9d5P8')\">Members('yrJJMCGwlX6POGUBR9d5P8')</link>");
			payload.append("<content>");
			payload.append("<properties>");
			payload.append("<Content>Five new top opportunities were added to the account.</Content>");
			payload.append("</properties>");
			payload.append("</content>");
			payload.append("</properties></content>");
			payload.append("</entry>");
			
			
			return payload.toString();
	}

	
}
