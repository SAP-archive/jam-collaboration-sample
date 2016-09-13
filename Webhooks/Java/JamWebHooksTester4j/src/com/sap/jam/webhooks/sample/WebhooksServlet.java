package com.sap.jam.webhooks.sample;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This is a simple servlet demonstrating how webhooks calls sent by SAP Jam should be handled.
 */
@WebServlet("/")
public class WebhooksServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String JAM_BASE_URL = "https://developer.sapjam.com"; // Change this if your Jam instance is not in Developer
    private static final String JAM_OAUTH_TOKEN = "<YOUR JAM_OAUTH_TOKEN>";
    private static final String JAM_WEBHOOK_VERIFICATION_TOKEN = "<YOUR JAM_WEBHOOK_VERIFICATION_TOKEN>";
    
    private final Client httpClient = ClientBuilder.newClient();
    private final ExecutorService eventHandlingPool = Executors.newCachedThreadPool();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public WebhooksServlet() {
        super();
    }

    /**
     * This is the POST end point that expects to receive webhook callbacks from Jam. Upon receiving any webhook callback,
     * clients must verify that the verification token within the request payload exists and is correct. After verification,
     * the client must echo the challenge string contained within the payload as the response to the request.
     * 
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Received POST call");

        final JSONObject callbackPayload = parseJSONRequest(request.getReader());

        final String verificationToken = (String)callbackPayload.get("@sapjam.hub.verificationToken");
        final String challengeString = (String)callbackPayload.get("@sapjam.hub.challenge");
        final JSONArray events = (JSONArray)callbackPayload.get("value");

        // Verify the identity of the server
        if (verificationToken.equals(JAM_WEBHOOK_VERIFICATION_TOKEN)) {
            handleEvents(events);
            response.getWriter().println(challengeString); // Echo the challenge string as a response
        } else {
            response.sendError(HttpStatus.SC_FORBIDDEN);
        }
    }

    /**
     * To make sure the webhook client responds to Jam webhook calls within a couple seconds, we encourage all event handling to be done
     * asynchronously. In Java, this can be done using with {@link ForkJoinPool} or {@link Executors} (used here).
     * 
     * @param events
     */
    private void handleEvents(final JSONArray events) {
    	if (events == null) {
    		return;
    	}
    	
        for (final Object event : events) {
            eventHandlingPool.execute(new Runnable() {

                @Override
                public void run() {
                    replyToEvent((JSONObject)event);
                }
            });
        }
    }

    /**
     * For this sample application, we want our webhooks client to post a reply to each document or comment that triggered a
     * webhook call. Since different types of event entities have different APIs for posting comments, we will have check the
     * type of the associated entity for each event to make the appropriate API call.
     * 
     * See our webhooks documentation for the possible event types and entity types that could be sent by a
     * webhook call.
     * 
     * @param eventObject
     */
    private void replyToEvent(final JSONObject eventObject) {
        final String eventEntityId = (String)eventObject.get("Id");
        final String entityType = (String)eventObject.get("@sapjam.event.entityType");

        System.out.printf("Processing event type: %s", entityType);

        switch (entityType) {
        case "ContentItem":
            final String contentItemType = (String)eventObject.get("ContentItemType");
            postToContentItem(eventEntityId, contentItemType);
            break;

        case "FeedEntry":
            postToFeed(eventEntityId);
            break;

        case "Comment":
            final String feedEntryId = (String)((JSONObject)eventObject.get("ParentFeedEntry")).get("Id");
            postToFeed(feedEntryId);
            break;

        default:
            break;
        }
    }

    /**
     * See @see <a href=
     * "https://developer.sapjam.com/ODataDocs/ui#!/Content/post_ContentItems_Id_Id_ContentItemType_ContentItemType_FeedEntries">
     * SAP Jam OData Documentation for reference</a>
     * 
     * @param contentItemId
     * @param contentItemType
     */
    private void postToContentItem(final String contentItemId, final String contentItemType) {
        final String oDataPath = String.format("ContentItems(Id='%s', ContentItemType='%s')/FeedEntries", contentItemId, contentItemType);
        postToOData(oDataPath, "{\"Text\": \"I've received a webhook call!\"}");
    }

    /**
     * See @see
     * <a href="https://developer.sapjam.com/ODataDocs/ui#!/Feed/get_FeedEntries_id_Replies">SAP Jam OData Documentation for
     * reference</a>
     * 
     * @param feedEntryId
     */
    private void postToFeed(final String feedEntryId) {
        final String oDataPath = String.format("FeedEntries('%s')/Replies", feedEntryId);
        postToOData(oDataPath, "{\"Text\": \"I've received a webhook call!\"}");
    }

    /**
     * This method encapsulates a basic way of making most POST calls to the Jam OData API under the JSON format.
     * 
     * @param oDataPath API end point to call
     * @param payload a JSON request body
     */
    private void postToOData(final String oDataPath, final String payload) {
        System.out.printf("Making Jam OData POST call to %s with payload: %n%s", oDataPath, payload);

        httpClient
            .target(JAM_BASE_URL)
            .path("/api/v1/OData/" + oDataPath)
            .queryParam("$format", "json")
            .request(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + JAM_OAUTH_TOKEN)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Accept", MediaType.APPLICATION_JSON)
            .async()
            .post(Entity.json(payload), new InvocationCallback<String>() {

                @Override
                public void completed(final String response) {
                    System.out.println("Received response: " + response);
                }

                @Override
                public void failed(final Throwable throwable) {
                    final ResponseProcessingException exception = (ResponseProcessingException)throwable;
                    final String responseString = exception.getResponse().readEntity(String.class);
                    System.out.println("Received error response: " + responseString);
                    throwable.printStackTrace();
                }
            });
    }

    /**
     * Converts a text input stream into a JSON object.
     * 
     * @param reader
     * @return a parsed JSONObject. If parsing fails, an empty JSON Object is returned.
     */
    private JSONObject parseJSONRequest(final Reader reader) {
        final JSONParser jsonParser = new JSONParser();
        JSONObject parsedPayload = new JSONObject();

        try {
            parsedPayload = (JSONObject)jsonParser.parse(reader);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        return parsedPayload;
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().println("Please add this endpoint as the callback URL in a Push Notification Subscription on SAP Jam.");
    }

    @Override
    public void destroy() {
        httpClient.close();
        eventHandlingPool.shutdown();
    }
}
