package sync;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import java.io.ByteArrayInputStream;

import utils.JamConfig;
import utils.JamNetworkManager;
import utils.JamNetworkParam;
import utils.JamTokenManager;
import utils.JamNetworkResult;

import java.io.InputStream;
import java.net.MalformedURLException;

public class JamSyncGroupContents extends JamSyncInterface {

    static class Feed {
        String id;
        String title;
        String text;
        String creatorEmail;
        boolean liked;
        public Feed(String _id, String _title, String _text, String _creatorEmail, boolean _liked) {
            id = _id;
            title = _title;
            text = _text;
            creatorEmail = _creatorEmail;
            liked = _liked;
        }
    }

    static class FeedEntry {
        public String id;
        public String title;
        public String text;
        public String actionOnly;
        public String action;
        public String createdAt;
        public String liked;
        public String canReply;
        public String canDelete;
        public String webUrl;
        public String read;
        public String ContentType;
    }

    static class Content {
        public String type;
        public String contentType;
        public String media_src;
        public String topLevelParent;
        public String documentSize;

        List<FeedEntry> comments;
    }

    static class Folder {
        public String fromParentFolderUUID;
        public String fromParentFolderName;
        public String fromUUID;
        public String toUUID;

        public String folderType;
        public String name;
        public Boolean isPrivate;
        public String lastModifiedAt;
        public String creatorId;
        public String creatorFullName;
        public String creatorEmail;

        public List<Folder> subFolders = new ArrayList<Folder>();
    }

    static class Reply {
        String text = "";
        String creatorEmail ="";
        boolean liked = false;

        Reply(String textStr,String creatorEmailStr, boolean likedReply) {
            text = textStr;
            creatorEmail = creatorEmailStr;
            liked = likedReply;
        }
    }

    HashMap<String, Folder> folderMap = new HashMap<String, Folder>();

    static String API_ODATA_GET_GROUP_URL = "/api/v1/OData/Groups('%1$s')?$expand=Creator";
    static String API_ODATA_GET_GROUP_ALL_CONTENT_ITEMS_URL = "/api/v1/OData/Groups('%1$s')/AllContentItems?$expand=Creator,LastModifier,FeedEntries,FeedEntries/Creator,ParentFolder&$select=Id,Name,ParentFolder/Id,ContentItemType,Description,CreatedAt,LastModifiedAt,Liked,IsFeatured,Creator/Id,Creator/Email,FeedEntries,FeedEntries/Id,FeedEntries/Title,FeedEntries/Replies,FeedEntries/Text,FeedEntries/Creator,FeedEntries/Liked,Liked";
    static String API_ODATA_GET_GROUP_ALL_FOLDERS_URL = "/api/v1/OData/Groups('%1$s')/AllFolders?$top=%2$i&$skip=%3$i&$inlinecount=allpages&$expand=ParentFolder";
    static String API_ODATA_GET_FEED_ENTRIES_REPLIES = "/api/v1/OData/FeedEntries('%1$s')/Replies?$expand=Creator&$top=%2$s&$skip=%3$s";

    static String API_ODATA_POST_FOLDERS = "/api/v1/OData/Folders(Id='%1$s',FolderType='%2$s')/Folders";
    static String API_ODATA_POST_GROUP_FOLDER_URL = "/api/v1/OData/Groups('%1$s')/Folders";
    static String API_ODATA_POST_GROUP_CONTENT_ITEMS = "/api/v1/OData/Groups('%1$s')/ContentItems";
    static String API_ODATA_POST_FOLDER_CONTENT_ITEMS = "/api/v1/OData/Folders(Id='%1$s',FolderType='%2$s')/ContentItems";
    static String API_ODATA_POST_CONTENT_ITEMS = "/api/v1/OData/ContentItems";
    static String API_ODATA_POST_CONTENT_ITEMS_FEED_ENTRIES = "/api/v1/OData/ContentItems(Id='%1$s',ContentItemType='%2$s')/FeedEntries";
    static String API_ODATA_POST_FEED_ENTRIES_REPLIES = "/api/v1/OData/FeedEntries('%1$s')/Replies";

    static String API_ODATA_PATCH_GROUP_CONTENT_ITEMS_LIKES = "/api/v1/OData/ContentItems(Id='%1$s',ContentItemType='%2$s')";
    static String API_ODATA_PATCH_FEEDENTRIES_LIKED_URL = "/api/v1/OData/FeedEntries('%1$s')";
    static String API_ODATA_PATCH_FEEDENTRIES_REPLIES_LIKED_URL = "/api/v1/OData/Comments('%1$s')";
    
    List<Content> contents = new ArrayList<Content>();

    public JamSyncGroupContents() {
    }

    @Override
    public void render() {
        System.out.println("JamSyncGroupContents::Render!");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Add folder to folder map for later lookup and build folder structure as pages comes in
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    public void addFolderToHashMap(Folder folder) {
        folderMap.put(folder.fromUUID, folder);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Go through all folders and link up child of each parent folder and add to sub folder list
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    public void linkFolderParents() {
        for (Map.Entry<String, Folder> folderEntry : folderMap.entrySet()) {
            Folder folder = (Folder)(folderEntry.getValue());

            // Find parent folder and add folder to sub folders of the respectful parent
            if (folder.fromParentFolderUUID != null && folderMap.containsKey(folder.fromParentFolderUUID)) {
                Folder parentFolder = folderMap.get(folder.fromParentFolderUUID);
                parentFolder.subFolders.add(folder);
            }
        }
    }

    public void syncLikes( String uploadLikedUrl, boolean liked) throws MalformedURLException {
        //////////////////////////////////////////////////////////////////////////////
        // [DESTINATION] UPLOADING Likes!!
        //////////////////////////////////////////////////////////////////////////////        
        JamNetworkParam likesParamHeader = new JamNetworkParam();
        likesParamHeader.add("Content-Type", "application/json");

        JSONObject likesParamBody = new JSONObject();
        likesParamBody.put("Liked", liked);

        final InputStream likedInputStream = new ByteArrayInputStream(likesParamBody.toString().getBytes());
        JamNetworkManager.getInstance().PatchRequest(uploadLikedUrl, likesParamHeader, likedInputStream);
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Recurse the folder tree structure and sync destination with the folder structure
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    public void syncFoldersToDestination(String toGroupUUID, Folder folder) throws Exception {
        final JamConfig.ConfigInfo toConfig = JamConfig.getInstance().getToConfig();

        // Create root folder first
        Folder parentFolder = folderMap.get(folder.fromParentFolderUUID);

        // Set the OAuth token from the member who will be creating the folder
        JamTokenManager.getInstance().setTokenFromConfig(toConfig,  folder.creatorEmail);
        
        System.out.println("");

        String folderUrl = "";
        if (parentFolder != null) {
            System.out.println("       [DESTINATION] Creating SUB Folder: " + folder.name + " uuid:" + folder.fromUUID);
            folderUrl = toConfig.host + String.format(API_ODATA_POST_FOLDERS, parentFolder.toUUID, "Folder");
        } else {
            System.out.println("       [DESTINATION] Creating ROOT Folder: " + folder.name + " uuid:" + folder.fromUUID);
            folderUrl = toConfig.host + String.format(API_ODATA_POST_GROUP_FOLDER_URL, toGroupUUID);
        }

        JamNetworkParam paramHeader = new JamNetworkParam();
        JSONObject paramBody = new JSONObject();
        paramBody.put("Name", folder.name);
        paramHeader.add("Content-Type", "application/json");

        final InputStream inputStream = new ByteArrayInputStream(paramBody.toString().getBytes());
        final JSONObject folderJSON = JamNetworkManager.getInstance().PostRequest(folderUrl, paramHeader, inputStream);

        // Get the destination group uuid
        final JSONObject folderInfoJSON = ((JSONObject)folderJSON.get("d")).getJSONObject("results");

        folder.toUUID = folderInfoJSON.get("Id").toString();
        System.out.println("       [DESTINATION] responded with toUUID: " + folder.toUUID);

        // For each sub folder, recurse through them as well to sync the folder in pre-order traversal manner
        for (Folder subFolder : folder.subFolders) {
            syncFoldersToDestination(toGroupUUID, subFolder);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Sync all folders from source to destination Jam instance
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    public void syncAllFolders(final String fromGroupUUID, final String toGroupUUID) throws Exception {
        System.out.println("   JamSyncGroupMembers::SyncAllFolders BEGIN!");

        final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();
        final int folderPageSize = 20;
        int folderCurrentPage = 0;
        boolean moreFolders = true;

        // Retrieve all folders from source Instance
        while (moreFolders) {
            final String url = fromConfig.host + "/api/v1/OData/Groups('" + fromGroupUUID + "')/AllFolders?$top=" + folderPageSize + "&$skip="
                + folderCurrentPage + "&$inlinecount=allpages&$expand=ParentFolder,Creator";
            final JSONObject allFoldersJSON = JamNetworkManager.getInstance().GetRequest(url, new JamNetworkParam());

            // Parse results for content information
            final JSONArray foldersListJson = ((JSONObject)allFoldersJSON.get("d")).getJSONArray("results");

            moreFolders = (foldersListJson.length() == folderPageSize);

            System.out.println("\n       Folder Result Count: " + foldersListJson.length());

            for (int i = 0; i < foldersListJson.length(); ++i) {
                final JSONObject folderJson = foldersListJson.getJSONObject(i);

                final Folder folder = new Folder();

                // Get folder info
                folder.fromUUID = folderJson.getString("Id");
                folder.name = folderJson.getString("Name");
                folder.folderType = folderJson.getString("FolderType");
                folder.isPrivate = folderJson.getBoolean("IsPrivate");
                folder.lastModifiedAt = folderJson.getString("LastModifiedAt");

                // Find creator info
                final JSONObject creatorJson = (JSONObject)folderJson.get("Creator");
                folder.creatorId = creatorJson.getString("Id");
                folder.creatorEmail = creatorJson.getString("Email");
                folder.creatorFullName = creatorJson.getString("FullName");

                // Find parent folder information
                folder.fromParentFolderUUID = null;

                try {
                    // Try to parse parent folder, if not found, then the current folder is a root folder
                    if (folderJson.has("ParentFolder")) {
                        final JSONObject parentJson = (JSONObject)folderJson.get("ParentFolder");

                        if (parentJson != null) {
                            folder.fromParentFolderUUID = parentJson.getString("Id");
                            folder.fromParentFolderName = parentJson.getString("Name");
                        }
                    }
                } catch (final Exception e) {
                    folder.fromParentFolderUUID = null;
                    folder.fromParentFolderName = null;
                }

                // log folder that was found
                if (folder.fromParentFolderUUID != null) {
                    System.out.println("         Folder: " + folder.name + "(" + folder.fromUUID + ")" + "\n              parent:"
                        + folder.fromParentFolderName + "(" + folder.fromParentFolderUUID + ")");
                } else {
                    System.out.println("         Folder: " + folder.name + "(" + folder.fromUUID + ")");
                }

                // Add folder to list to process
                addFolderToHashMap(folder);
            }

            // If there are no more content 
            if (moreFolders) {
                folderCurrentPage += folderPageSize;
            }
        }

        // For each folder link up folder to parent
        linkFolderParents();

        // Find all root folders
        for (Map.Entry<String, Folder> folderEntry : folderMap.entrySet()) {
            Folder folder = (Folder)(folderEntry.getValue());

            if (folder.fromParentFolderUUID == null) {
                syncFoldersToDestination(toGroupUUID, folder);
            }
        }

        System.out.println("   JamSyncGroupContents::SyncFromSourceAllFolders END!");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Sync all contents from source to destination Jam instance
    //////////////////////////////////////////////////////////////////////////////////////////////////////	
    public void syncAllContents(final String fromGroupUUID, final String toGroupUUID) {
        // Get list of contents to download
        System.out.println("   JamSyncGroupMembers::SyncAllContents BEGIN!");

        try {
            final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();

            // Set OAuth and network settings
            JamTokenManager.getInstance().setTokenFromConfig(fromConfig,  fromConfig.adminEmail);

            syncAllFolders(fromGroupUUID, toGroupUUID);
            syncAllContents(fromGroupUUID, toGroupUUID);
        } catch (final Exception e) {
            System.out.println("\n  JamSyncGroupContents::SyncFromSource error: " + e.toString());
            e.printStackTrace();
        }

        // Download content to temporary location
        System.out.println("   JamSyncGroupContents::SyncFromSource END!");
    }

    //////////////////////////////////////////////////////////////////////////////
    // Sync replies of a feed between Jam Instances
    //////////////////////////////////////////////////////////////////////////////
    public void syncContentFeedEntriesReplies(String fromGroupUUID, String toGroupUUID, String fromFeedEntryUUID, String toFeedEntryUUID, String creatorEmail)
            throws Exception {
        final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();
        final JamConfig.ConfigInfo toConfig = JamConfig.getInstance().getToConfig();

        System.out.println("      Sync Replies for FeedEntries => uuid:" + fromFeedEntryUUID);

        int top = 20;
        int skip = 0;
        
        JamTokenManager.getInstance().setTokenFromConfig(fromConfig, creatorEmail);
        
        String repliesUrl = fromConfig.host + String.format(API_ODATA_GET_FEED_ENTRIES_REPLIES, fromFeedEntryUUID, top, skip);
        JSONObject repliesResponseJSON = JamNetworkManager.getInstance().GetRequest(repliesUrl, new JamNetworkParam());
        JSONArray replyListJson = (repliesResponseJSON.getJSONObject("d")).getJSONArray("results");

        //////////////////////////////////////////////////////////////////////////////
        // DOWNLOAD replies of a feed from source Jam Instance
        //////////////////////////////////////////////////////////////////////////////

        List<Reply> replies = new ArrayList<Reply>();
        while (replyListJson != null && replyListJson.length() > 0) {
            for (int i = 0; i < replyListJson.length(); ++i) {

                if (replyListJson.getJSONObject(i).has("Text")) {
                    JSONObject replyJson = replyListJson.getJSONObject(i);
                    String text = replyJson.getString("Text").trim();
                    String commenterEmail = replyJson.getJSONObject("Creator").getString("Email");
                    boolean liked = replyJson.getBoolean("Liked");
                    if (text != null && !text.equals("")) {
                        replies.add(new Reply(text, commenterEmail, liked));
                    }
                }
            }

            // If here are more pages, continue to request for next page
            if (replyListJson.length() < top) {
                skip += top;

                String url = fromConfig.host + String.format(API_ODATA_GET_FEED_ENTRIES_REPLIES, fromFeedEntryUUID, top, skip);
                repliesResponseJSON = JamNetworkManager.getInstance().GetRequest(url, new JamNetworkParam());
                replyListJson = repliesResponseJSON.getJSONObject("d").getJSONArray("results");
            } else {
                break;
            }
        }

        //////////////////////////////////////////////////////////////////////////////
        // UPLOAD replies in reverse order to destination Jam Instance
        //////////////////////////////////////////////////////////////////////////////
        for (int i = 0; i < replies.size(); i++) {
            Reply r = replies.get(i);

            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOADING Reply
            //////////////////////////////////////////////////////////////////////////////
            JamNetworkParam uploadHeaderParam = new JamNetworkParam();
            uploadHeaderParam.add("Accept", "application/json");
            uploadHeaderParam.add("Content-Type", "application/json");

            JSONObject paramBody = new JSONObject();
            paramBody.put("Text", r.text);
            paramBody.put("Liked", r.liked);

            JamTokenManager.getInstance().setTokenFromConfig(toConfig,  r.creatorEmail);
            
            final InputStream inputStream = new ByteArrayInputStream(paramBody.toString().getBytes());
            String toPostUrl = toConfig.host + String.format(API_ODATA_POST_FEED_ENTRIES_REPLIES, toFeedEntryUUID);
            JSONObject toReplyJson = JamNetworkManager.getInstance().PostRequest(toPostUrl, uploadHeaderParam, inputStream);

            String toReplyUUID = toReplyJson.getJSONObject("d").getJSONObject("results").getString("Id");
            
            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOADING Likes!!
            //////////////////////////////////////////////////////////////////////////////
            syncLikes(toConfig.host + String.format(API_ODATA_PATCH_FEEDENTRIES_REPLIES_LIKED_URL, toReplyUUID), r.liked);
        }
    }

    // Sync feed entries of a content item including the replies
    public void syncContentFeedEntries(
        String fromGroupUUID,
        String toGroupUUID,
        String toContentItemUUID,
        String toContentItemType,
        JSONObject feedEntries) throws Exception {
        System.out.println("      SyncContentFeedEntries for ContentItem uuid:" + toContentItemUUID);

        final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();
        final JamConfig.ConfigInfo toConfig = JamConfig.getInstance().getToConfig();
        JSONArray fromContentFeedEntriesJSON = feedEntries.getJSONArray("results");

        //////////////////////////////////////////////////////////////////////////////
        // DOWNLOAD feeds from source Jam Instance
        //////////////////////////////////////////////////////////////////////////////
        List<Feed> feeds = new ArrayList<Feed>();
        while (fromContentFeedEntriesJSON != null && fromContentFeedEntriesJSON.length() > 0) {
            System.out.println("        Found feeds: " + fromContentFeedEntriesJSON.length());

            for (int i = 0; i < fromContentFeedEntriesJSON.length(); i++) {
                final JSONObject fromFeedEntriesJson = fromContentFeedEntriesJSON.getJSONObject(i);
                String fromFeeEntryTitle = fromFeedEntriesJson.getString("Title");
                String fromFeedEntryUUID = fromFeedEntriesJson.getString("Id");
                String fromCreatorEmail = fromFeedEntriesJson.getJSONObject("Creator").getString("Email");
                boolean liked = fromFeedEntriesJson.getBoolean("Liked");
                if (fromFeedEntriesJson.has("Text")) {

                    String fromFeedEntryText = fromFeedEntriesJson.getString("Text");
                    fromFeedEntryText = fromFeedEntryText.trim();
                    System.out.println("        Feed entry for ContentItem uuid:" + toContentItemUUID + "\n          Title:" + fromFeeEntryTitle
                        + "\n          Text:-" + fromFeedEntryText + "-");

                    if (fromFeedEntryText != null && !fromFeedEntryText.equals("")) {
                        feeds.add(new Feed(fromFeedEntryUUID, fromFeeEntryTitle, fromFeedEntryText, fromCreatorEmail, liked));
                    }
                }
            }

            // If here are more pages, continue to request for next page
            final String toNextPageUrl = feedEntries.getString("__next");
            String url = fromConfig.host + "/api/v1/OData/" + toNextPageUrl + "&$expand=Creator";
            JSONObject feedEntriesReponseJSON = JamNetworkManager.getInstance().GetRequest(url, new JamNetworkParam());
            feedEntries = feedEntriesReponseJSON.getJSONObject("d");
            fromContentFeedEntriesJSON = feedEntries.getJSONArray("results");
        }

        //////////////////////////////////////////////////////////////////////////////
        // UPLOAD feeds to destination Jam instance in reverse order
        //////////////////////////////////////////////////////////////////////////////
        for (int i = feeds.size() - 1; i >= 0; i--) {
            Feed feed = feeds.get(i);
            JamTokenManager.getInstance().setTokenFromConfig(toConfig,  feed.creatorEmail);

            // Upload to destination
            JamNetworkParam uploadHeaderParam = new JamNetworkParam();
            uploadHeaderParam.add("Accept", "application/json");
            uploadHeaderParam.add("Content-Type", "application/json");

            JSONObject paramBody = new JSONObject();
            paramBody.put("Text", feed.text);
            paramBody.put("Liked", feed.liked);

            final InputStream inputStream = new ByteArrayInputStream(paramBody.toString().getBytes());
            String toPostUrl = toConfig.host + String.format(API_ODATA_POST_CONTENT_ITEMS_FEED_ENTRIES, toContentItemUUID, toContentItemType);
            final JSONObject postedToContentFeedEntriesJSON = JamNetworkManager.getInstance().PostRequest(toPostUrl, uploadHeaderParam, inputStream);
            String toContentFeedEntryUUID = postedToContentFeedEntriesJSON.getJSONObject("d").getJSONObject("results").getString("Id");

            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOADING Likes!!
            //////////////////////////////////////////////////////////////////////////////
            syncLikes(toConfig.host + String.format(API_ODATA_PATCH_FEEDENTRIES_LIKED_URL, toContentFeedEntryUUID), feed.liked);
            
            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOADING Replies
            //////////////////////////////////////////////////////////////////////////////
            String toFeedEntryUUID = postedToContentFeedEntriesJSON.getJSONObject("d").getJSONObject("results").getString("Id");
            syncContentFeedEntriesReplies(fromGroupUUID, toGroupUUID, feed.id, toFeedEntryUUID, feed.creatorEmail);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Sync all content items from source to destination Jam instance
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    public void syncAllContentItems(String fromGroupUUID, String toGroupUUID) throws Exception {

        System.out.println("\n**************************************************************************************************");
        System.out.println("   JamSyncGroupMembers::SyncAllContentItems BEGIN!");

        final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();
        final JamConfig.ConfigInfo toConfig = JamConfig.getInstance().getToConfig();

        JamTokenManager.getInstance().setTokenFromConfig(fromConfig,  fromConfig.adminEmail);
        System.out.println("\n");

        //////////////////////////////////////////////////////////////////////////////
        // [SOURCE] DOWNLOADING AllContentItems to get first page of content items
        //////////////////////////////////////////////////////////////////////////////
        String url = fromConfig.host + String.format(API_ODATA_GET_GROUP_ALL_CONTENT_ITEMS_URL, fromGroupUUID);
        JSONObject allContentItemsReponseJSON = JamNetworkManager.getInstance().GetRequest(url, new JamNetworkParam());
        JSONArray contentItemListJson = ((JSONObject)allContentItemsReponseJSON.get("d")).getJSONArray("results");

        // Go through each page of AllContentItems and upload the content items to destination Jam instance
        while (contentItemListJson != null && contentItemListJson.length() > 0) {
            System.out.println("        Found CONTENTITEMS: " + contentItemListJson.length());

            for (int i = 0; i < contentItemListJson.length(); ++i) {
                final JSONObject fromContentItemJson = contentItemListJson.getJSONObject(i);

                String name = fromContentItemJson.getString("Name");
                String jamContentType = fromContentItemJson.getString("ContentItemType");
                String contentType = fromContentItemJson.getJSONObject("__metadata").getString("content_type");
                String mediaSrcUrl = fromContentItemJson.getJSONObject("__metadata").getString("media_src");
                String createdAt = fromContentItemJson.getString("CreatedAt");
                boolean like = fromContentItemJson.getBoolean("Liked");
                String creatorEmail = fromContentItemJson.getJSONObject("Creator").getString("Email");

                String toParentFolderUUID = null;
                if (fromContentItemJson != null && fromContentItemJson.has("ParentFolder") && !fromContentItemJson.isNull("ParentFolder")) {
                    String parentFolderUUID = fromContentItemJson.getJSONObject("ParentFolder").getString("Id");
                    toParentFolderUUID = ((Folder)(folderMap.get(parentFolderUUID))).toUUID;
                }

                System.out.println("\n       Content => Name:'" + name + "' contentType:'" + contentType + "' creator:'" + creatorEmail
                    + "' TOParentFolder:'" + toParentFolderUUID + "'");

                //////////////////////////////////////////////////////////////////////////////
                // [SOURCE] DOWNLOADING media content
                //////////////////////////////////////////////////////////////////////////////
                JamTokenManager.getInstance().setTokenFromConfig(fromConfig,  creatorEmail);

                String contentMediaUrl = fromConfig.host + "/api/v1/OData/" + mediaSrcUrl;
                JamNetworkParam param = new JamNetworkParam();
                param.add("Accept", contentType);
                JamNetworkResult downloadedContentResult = JamNetworkManager.getInstance().GetRequestWithResult(contentMediaUrl, param);

                //////////////////////////////////////////////////////////////////////////////
                // [DESTINATION] UPLOADING media content to destination
                //////////////////////////////////////////////////////////////////////////////
                JamTokenManager.getInstance().setTokenFromConfig(toConfig,  creatorEmail);

                String uploadContentUrl = toConfig.host + String.format(API_ODATA_POST_GROUP_CONTENT_ITEMS, toGroupUUID);

                if (toParentFolderUUID != null) {
                    uploadContentUrl = toConfig.host + String.format(API_ODATA_POST_FOLDER_CONTENT_ITEMS, toParentFolderUUID, "Folder");
                }

                JamNetworkParam uploadHeaderParam = new JamNetworkParam();
                uploadHeaderParam.add("Accept", contentType);
                uploadHeaderParam.add("Content-Type", contentType);
                uploadHeaderParam.add("Slug", name);

                final JSONObject postedToContentItemJSON = JamNetworkManager.getInstance().PostRequest(uploadContentUrl,
                    uploadHeaderParam,
                    downloadedContentResult.inputStream);
                final JSONObject toContentItemJSON = ((JSONObject)postedToContentItemJSON.get("d")).getJSONObject("results");

                String toContentUUID = toContentItemJSON.get("Id").toString();
                System.out.println("       [DESTINATION] uploaded ContentItem toUUID: " + toContentUUID);

                //////////////////////////////////////////////////////////////////////////////
                // [DESTINATION] UPLOADING Likes!!
                //////////////////////////////////////////////////////////////////////////////
                syncLikes(toConfig.host + String.format(API_ODATA_PATCH_GROUP_CONTENT_ITEMS_LIKES, toContentUUID,jamContentType), like);
                
                //////////////////////////////////////////////////////////////////////////////
                // UPLOAD all FeedEntries for the content items
                //////////////////////////////////////////////////////////////////////////////
                JamTokenManager.getInstance().setTokenFromConfig(fromConfig,  creatorEmail);

                // Now that the contents are done uploading to Destination Instance, next would be the feed entries
                if (fromContentItemJson.has("FeedEntries")) {
                    syncContentFeedEntries(fromGroupUUID,
                        toGroupUUID,
                        toContentUUID,
                        jamContentType,
                        fromContentItemJson.getJSONObject("FeedEntries"));
                }
            }

            // Get Next page
            String nextUrl = ((JSONObject)allContentItemsReponseJSON.get("d")).getString("__next");
            JamTokenManager.getInstance().setTokenFromConfig(fromConfig,  fromConfig.adminEmail);

            url = fromConfig.host + "/api/v1/OData/" + nextUrl;
            allContentItemsReponseJSON = JamNetworkManager.getInstance().GetRequest(url, new JamNetworkParam());
            contentItemListJson = ((JSONObject)allContentItemsReponseJSON.get("d")).getJSONArray("results");
        }

        System.out.println("   JamSyncGroupMembers::SyncAllContentItems END!");
        System.out.println("**************************************************************************************************");
    }

    @Override
    public void sync(String fromGroupUUID) {
        System.out.println("\n  ##################################################################################################################################################");
        System.out.println("  JamSyncGroupMembers::SYNC Begin!");

        final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();

        try {
            String toGroupUUID = JamSyncGroupMemberManager.getInstance().getGroup(fromGroupUUID).toUUID;

            // Set the OAuth token and proxy so that Network layer reuse the same token and proxy
            JamTokenManager.getInstance().setTokenFromConfig(fromConfig,  fromConfig.adminEmail);

            syncAllFolders(fromGroupUUID, toGroupUUID);
            syncAllContentItems(fromGroupUUID, toGroupUUID);
        } catch (final Exception e) {
            System.out.println("\n  JamSyncGroupContents::Sync error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("  JamSyncGroupContents::Sync end!");
        System.out.println(
            "\n  ##################################################################################################################################################");
    }
}