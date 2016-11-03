package client;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URLDecoder;
import java.net.URLEncoder;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import java.security.PrivateKey;
import java.security.KeyFactory;

import utils.JamConfig;
import utils.JamNetworkManager;
import utils.JamNetworkParam;
import utils.JamNetworkUrl;
import utils.JamTokenManager;
import sync.JamSyncGroupContents;
import sync.JamSyncGroupMembership;
import sync.JamSyncGroupQIDs;

import sync.JamSyncGroupMembers;

import java.security.PublicKey;
import java.security.Signature;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.io.ByteArrayInputStream;

import javax.crypto.Cipher;

import com.sap.jam.api.security.SignatureUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class SyncClient {

    public SyncClient() {
    }

    // Run the test client to sync from one Jam instance to the next
    // Only Contents, Forums, and Group Followers are synced up
    public void Run() {
        try {
            // Load Configuration file
            JamConfig.getInstance().load("config.json");

            final JamConfig.ConfigInfo fromConfig = JamConfig.getInstance().getFromConfig();

            // For each group, get list of member and add them to cache
            for (final String fromGroupUUID : fromConfig.groupList) {
                System.out.println("\n\n**************************************************************************************************");
                System.out.println("[TESTCLIENT] Start Syncing Group : " + fromGroupUUID);
                System.out.println("**************************************************************************************************");
                // Sync groups and members info from Source Instance
                final JamSyncGroupMembers syncMembers = new JamSyncGroupMembers();
                syncMembers.sync(fromGroupUUID);
    
                // Sync group members (invite member and accept invite) to Destination Instance
                final JamSyncGroupMembership syncGroupMembership = new JamSyncGroupMembership();
                syncGroupMembership.sync(fromGroupUUID);
    
                final JamSyncGroupContents syncContents = new JamSyncGroupContents();
                syncContents.sync(fromGroupUUID);
    
                // Sync Forums QID
                final JamSyncGroupQIDs syncQIDs = new JamSyncGroupQIDs();
                syncQIDs.sync(fromGroupUUID);
                System.out.println("**************************************************************************************************");
                System.out.println("[TESTCLIENT] Finished Syncing Group : " + fromGroupUUID);
                System.out.println("**************************************************************************************************\n");
            }
            // Sync Followers from a group
        } catch (final Exception e) {
            System.out.println("\nTest Client experience a failure with error: " + e.toString());

        }

    }
}
