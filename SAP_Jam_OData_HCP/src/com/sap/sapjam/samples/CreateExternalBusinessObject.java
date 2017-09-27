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
 * This simple Java servlet shows how to create a new External Object in SAP Jam. Once created External Objects can be either 
 * the primary business object in a group, or they can be featured in a business object.
 * Once a primary or featured object is in a group, the group will receive all of the event updates for that object.   
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



public class CreateExternalBusinessObject extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateExternalBusinessObject() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String command = "";
		String id = "";
		
		command = request.getParameter("command");
		id = request.getParameter("id");
		
		PrintWriter out = response.getWriter();
		out.println("Command = " + command );
		
		try {
			// Code to retrieve the Java SAP Cloud Platform Destination.
			// The SAP Cloud Platform Destination handles the SAML2OAuthBearer Assertion workflow.
			Context ctx = new InitialContext();
			HttpDestination destination = (HttpDestination)ctx.lookup("java:comp/env/sap_jam_odata");
			HttpClient client = destination.createHttpClient();
			
			//https://help.sap.com/viewer/u_collaboration_dev_help/odata/references/ExternalObjectsPOSTExternalObjects.html
			String url = "/ExternalObjects";
			HttpPost jamRequest = new HttpPost( url );
			String name = "";
			
			String xmlBody = getXMLRequest( name, id );
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
	
	private String getXMLRequest(String name, String id) {
		StringBuilder payload = new StringBuilder();
		
			payload.append("<entry>");
			payload.append("<content>");
			payload.append("<properties>");
			payload.append("<Exid>http://benefitsjam.hana.ondemand.com/BenefitType(ObjectID='" + id + "1',ObjectType='BUS1006')</Exid>");
			payload.append("<Name>"+ name +"</Name>");
			payload.append("<Permalink></Permalink>");
			payload.append("<ODataAnnotations>https://benefitsjam.hana.ondemand.com/com.sap.hana.cloud.samples.benefits/common/annotations.xml</ODataAnnotations>");
			payload.append("<ODataMetadata>	</ODataMetadata>");
			payload.append("<ODataLink><Exid>http:/benefitsjam.hana.ondemand.com/BenefitType(ObjectID='" + id + "1',ObjectType='BUS1006')</Exid></ODataLink>");
			payload.append("<ObjectType>https://benefitsjam.hana.ondemand.com/com.sap.hana.cloud.samples.benefits/OData.svc/$metadata#BenefitTypes</ObjectType>");
			payload.append("</properties></content>");
			payload.append("</entry>");
			
			return payload.toString();
			
			
	}

	
	
}
