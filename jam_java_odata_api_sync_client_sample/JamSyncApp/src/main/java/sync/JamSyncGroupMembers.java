package sync;

import org.json.JSONObject;
import org.json.JSONArray;

import utils.JamConfig;
import utils.JamNetworkManager;
import utils.JamTokenManager;
import sync.JamSyncGroupMemberManager;

import utils.JamNetworkParam;;

public class JamSyncGroupMembers extends JamSyncInterface {
    static String API_ODATA_GET_GROUP_MEMBER_URL = "/api/v1/OData/Groups('%1$s')/Memberships?$expand=Member&$select=GroupId,MemberId,Member/Id,Member/FullName,Member/Email";

    public JamSyncGroupMembers() {

    }

    @Override
    public void render() {
        System.out.println("JamSyncGroupFeeds::Render!");
    }

    @Override
    public void sync(String fromGroupUUID) {
        System.out.println(
            "\n  ##################################################################################################################################################");
        System.out.println("  JamSyncGroupMembers::SYNC Start!");

        try {
            final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();

            // Set the OAuth token and proxy so that Network layer reuse the same token and proxy
            JamTokenManager.getInstance().setTokenFromConfig(fromConfig,  fromConfig.adminEmail);
            
            // For each group, get list of member and add them to cache
            System.out.println("\n     Processing group: " + fromGroupUUID);

            final String url = fromConfig.host + String.format(API_ODATA_GET_GROUP_MEMBER_URL, fromGroupUUID);

            final JSONObject membersJSON = JamNetworkManager.getInstance().GetRequest(url, new JamNetworkParam());

            // Parse Members JSON response
            final JSONArray membersListJson = ((JSONObject)membersJSON.get("d")).getJSONArray("results");

            System.out.println("\n       Members result Count: " + membersListJson.length());

            for (int i = 0; i < membersListJson.length(); ++i) {
                final JamSyncGroupMemberManager.Member member = new JamSyncGroupMemberManager.Member();
                final JSONObject memberJson = membersListJson.getJSONObject(i);
                final JSONObject memberInfoObj = (JSONObject)memberJson.get("Member");

                member.toGroupUUID = null;
                member.toUUID = null;
                member.fromUUID = memberJson.getString("MemberId");
                member.fromGroupUUID = memberJson.getString("GroupId");
                member.fullName = memberInfoObj.getString("FullName");
                member.email = memberInfoObj.getString("Email");

                System.out.println("         Member: " + member.email);

                JamSyncGroupMemberManager.getInstance().AddMember(member);
            }
        } catch (final Exception e) {
            System.out.println("\n  JamSyncGroupMembers::Sync error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("  JamSyncGroupMembers::SYNC Done!");
        System.out.println(
            "  ##################################################################################################################################################");

    }
}