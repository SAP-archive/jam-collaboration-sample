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
 * 
 * 
 * This API uses the OData v2.0 specifications. For more details, visit:
 * http://www.odata.org/documentation/odata-version-2-0/
 * 
 * You will need an account on a SAP Jam  instance and an associated SAP Cloud Platform trial account to run these samples.
 * 
 *  This Java SAP Cloud Platform project requires a Java destination called sap_jam_odata. This file can be found in the root of this project. 
 * 
 * *  For more information about the SAP Jam API see: https://help.sap.com/viewer/u_collaboration_dev_help
 *  
 */

/**
 * Servlet implementation class CreateGroup
 */
public class CreateGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	public enum DataFormat {
		JSON("application/json"), 
		XML("application/xml");
		
		private final String text;
		
		private DataFormat(final String text) {
			this.text = text;
		}
		
		public String toString() {
			return text;
		}
	};
	
	public enum GroupTemplate {SYSTEM, CUSTOM}
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateGroup() {
        super();

    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		
		try {
			// Code to retrieve the Java SAP Cloud Platform Destination.
			// The SAP Cloud Platform Destination handles the SAML2OAuthBearer Assertion workflow.
			Context ctx = new InitialContext();
			HttpDestination destination = (HttpDestination)ctx.lookup("java:comp/env/sap_jam_odata");
			HttpClient client = destination.createHttpClient();
			
			//TODO URL
			String url = "/Groups";
			HttpPost jamRequest = new HttpPost( url );
			
			//For this code you need to retrieve the template ID of the template you want to use to create the group 
			String xmlBody = getCreateGroupRequest( "<add template ID", GroupTemplate.CUSTOM, "My new Group", DataFormat.JSON) ;
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
	private String getCreateGroupRequest(String templateId, GroupTemplate type, String name, DataFormat format) {
		String templateCompositeKey = "GroupTemplates(Id='" + templateId + "', GroupTemplateType='" + type.name().toLowerCase() + "')";
		
		StringBuilder payload = new StringBuilder();
	
		if (format.equals(DataFormat.XML)) {
			payload.append("<entry>");
			payload.append("<link rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/Template\" href=\"" + templateCompositeKey + "\"/>");
			payload.append("<content><properties>");
			payload.append("<Name>" + name + "</Name>");
			payload.append("</properties></content>");
			payload.append("</entry>");
		}
		else {
			payload.append("{\"Name\":" + "\"" + name + "\",");
			payload.append("\"Template\":{\"__metadata\":{\"uri\":" + "\"" + templateCompositeKey + "\"" + "}}}");
		}
		
		return payload.toString();
	}


	

}
