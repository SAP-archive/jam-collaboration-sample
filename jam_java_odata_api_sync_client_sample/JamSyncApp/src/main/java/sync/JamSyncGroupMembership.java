package sync;

import org.json.JSONObject;
import org.json.JSONArray;

import utils.JamConfig;
import utils.JamNetworkManager;
import utils.JamNetworkParam;
import utils.JamTokenManager;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.io.ByteArrayInputStream;

import java.util.List;
import java.util.ArrayList;

import sync.JamSyncGroupMemberManager;
import sync.JamSyncGroupMemberManager.Member;

public class JamSyncGroupMembership extends JamSyncInterface {
    static String API_ODATA_GET_GROUP_URL = "/api/v1/OData/Groups('%1$s')?$expand=Creator";
    static String API_ODATA_GET_GROUP_FILTER_URL = "/api/v1/OData/Groups";

    static String API_ODATA_POST_GROUPS_URL = "/api/v1/OData/Groups";
    static String API_ODATA_POST_GROUP_JOIN_URL = "/api/v1/OData/Group_Join";
    static String API_ODATA_POST_GROUP_INVITE_URL = "/api/v1/OData/Group_Invite";

    static String API_ODATA_POST_GROUP_NOTIFICATION_URL = "/api/v1/OData/Notifications";
    static String API_ODATA_POST_GROUP_NOTIFICATION_ACCEPT_URL = "/api/v1/OData/Notification_Accept";

    static class Group {
        public String uuid;
        public String name;
        public Boolean isActive;
        public Boolean autoSubscribe;
        public String groupType;
        public String createdAt;
        public String participation;
        public Boolean moderationPolicy;
        public Boolean autoGroup;
        public Boolean disableAtNotify;
        public Boolean contentsVisible;
        public Boolean questionsVisible;
        public Boolean ideasVisisble;
        public Boolean discussionsVisible;
        public Boolean tasksVisible;
        public Boolean eventsVisible;
        public Boolean linksVisible;
        public Boolean subgroupsVisible;
        public Boolean recommendationsVisible;
        public Boolean hasOverview;
        public String taskPolicy;
        public Boolean isAdmin;
        public String creatorEmail;
        public String creatorUUID;
    }

    public JamSyncGroupMembership() {
    }

    // Retrieve group information from source instance and create same group to the destination instance
    public Boolean syncGroupCreation(final String fromGroupUUID) {
        try {
            // [FROM INSTANCE] Get group info            
            final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();
            final String url = fromConfig.host + String.format(API_ODATA_GET_GROUP_URL, fromGroupUUID);
            final JSONObject groupResponseJSON = JamNetworkManager.getInstance().GetRequest(url, new JamNetworkParam());
            final JSONObject groupJson = ((JSONObject)groupResponseJSON.get("d")).getJSONObject("results");
            System.out.println("\n  Processing group from SOURCE instance: " + fromGroupUUID);

            // Replicate group information to create new group for destination instance
            final JSONObject groupParam = new JSONObject();
            groupParam.put("Id", groupJson.getString("Id"));
            groupParam.put("Name", groupJson.getString("Name") + "_Copy");
            groupParam.put("IsActive", groupJson.getBoolean("IsActive"));
            groupParam.put("AutoSubscribe", groupJson.getBoolean("AutoSubscribe"));
            groupParam.put("GroupType", groupJson.getString("GroupType"));
            groupParam.put("CreatedAt", groupJson.getString("CreatedAt"));
            groupParam.put("Participation", groupJson.getString("Participation"));
            groupParam.put("ModerationPolicy", groupJson.getBoolean("ModerationPolicy"));
            groupParam.put("AutoGroup", groupJson.getBoolean("AutoGroup"));
            groupParam.put("DisableAtNotify", groupJson.getBoolean("DisableAtNotify"));
            groupParam.put("ContentsVisible", groupJson.getBoolean("ContentsVisible"));
            groupParam.put("QuestionsVisible", groupJson.getBoolean("QuestionsVisible"));
            groupParam.put("IdeasVisible", groupJson.getBoolean("IdeasVisible"));
            groupParam.put("DiscussionsVisible", groupJson.getBoolean("DiscussionsVisible"));
            groupParam.put("TasksVisible", groupJson.getBoolean("TasksVisible"));
            groupParam.put("EventsVisible", groupJson.getBoolean("EventsVisible"));
            groupParam.put("LinksVisible", groupJson.getBoolean("LinksVisible"));
            groupParam.put("SubgroupsVisible", groupJson.getBoolean("SubgroupsVisible"));
            groupParam.put("RecommendationsVisible", groupJson.getBoolean("RecommendationsVisible"));
            groupParam.put("HasOverview", groupJson.getBoolean("HasOverview"));
            groupParam.put("TaskPolicy", groupJson.getString("TaskPolicy"));
            groupParam.put("IsAdmin", groupJson.getBoolean("IsAdmin"));

            // Get creator so we can create the group using the same creator
            final JamConfig.ConfigInfo toConfig = JamConfig.getInstance().getToConfig();
            final JSONObject creatorJson = groupJson.getJSONObject("Creator");
            final String creatorUUID = creatorJson.getString("Id");
            final String creatorEmail = creatorJson.getString("Email");
            String toGroupUUID = null;

            // [TO INSTANCE] Create same group and replicate the same info
            // Get Member token for same member on destination jam instance
            JamTokenManager.getInstance().setTokenFromConfig(toConfig,  creatorEmail);

            System.out.println("\n");

            // Build params to POST a group creation request for destination instance
            JamNetworkParam params = new JamNetworkParam();
            params.add("Content-Type", "application/json");

            // Create create group post url
            final String createGroupurl = toConfig.host + API_ODATA_POST_GROUPS_URL;
            System.out.println("    Body: " + groupParam.toString());

            // Post create group URL request
            final InputStream inputStream = new ByteArrayInputStream(groupParam.toString().getBytes());
            final JSONObject result = JamNetworkManager.getInstance().PostRequest(createGroupurl, params, inputStream);

            final JSONObject createdGroupJson = ((JSONObject)result.get("d")).getJSONObject("results");
            toGroupUUID = createdGroupJson.getString("Id");

            // Set from and to group information
            JamSyncGroupMemberManager.Group group = new JamSyncGroupMemberManager.Group();
            group.name = groupParam.getString("Name");
            group.fromUUID = fromGroupUUID;
            group.toUUID = toGroupUUID;
            group.creatorEmail = creatorEmail;
            JamSyncGroupMemberManager.getInstance().SetGroupInfo(group);

            // Invite all users to group
            syncMembershipInviteAndAccept(fromGroupUUID, toGroupUUID, creatorEmail);
        } catch (final Exception e) {
            System.out.println("\n  JamSyncGroupContents::SyncFromGroupInfo error: " + e.toString());
            e.printStackTrace();
            return false;

        }
        return true;
    }

    // For each user, send invite to join the designated group
    public void syncMembershipInviteAndAccept(String fromGroupUUID, String toGroupUUID, String toGroupAdminEmail) throws Exception {

        System.out.println("      Inviting members to destination group: " + toGroupUUID);

        final JamConfig.ConfigInfo toConfig = JamConfig.getInstance().getToConfig();
        final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();
        List<Member> memberList = JamSyncGroupMemberManager.getInstance().getAllMemberEmails(fromGroupUUID);

        // Get Member token for same member on destination jam instance
        JamTokenManager.getInstance().setTokenFromConfig(toConfig,  toGroupAdminEmail);

        for (Member member : memberList) {
            member.toGroupUUID = toGroupUUID;
            
            // Invite member to new group
            System.out.println("      Invite member: " + member.email + " to destination group: " + toGroupUUID);

            final String inviteUrl = toConfig.host + API_ODATA_POST_GROUP_INVITE_URL;

            JamNetworkParam inviteParamBody = new JamNetworkParam();
            JamNetworkParam inviteParamHeader = new JamNetworkParam();

            inviteParamBody.add("Id", "'" + toGroupUUID + "'");
            inviteParamBody.add("Email", "'" + member.email + "'");
            inviteParamBody.add("Message", "'Invite to group!'");

            final InputStream inviteInputStream = new ByteArrayInputStream(inviteParamBody.toString().getBytes());
            JamNetworkManager.getInstance().PostRequest(inviteUrl, inviteParamHeader, inviteInputStream);
        }

        for (Member member : memberList) {
            // Get Member token for same member on destination jam instance
            JamTokenManager.getInstance().setTokenFromConfig(toConfig,  member.email);

            final String notificationsUrl = toConfig.host + API_ODATA_POST_GROUP_NOTIFICATION_URL;
            JSONObject notificationResults = JamNetworkManager.getInstance().GetRequest(notificationsUrl, new JamNetworkParam());

            final JSONArray notificationResult = ((JSONObject)notificationResults.get("d")).getJSONArray("results");

            if (notificationResult.length() > 0) {
                JSONObject event = notificationResult.getJSONObject(0);

                String eventType = event.getString("EventType");

                if (eventType.equals("InviteToGroup")) {

                    String notificationId = event.getString("Id");
                    final String notificationAcceptUrl = toConfig.host + API_ODATA_POST_GROUP_NOTIFICATION_ACCEPT_URL;

                    System.out.println("\n        Invite Notitication ID : " + notificationId);

                    JamNetworkParam notificationAcceptParamBody = new JamNetworkParam();
                    JamNetworkParam notificationAcceptParamHeader = new JamNetworkParam();
                    notificationAcceptParamBody.add("Id", "'" + notificationId + "'");

                    InputStream inputStream1 = new ByteArrayInputStream(notificationAcceptParamBody.toString().getBytes());
                    JSONObject noticiationAcceptResults = JamNetworkManager.getInstance().PostRequest(notificationAcceptUrl,
                        notificationAcceptParamHeader,
                        inputStream1);
                }
            }

        }
    }

    @Override
    public void render() {
        System.out.println("JamSyncGroupMemberShip::Render!");
    }

    @Override
    public void sync(String fromGroupUUID) {
        System.out.println("\n  ##################################################################################################################################################");
        System.out.println("  JamSyncGroupMemberShip::SYNC Begin!");

        final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();

        // For each group, get list of member and add them to cache
        Boolean passed = syncGroupCreation(fromGroupUUID);

        System.out.println("  JamSyncGroupMemberShip::Sync end!");
        System.out.println("\n  ##################################################################################################################################################");
    }
}
