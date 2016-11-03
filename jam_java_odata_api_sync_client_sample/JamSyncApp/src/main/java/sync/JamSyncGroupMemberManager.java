package sync;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import utils.JamConfig;
import utils.JamTokenManager;

public class JamSyncGroupMemberManager {

    // Group info class
    public static class Group {
        public String name;
        public String creatorEmail;
        public String fromUUID;
        public String toUUID;
    }
    
    // Member info class
    static public class Member {
        public String toGroupUUID = null;
        public String toUUID = null;

        public String fromGroupUUID = null;
        public String fromUUID = null;

        public String fullName;
        public String email;
        public String token = null; // token will be empty until the member is used
    }

    // Member table
    HashMap<String, Member> members = new HashMap<String, Member>();
    HashMap<String, Group> groups = new HashMap<String, Group>();
    HashMap<String, String> groupss = new HashMap<String, String>();

    private static JamSyncGroupMemberManager instance = new JamSyncGroupMemberManager();

    private JamSyncGroupMemberManager() {
    }

    public static JamSyncGroupMemberManager getInstance() {
        return instance;
    }

    public List<Member> getAllMemberEmails(String groupUUID) {

        List<Member> membersInGroupList = new ArrayList();

        for (Object emailKey : members.keySet().toArray()) {
            String key = (String)emailKey;

            Member m = members.get(key);

            if ((m.fromGroupUUID != null && groupUUID.equals(m.fromGroupUUID)) || (m.toGroupUUID != null && m.toGroupUUID.equals(groupUUID))) {
                membersInGroupList.add(m);
            }
        }

        return membersInGroupList;
    }

    public void SetGroupInfo(Group group) {
        groups.put(group.fromUUID, group);
    }

    public Group getGroup(String fromGroupUUID)
    {
        return groups.get(fromGroupUUID);
    }
    
    // Add member to cache
    public void AddMember(final Member member) {
        members.put(member.email, member);
    }

    // Get member by their email
    public Member GetMember(final String memberEmail) throws Exception {
        if (members.containsKey(memberEmail)) {

            final Member member = members.get(memberEmail);

            // Only get token when the member is used.  
            // Not all members are active, so lets not waste tokens
            if (member.token == null) {
                
                final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();
                member.token = JamTokenManager.getInstance().getTokenForMember(fromConfig.host,
                    fromConfig.proxy,
                    member.email,
                    fromConfig.clientId,
                    fromConfig.clientSecret,
                    fromConfig.grantType,
                    fromConfig.samlConfig);
            }

            return members.get(memberEmail);
        }

        return null;
    }

}