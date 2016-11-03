package sync;

import org.json.JSONArray;
import org.json.JSONObject;

import sync.JamSyncGroupMemberManager;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import java.io.ByteArrayInputStream;


import utils.JamConfig;
import utils.JamNetworkManager;
import utils.JamNetworkParam;
import utils.JamTokenManager;

import java.io.InputStream;
import java.net.MalformedURLException;

public class JamSyncGroupQIDs extends JamSyncInterface {
    
    // GET ODATA API CALLS
    static String API_ODATA_GET_GROUP_FORUMS_URL                = "/api/v1/OData/Groups('%1$s')/Forums?$expand=Creator&$select=Name,Type,Id,Creator";
    
    static String API_ODATA_GET_GROUP_ALL_QUESTIONS_URL         = "/api/v1/OData/Groups('%1$s')/AllQuestions?$expand=Answers,BestAnswer,Creator,Forum,BestAnswer/Creator&$select=Answers,BestAnswer,Creator/Email,Id,Name,Content,HasBestAnswer,AnswersCount,Liked,Forum/Id,Liked";
    static String API_ODATA_GET_GROUP_ALL_IDEAS_URL             = "/api/v1/OData/Groups('%1$s')/AllIdeas?$expand=Creator,Forum,Posts&$select=Name,Id,Creator,Forum,Content,Status";
    static String API_ODATA_GET_GROUP_ALL_DISCUSSIONS_URL       = "/api/v1/OData/Groups('%1$s')/AllDiscussions?$expand=Creator,Forum,Comments&$select=Name,Id,Creator,Content,Liked,Forum,CommentsCount";
    static String API_ODATA_GET_FORUM_QUESTION_ANSWER_URL       = "/api/v1/OData/Questions('%1$s')/Answers?$expand=Comments,Creator";
    static String API_ODATA_GET_WALL_COMMENTS_URL               = "/api/v1/OData/WallComments('%1$s')/Comments?$expand=Creator&$select=Id,Creator,Liked,Text";
    static String API_ODATA_GET_DISCUSSIONS_COMMENTS_URL        = "/api/v1/OData/Discussions('%1$s')/Comments?$expand=Creator&$select=Id,Creator,Liked,Text";
    
    // POST ODATA API CALLS
    static String API_ODATA_POST_GROUP_FORUMS_URL               = "/api/v1/OData/Groups('%1$s')/Forums";
    static String API_ODATA_POST_GROUP_FORUMS_QUESTIONS_URL     ="/api/v1/OData/Forums('%1$s')/Questions";
    static String API_ODATA_POST_GROUP_FORUMS_IDEAS_URL         = "/api/v1/OData/Forums('%1$s')/Ideas";
    static String API_ODATA_POST_GROUP_FORUMS_DISCUSSIONS_URL   = "/api/v1/OData/Forums('%1$s')/Discussions";
    static String API_ODATA_POST_FORUM_QUESTION_ANSWER_URL      = "/api/v1/OData/Questions('%1$s')/Answers";

    static String API_ODATA_POST_WALL_COMMENTS_URL              = "/api/v1/OData/WallComments('%1$s')/Comments";
    static String API_ODATA_POST_QUESTION_BEST_ANSWER_URL       = "/api/v1/OData/Questions('%1$s')/$links/BestAnswer";
    static String API_ODATA_POST_DISCUSSIONS_COMMENTS_URL       = "/api/v1/OData/Discussions('%1$s')/Comments";
    static String API_ODATA_PATCH_GROUP_FORUMS_IDEAS_INFO_URL   = "/api/v1/OData/Ideas('%1$s')";
    static String API_ODATA_PATCH_COMMENTS_LIKED_URL            = "/api/v1/OData/Comments('%1$s')";
    
    static String API_ODATA_PATCH_DISCUSSIONS_LIKED_URL         ="/api/v1/OData/Discussions('%1$s')";
    static String API_ODATA_PATCH_QUESTIONS_LIKED_URL            ="/api/v1/OData/Questions('%1$s')";
    static String API_ODATA_PATCH_WALL_COMMENTS_LIKED_URL       ="/api/v1/OData/WallComments('%1$s')";

    static  class Forum {
        public String fromUUID=null;
        public String toUUID=null;
        public String name;
        public String type;
        public String creatorEmail;
        
    }
    
    static class Answer {
        public String fromUUID;
        public String toUUID;
        public String comment;
        public boolean liked;
    }
    
    HashMap<String, Forum> forumsMap = new HashMap<String, Forum>();
    HashMap<String, Answer> answersMap = new HashMap<String, Answer>();

    public JamSyncGroupQIDs() {
    }

    private Forum findForumByName(String name) {
        for (Map.Entry<String, Forum> entry : forumsMap.entrySet()) {
            Forum forum = (Forum)(entry.getValue());
            
            if(forum.name.equals(name)) {
                return forum;
            }
        }
        
        return null;
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
    
    private void syncAllForumInfo(JamSyncGroupMemberManager.Group group) throws Exception {
        final JamConfig.ConfigInfo toConfig = JamConfig.getInstance().getToConfig();
        final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();

        //////////////////////////////////////////////////////////////////////////////
        // [SOURCE] DOWNLOAD all forums
        //////////////////////////////////////////////////////////////////////////////
        System.out.println("       [SOURCE] Getting forums for group: " + group.fromUUID);
        
        JamTokenManager.getInstance().setTokenFromConfig(fromConfig,  group.creatorEmail); // FROM GROUP CREATOR
        String forumsUrl = fromConfig.host + String.format(API_ODATA_GET_GROUP_FORUMS_URL, group.fromUUID);
        JSONObject forumsJSON = JamNetworkManager.getInstance().GetRequest(forumsUrl, new JamNetworkParam()).getJSONObject("d");
        
        JSONArray forumsListJson = forumsJSON.getJSONArray("results");
        
        List<JSONObject> forums = new ArrayList<JSONObject>();
        while (forumsListJson !=null && forumsListJson.length() > 0) {
            System.out.println("        Found ideas: " + forumsListJson.length());
            for (int i = 0; i <forumsListJson.length(); ++i) {
                forums.add(forumsListJson.getJSONObject(i));
            }
            
            // If here are more pages, continue to request for next page
            if (forumsJSON.has("__next")) {
                final String toNextPageUrl = forumsJSON.getString("__next");
                String url = fromConfig.host + "/api/v1/OData/" + toNextPageUrl;
                forumsJSON = JamNetworkManager.getInstance().GetRequest(url, new JamNetworkParam()).getJSONObject("d");
                forumsListJson = forumsJSON.getJSONArray("results");  
            } else {
                forumsListJson = null;
            }
            
        }
        
        for (int i = 0; i < forums.size(); ++i) {
            final JSONObject fromForumJson = forums.get(i);

            final Forum forum = new Forum();
            forum.fromUUID = fromForumJson.getString("Id");
            forum.name = fromForumJson.getString("Name");
            forum.type = fromForumJson.getString("Type");
            forum.creatorEmail = fromForumJson.getJSONObject("Creator").getString("Email");

            System.out.println("       [SOURCE] Getting group forum: name:" +  forum.name + " type:"+forum.type + " creator" + forum.creatorEmail);
            forumsMap.put(forum.fromUUID,forum);
        }

        //////////////////////////////////////////////////////////////////////////////
        // [DESTINATION] Check to see if Forums exist, if so, use them for uploading
        //////////////////////////////////////////////////////////////////////////////
        String toForumsUrl = toConfig.host + String.format(API_ODATA_GET_GROUP_FORUMS_URL, group.toUUID);
        JamTokenManager.getInstance().setTokenFromConfig(toConfig,  group.creatorEmail); // FROM GROUP CREATOR
        final JSONObject toForumsJSON = JamNetworkManager.getInstance().GetRequest(toForumsUrl, new JamNetworkParam());
        final JSONArray toForumsListJson = ((JSONObject)toForumsJSON.get("d")).getJSONArray("results");
        
        for (int i = 0; i < toForumsListJson.length(); ++i) {
            final JSONObject toForumJson = toForumsListJson.getJSONObject(i);

            System.out.println("       [DESTINATION] Found :" +  toForumJson.getString("Name"));
            Forum existForum = findForumByName(toForumJson.getString("Name"));
            
            if(existForum != null) {

                existForum.toUUID = toForumJson.getString("Id");
                System.out.println("       [SOURCE] Added ID for group forum: name:" +  existForum.name + " type:"+existForum.type + " creator" + existForum.creatorEmail + "  touuid:" + existForum.toUUID);
                }
        }

        //////////////////////////////////////////////////////////////////////////////
        // [DESTINATION] UPLOAD - For forums that doesn't exist yet, upload / create them
        //////////////////////////////////////////////////////////////////////////////
        String toForumsPostUrl = toConfig.host + String.format(API_ODATA_POST_GROUP_FORUMS_URL, group.toUUID);
        
        for (Map.Entry<String, Forum> entry : forumsMap.entrySet()) {
            String key = entry.getKey();
            Forum forum = (Forum)(entry.getValue());

            if(forum.toUUID == null) {
                JamTokenManager.getInstance().setTokenFromConfig(toConfig,  forum.creatorEmail);

                JSONObject paramBody = new JSONObject();
                
                paramBody.put("Name", forum.name);
                paramBody.put("Type", forum.type);

                
                System.out.println("       [SOURCE] group forum name: " + forum.name + "  type:" + forum.type);
                JamNetworkParam paramHeader = new JamNetworkParam();
                paramHeader.add("Content-Type", "application/json");

                final InputStream inputStream = new ByteArrayInputStream(paramBody.toString().getBytes());
                JSONObject forumJSON = JamNetworkManager.getInstance().PostRequest(toForumsPostUrl, paramHeader, inputStream);

                JSONObject toForumJson = forumJSON.getJSONObject("d").getJSONObject("results");                
                forum.toUUID = toForumJson.getString("Id");
            }
        }        
    }
    
    private void syncComments(JamSyncGroupMemberManager.Group group, String creatorEmail, String getCommentsUrl, String postCommentsUrl) throws Exception {
        final JamConfig.ConfigInfo toConfig = JamConfig.getInstance().getToConfig();
        final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();

        //////////////////////////////////////////////////////////////////////////////
        // [SOURCE] DOWNLOADING Comments
        //////////////////////////////////////////////////////////////////////////////                
        JamTokenManager.getInstance().setTokenFromConfig(fromConfig, creatorEmail); // FROM GROUP CREATOR
        final JSONObject fromCommentsJSON = JamNetworkManager.getInstance().GetRequest(getCommentsUrl, new JamNetworkParam());        
        final JSONArray fromCommentsListJson = ((JSONObject)fromCommentsJSON.get("d")).getJSONArray("results");
 
        System.out.println("       [SYNC] Syncing comments("+ fromCommentsListJson.length()+ ") from:" + getCommentsUrl + "  to: " + postCommentsUrl);

        for (int r = 0; r < fromCommentsListJson.length(); r++) {
            final JSONObject fromCommentJson = fromCommentsListJson.getJSONObject(r);

            System.out.println("       [SYNC] Syncing comment : " + fromCommentJson.getString("Text"));
            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOADING Comments
            //////////////////////////////////////////////////////////////////////////////
            
            String commenterEmail = fromCommentJson.getJSONObject("Creator").getString("Email");
            JamTokenManager.getInstance().setTokenFromConfig(toConfig,  commenterEmail);

            JSONObject paramBody = new JSONObject();
            paramBody.put("Text", fromCommentJson.getString("Text"));
            
            JamNetworkParam paramHeader = new JamNetworkParam();
            paramHeader.add("Content-Type", "application/json");

            final InputStream commentInputStream = new ByteArrayInputStream(paramBody.toString().getBytes());
            JSONObject toCommentJSON = JamNetworkManager.getInstance().PostRequest(postCommentsUrl, paramHeader, commentInputStream);
            
            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOADING Liked
            //////////////////////////////////////////////////////////////////////////////                
            String toCommentUUID = toCommentJSON.getJSONObject("d").getJSONObject("results").getString("Id");
            System.out.println("       [SYNC] Syncing comments txt:"+ fromCommentJson.getString("Text") + " => liked:" + fromCommentJson.getBoolean("Liked"));
            syncLikes( toConfig.host + String.format(API_ODATA_PATCH_COMMENTS_LIKED_URL, toCommentUUID), fromCommentJson.getBoolean("Liked"));
        }
    }
    
    private void syncAllQuestions(JamSyncGroupMemberManager.Group group) throws Exception {
        final JamConfig.ConfigInfo toConfig = JamConfig.getInstance().getToConfig();
        final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();

        //////////////////////////////////////////////////////////////////////////////
        // [SOURCE] DOWNLOAD all questions
        //////////////////////////////////////////////////////////////////////////////
        System.out.println("       [SOURCE] Getting AllQuestions for group: " + group.fromUUID);
        
        JamTokenManager.getInstance().setTokenFromConfig(fromConfig,  group.creatorEmail); // FROM GROUP CREATOR
        String forumsUrl = fromConfig.host + String.format(API_ODATA_GET_GROUP_ALL_QUESTIONS_URL, group.fromUUID);
        JSONObject fromQuestionsJSON = JamNetworkManager.getInstance().GetRequest(forumsUrl, new JamNetworkParam()).getJSONObject("d");
        
        JSONArray fromQuestionsListJson = fromQuestionsJSON.getJSONArray("results");
        
        List<JSONObject> questions = new ArrayList<JSONObject>();
        while (fromQuestionsListJson !=null && fromQuestionsListJson.length() > 0) {

            System.out.println("        Found questions: " + fromQuestionsListJson.length());
            for (int i = 0; i <fromQuestionsListJson.length(); ++i) {
                questions.add(fromQuestionsListJson.getJSONObject(i));
            }
            
            // If here are more pages, continue to request for next page
            if (fromQuestionsJSON.has("__next")) {
                final String toNextPageUrl = fromQuestionsJSON.getString("__next");
                String url = fromConfig.host + "/api/v1/OData/" + toNextPageUrl;
                fromQuestionsJSON = JamNetworkManager.getInstance().GetRequest(url, new JamNetworkParam()).getJSONObject("d");
                fromQuestionsListJson = fromQuestionsJSON.getJSONArray("results");  
            } else {
                fromQuestionsListJson = null;
            }
        }
        
        for (int i = questions.size()-1; i >= 0; --i) {
            final JSONObject questionJson = questions.get(i);
            
            // Get Questions
            String name             = questionJson.getString("Name");
            String fromQuestionUUID = questionJson.getString("Id");
            int answerCount         = questionJson.getInt("AnswersCount");
            String content          = questionJson.getString("Content");
            String creatorEmail     = questionJson.getJSONObject("Creator").getString("Email");
            String fromForumUUID    = questionJson.getJSONObject("Forum").getString("Id");
            JSONArray answers       = questionJson.getJSONObject("Answers").getJSONArray("results");
            boolean liked           = questionJson.getBoolean("Liked");
            
            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOADING A QUESTION
            //////////////////////////////////////////////////////////////////////////////
            String postToQuestionUrl = toConfig.host + String.format(API_ODATA_POST_GROUP_FORUMS_QUESTIONS_URL,  forumsMap.get(fromForumUUID).toUUID);
            JamTokenManager.getInstance().setTokenFromConfig(toConfig,  creatorEmail); // FROM GROUP CREATOR

            JSONObject paramBody = new JSONObject();
            
            paramBody.put("Name", name);
            paramBody.put("Content", content);
            
            JamNetworkParam paramHeader = new JamNetworkParam();
            paramHeader.add("Content-Type", "application/json");

            final InputStream inputStream = new ByteArrayInputStream(paramBody.toString().getBytes());
            JSONObject forumJSON = JamNetworkManager.getInstance().PostRequest(postToQuestionUrl, paramHeader, inputStream);

            JSONObject toForumJson = forumJSON.getJSONObject("d").getJSONObject("results");                
            String toQuestionUUID = toForumJson.getString("Id");            
            
            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOADING liked
            //////////////////////////////////////////////////////////////////////////////
            syncLikes(toConfig.host + String.format(API_ODATA_PATCH_QUESTIONS_LIKED_URL, toQuestionUUID), liked);

            // Get Answers
            for (int a = answers.length()-1; a >=0; --a) {
                final JSONObject fromAnswersJson = answers.getJSONObject(a);
                
                //////////////////////////////////////////////////////////////////////////////
                // [DESTINATION] UPLOADING AN ANSWER
                //////////////////////////////////////////////////////////////////////////////                
                String postToAnswerUrl = toConfig.host + String.format(API_ODATA_POST_FORUM_QUESTION_ANSWER_URL,  toQuestionUUID);
                JamTokenManager.getInstance().setTokenFromConfig(toConfig,  creatorEmail); // FROM GROUP CREATOR

                JSONObject answerParamBody = new JSONObject();
                answerParamBody.put("Comment", fromAnswersJson.getString("Comment"));
                answerParamBody.put("Liked", fromAnswersJson.getBoolean("Liked"));
                
                JamNetworkParam answerParamHeader = new JamNetworkParam();
                answerParamHeader.add("Content-Type", "application/json");

                final InputStream answerInputStream = new ByteArrayInputStream(answerParamBody.toString().getBytes());
                JSONObject answerJSON = JamNetworkManager.getInstance().PostRequest(postToAnswerUrl, answerParamHeader, answerInputStream);

                JSONObject toAnswerJson = answerJSON.getJSONObject("d").getJSONObject("results");                
                
                Answer answerObj = new Answer();
                answerObj.fromUUID =  fromAnswersJson.getString("Id");
                answerObj.comment = fromAnswersJson.getString("Comment");
                answerObj.liked  = fromAnswersJson.getBoolean("Liked");
                answerObj.toUUID = toAnswerJson.getString("Id");
                answersMap.put(answerObj.fromUUID, answerObj);

                //////////////////////////////////////////////////////////////////////////////
                // [DESTINATION] UPLOADING Liked
                //////////////////////////////////////////////////////////////////////////////
                System.out.println("       [SOURCE] uploading Wall comment / Answer ("+answerObj.comment+")  liked: " + answerObj.liked);
                syncLikes( toConfig.host + String.format(API_ODATA_PATCH_WALL_COMMENTS_LIKED_URL, answerObj.toUUID), answerObj.liked);

                //////////////////////////////////////////////////////////////////////////////
                // [DESTINATION] UPLOADING Replies
                //////////////////////////////////////////////////////////////////////////////
                String fromComments     = fromConfig.host + String.format(API_ODATA_GET_WALL_COMMENTS_URL, answerObj.fromUUID );
                String postComments     = toConfig.host + String.format(API_ODATA_POST_WALL_COMMENTS_URL, answerObj.toUUID );
                
                System.out.println("       [SOURCE] Getting Comments for Answers: " + answerObj.comment);
                syncComments(group, creatorEmail, fromComments, postComments);             
            }
            
            
            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOAD BEST ANSWERS
            //////////////////////////////////////////////////////////////////////////////                
            if( questionJson.getBoolean("HasBestAnswer") == true) {
                JSONObject bestAnswerJson = questionJson.getJSONObject("BestAnswer");
                
                if(bestAnswerJson!=null) {
                    String toBestAnswers = toConfig.host + String.format(API_ODATA_POST_QUESTION_BEST_ANSWER_URL, toQuestionUUID );
                    JamTokenManager.getInstance().setTokenFromConfig(toConfig,  bestAnswerJson.getJSONObject("Creator").getString("Email"));
        
                    JSONObject answerParamBody = new JSONObject();
                    
                    String bestToAnswerUUID = answersMap.get(bestAnswerJson.getString("Id")).toUUID;
                    
                    String link = "WallComments('"+bestToAnswerUUID+"')";
                    System.out.println("              [POST] BEST ANSWER WallComments uri link=> " + link );
                    
                    answerParamBody.put("uri", link);
                    
                    JamNetworkParam answerParamHeader = new JamNetworkParam();
                    answerParamHeader.add("Content-Type", "application/json");
        
                    final InputStream answerInputStream = new ByteArrayInputStream(answerParamBody.toString().getBytes());
                    JamNetworkManager.getInstance().PostRequest(toBestAnswers, answerParamHeader, answerInputStream);
        
                }      
            }
        }
    }

    private void syncAllIdeas(JamSyncGroupMemberManager.Group group) throws Exception {
        final JamConfig.ConfigInfo toConfig = JamConfig.getInstance().getToConfig();
        final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();

        //////////////////////////////////////////////////////////////////////////////
        // [SOURCE] DOWNLOAD ALL IDEAS
        //////////////////////////////////////////////////////////////////////////////
        System.out.println("       [SOURCE] Getting AllIdeas for group: " + group.fromUUID);
        
        JamTokenManager.getInstance().setTokenFromConfig(fromConfig,  group.creatorEmail); // FROM GROUP CREATOR
        String forumsUrl = fromConfig.host + String.format(API_ODATA_GET_GROUP_ALL_IDEAS_URL, group.fromUUID);
        JSONObject fromIdeasJSON = JamNetworkManager.getInstance().GetRequest(forumsUrl, new JamNetworkParam()).getJSONObject("d");
        
        JSONArray fromIdeasListJson = fromIdeasJSON.getJSONArray("results");
 
        List<JSONObject> ideas = new ArrayList();
        while (fromIdeasListJson !=null && fromIdeasListJson.length() > 0) {

            System.out.println("        Found ideas: " + fromIdeasListJson.length());
            for (int i = 0; i <fromIdeasListJson.length(); ++i) {
                ideas.add(fromIdeasListJson.getJSONObject(i));
            }
            
            // If here are more pages, continue to request for next page
            if (fromIdeasJSON.has("__next")) {
                final String toNextPageUrl = fromIdeasJSON.getString("__next");
                String url = fromConfig.host + "/api/v1/OData/" + toNextPageUrl;
                fromIdeasJSON = JamNetworkManager.getInstance().GetRequest(url, new JamNetworkParam()).getJSONObject("d");
                fromIdeasListJson = fromIdeasJSON.getJSONArray("results");  
            } else {
                fromIdeasListJson = null;
            }
            
        }
        
        for (int i = ideas.size()-1; i >=0; --i) {
            final JSONObject ideaJson = ideas.get(i);
            
            // Get Ideas
            String name         = ideaJson.getString("Name");
            String uuid         = ideaJson.getString("Id");
            String creatorEmail = ideaJson.getJSONObject("Creator").getString("Email");
            String fromForumUUID= ideaJson.getJSONObject("Forum").getString("Id");
            String content      = ideaJson.getString("Content");
            String status       = ideaJson.getString("Status");
            String vote         = "";
            
            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOADING AN IDEA
            //////////////////////////////////////////////////////////////////////////////
            String postToIdeasUrl = toConfig.host + String.format(API_ODATA_POST_GROUP_FORUMS_IDEAS_URL,  forumsMap.get(fromForumUUID).toUUID);
            JamTokenManager.getInstance().setTokenFromConfig(toConfig,  creatorEmail); // FROM GROUP CREATOR

            JSONObject paramBody = new JSONObject();
            
            paramBody.put("Name", name);
            paramBody.put("Content", content);
            paramBody.put("Status", status);
            paramBody.put("Vote", vote);
            
            System.out.println("       [SOURCE] Found Idea => name:"+name+" Status:" +status );

            JamNetworkParam paramHeader = new JamNetworkParam();
            paramHeader.add("Content-Type", "application/json");

            final InputStream inputStream = new ByteArrayInputStream(paramBody.toString().getBytes());
            JSONObject toIdeasJSON = JamNetworkManager.getInstance().PostRequest(postToIdeasUrl, paramHeader, inputStream);

            JSONObject toForumJson = toIdeasJSON.getJSONObject("d").getJSONObject("results");                
            String toIdeaUUID = toForumJson.getString("Id");            

            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOADING AN IDEA STATUS
            //////////////////////////////////////////////////////////////////////////////
            String postToIdeasInfoUrl = toConfig.host + String.format(API_ODATA_PATCH_GROUP_FORUMS_IDEAS_INFO_URL, toIdeaUUID);
            JamTokenManager.getInstance().setTokenFromConfig(toConfig,  creatorEmail); // FROM GROUP CREATOR

            JSONObject ideaInfoParamBody = new JSONObject();
            
            ideaInfoParamBody.put("Status", status);

            JamNetworkParam ideaInfoParamHeader = new JamNetworkParam();
            ideaInfoParamHeader.add("Content-Type", "application/json");

            final InputStream toIdeaInfoInputStream = new ByteArrayInputStream(ideaInfoParamBody.toString().getBytes());
            JamNetworkManager.getInstance().PatchRequest(postToIdeasInfoUrl, paramHeader, toIdeaInfoInputStream);
        }   
    }

    
    private void syncAllDiscussions(JamSyncGroupMemberManager.Group group) throws Exception {
        final JamConfig.ConfigInfo toConfig = JamConfig.getInstance().getToConfig();
        final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();

        //////////////////////////////////////////////////////////////////////////////
        // [SOURCE] DOWNLOAD all discussions
        //////////////////////////////////////////////////////////////////////////////
        System.out.println("       [SOURCE] Getting AllDiscussions for group: " + group.fromUUID);
        
        JamTokenManager.getInstance().setTokenFromConfig(fromConfig,  group.creatorEmail); // FROM GROUP CREATOR
        String dicussionsUrl = fromConfig.host + String.format(API_ODATA_GET_GROUP_ALL_DISCUSSIONS_URL, group.fromUUID);
        JSONObject fromdiscussionsJSON = JamNetworkManager.getInstance().GetRequest(dicussionsUrl, new JamNetworkParam()).getJSONObject("d");
        JSONArray fromDiscussionsListJson = fromdiscussionsJSON.getJSONArray("results");
 
        List<JSONObject> discussions = new ArrayList<JSONObject>();
        while (fromDiscussionsListJson !=null && fromDiscussionsListJson.length() > 0) {

            System.out.println("        Found ideas: " + fromDiscussionsListJson.length());
            for (int i = 0; i <fromDiscussionsListJson.length(); ++i) {
                discussions.add(fromDiscussionsListJson.getJSONObject(i));
            }
            
            // If here are more pages, continue to request for next page
            if (fromdiscussionsJSON.has("__next")) {
                final String toNextPageUrl = fromdiscussionsJSON.getString("__next");
                String url = fromConfig.host + "/api/v1/OData/" + toNextPageUrl;
                fromdiscussionsJSON = JamNetworkManager.getInstance().GetRequest(url, new JamNetworkParam()).getJSONObject("d");
                fromDiscussionsListJson = fromdiscussionsJSON.getJSONArray("results");  
            } else {
                fromDiscussionsListJson = null;
            }
        }
        
        for (int i = discussions.size()-1; i >=0 ; --i) {
            final JSONObject discussionJson = discussions.get(i);
            
            // Get Discussions
            String name                 = discussionJson.getString("Name");
            String fromDiscussionUUID   = discussionJson.getString("Id");
            String creatorEmail         = discussionJson.getJSONObject("Creator").getString("Email");
            String fromForumUUID        = discussionJson.getJSONObject("Forum").getString("Id");
            String content              = discussionJson.getString("Content");
            int commentCount            = discussionJson.getInt("CommentsCount");
            boolean liked               = discussionJson.getBoolean("Liked");
            
            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOADING A QUESTION
            //////////////////////////////////////////////////////////////////////////////
            String postToIdeasUrl = toConfig.host + String.format(API_ODATA_POST_GROUP_FORUMS_DISCUSSIONS_URL,  forumsMap.get(fromForumUUID).toUUID);
            JamTokenManager.getInstance().setTokenFromConfig(toConfig,  creatorEmail); // FROM GROUP CREATOR

            JSONObject paramBody = new JSONObject();
            
            paramBody.put("Name", name);
            paramBody.put("Content", content);
            paramBody.put("Liked",liked );

            JamNetworkParam paramHeader = new JamNetworkParam();
            paramHeader.add("Content-Type", "application/json");

            final InputStream inputStream = new ByteArrayInputStream(paramBody.toString().getBytes());
            JSONObject toDiscussionsJSON = JamNetworkManager.getInstance().PostRequest(postToIdeasUrl, paramHeader, inputStream);

            JSONObject toForumJson = toDiscussionsJSON.getJSONObject("d").getJSONObject("results");                
            String toDiscussionUUID = toForumJson.getString("Id");

            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOADING liked
            //////////////////////////////////////////////////////////////////////////////
            syncLikes(toConfig.host + String.format(API_ODATA_PATCH_DISCUSSIONS_LIKED_URL, toDiscussionUUID),liked);            

            //////////////////////////////////////////////////////////////////////////////
            // [DESTINATION] UPLOADING comments
            //////////////////////////////////////////////////////////////////////////////
            String fromComments     = fromConfig.host + String.format(API_ODATA_GET_DISCUSSIONS_COMMENTS_URL, fromDiscussionUUID);
            String postComments     = toConfig.host + String.format(API_ODATA_POST_DISCUSSIONS_COMMENTS_URL, toDiscussionUUID );
            syncComments(group, creatorEmail, fromComments, postComments);
        }            
    }

    @Override
    public void render() {
        System.out.println("JamSyncGroupQIDs::Render!");
    }
    
    @Override
    public void sync(String fromGroupUUID) {
        System.out.println("\n  ##################################################################################################################################################");
        System.out.println("  JamSyncGroupQIDs::SYNC Begin!");

        try {    
            final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();

            // For each group, get list of member and add them to cache
            JamSyncGroupMemberManager.Group group = JamSyncGroupMemberManager.getInstance().getGroup(fromGroupUUID);

            syncAllForumInfo(group);
            syncAllQuestions(group);
            syncAllIdeas(group);
            syncAllDiscussions(group);

        } catch (final Exception e) {
            System.out.println("\n  JamSyncGroupQIDs::Sync error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("  JamSyncGroupQIDs::SYNC End!");
        System.out.println("\n  ##################################################################################################################################################");
    }
}
