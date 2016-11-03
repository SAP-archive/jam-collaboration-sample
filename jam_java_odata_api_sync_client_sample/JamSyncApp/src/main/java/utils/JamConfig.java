package utils;

import java.io.BufferedReader;

import java.io.FileReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.io.FileNotFoundException;
import org.json.JSONObject;

public class JamConfig {
    public class SamlConfigInfo {
        public String idpId; 
        public String idpPrivateKey; 
        public String spId; 
    }

    public class ConfigInfo {
        public String host = "";
        public String clientId = "";
        public String clientSecret = "";
        public String companyUUID = "";
        public String[] groupList;
        public String grantType = "";
        public String proxyHost = "";
        public String proxyPort = "";
        public Proxy proxy = null;
        public String adminEmail = "";
        public SamlConfigInfo samlConfig = null;
        public boolean isDestination = false;
        
        public void validateConfigParams() {
        	StringBuilder sb = new StringBuilder("[ERROR] Update the config.json file and try again: \n");
        	
        	if (host.length() == 0) {
        		sb.append("Missing value for host. This value specifies the host for your jam instance. "
        				+ "For example: \"https://developer.sapjam.com\" \n");
        	} 
        	if (proxyHost.equals("proxy.example")) {
        		sb.append("proxy_host and proxy_port should be configured to your own proxy settings. "
        				+ "If you are not using a proxy, leave them as empty strings \n");
        	}
        	if (proxyHost.length() != 0 && proxyPort.length() == 0) {
        		sb.append("Missing value for proxy_port \n");
        	} 
        	if (clientId.length() == 0 || clientSecret.length() == 0) {
        		sb.append("Missing values for client_id and/or client_secret. The values should be the key "
        				+ "and secret obtained from creating a new OAuth Client in Admin -> Integrations -> OAuth clients \n");
        	}
        	if (companyUUID.length() == 0) {
        		sb.append("Missing value for company_uuid. Your company uuid can found in the url of the "
        				+ "Admin page. For example, \"abcdefg\" is the company uuid here: https://developer.sapjam.com/company/info/abcdefg \n");
        	}
        	if (!isDestination && groupList[0].isEmpty()) {
        		sb.append("Missing values for group_uuids. These are uuids of groups which will be copied "
        				+ "from the source instance to the destination instance. Must be a comma separated list with no spaces. For example: "
        				+ "\"group1uuid,group2uuid,group3uuid\" \n");
        	}
        	if (adminEmail.length() == 0) {
        		sb.append("Missing value for group_admin_email. This is the email of an administrator for "
        				+ "the groups which will be copied from the source instance to the destination instance \n");
        	}
        	if (samlConfig == null || samlConfig.idpId.length() == 0 || samlConfig.idpPrivateKey.length() == 0 || samlConfig.spId.length() == 0) {
        		sb.append("Missing values for saml configuration. These values can be obtained by creating a "
        				+ "new SAML Trusted IDP under Admin -> Integrations -> SAML Trusted IDPs \n");
        	}
        	
        	
        	if (!sb.toString().equals("[ERROR] Update the config.json file and try again: \n")) {
        		System.out.println(sb.toString());
        		System.exit(1);
        	}
        	
        }
    }
    

    private static JamConfig singleton = new JamConfig();
    private JSONObject jsonConfig = null;
    private ConfigInfo fromConfig;
    private ConfigInfo toConfig;

    static final Proxy PROXY_NONE = Proxy.NO_PROXY;

    private JamConfig() {

    }

    // Parse the json file and get the destination from / to configuration info
    private void ParseConfig() {

        fromConfig = new ConfigInfo();
        final JSONObject fromJson = (JSONObject)jsonConfig.get("from");
        fromConfig.host = fromJson.getString("host");
        fromConfig.clientId = fromJson.getString("client_id");
        fromConfig.clientSecret = fromJson.getString("client_secret");
        fromConfig.companyUUID = fromJson.getString("company_uuid");
        fromConfig.groupList = (fromJson.getString("group_uuids")).split(",");
        fromConfig.grantType = fromJson.getString("grant_type");
        fromConfig.proxyHost = fromJson.getString("proxy_host");
        fromConfig.proxyPort = fromJson.getString("proxy_port");
        fromConfig.adminEmail = fromJson.getString("group_admin_email");

        if (fromConfig.proxyHost.length() > 0 && fromConfig.proxyPort.length() > 0) {
            fromConfig.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(fromConfig.proxyHost, Integer.parseInt(fromConfig.proxyPort)));
        } else {
            fromConfig.proxy = Proxy.NO_PROXY;
        }

        if (fromJson.has("saml")) {
            final JSONObject samlJson = (JSONObject)fromJson.get("saml");

            fromConfig.samlConfig = new SamlConfigInfo();
            fromConfig.samlConfig.idpId = (String)samlJson.get("idp");
            fromConfig.samlConfig.idpPrivateKey = (String)samlJson.get("idp_private_key");
            fromConfig.samlConfig.spId = (String)samlJson.get("sp_id");
        }
        
        fromConfig.validateConfigParams();

        toConfig = new ConfigInfo();
        final JSONObject toJson = (JSONObject)jsonConfig.get("to");
        toConfig.host = toJson.getString("host");
        toConfig.clientId = toJson.getString("client_id");
        toConfig.clientSecret = toJson.getString("client_secret");
        toConfig.companyUUID = toJson.getString("company_uuid");
      //  toConfig.groupList = (toJson.getString("group_uuids")).split(",");
        toConfig.grantType = toJson.getString("grant_type");
        toConfig.proxyHost = toJson.getString("proxy_host");
        toConfig.proxyPort = toJson.getString("proxy_port");
        toConfig.adminEmail = toJson.getString("group_admin_email");
        toConfig.isDestination = true;

        if (toConfig.proxyHost.length() > 0 && toConfig.proxyPort.length() > 0) {
            toConfig.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(toConfig.proxyHost, Integer.parseInt(toConfig.proxyPort)));
        } else {
            toConfig.proxy = Proxy.NO_PROXY;
        }

        if (toJson.has("saml")) {
            final JSONObject samlJson = (JSONObject)toJson.get("saml");
            toConfig.samlConfig = new SamlConfigInfo();
            toConfig.samlConfig.idpId = (String)samlJson.get("idp");
            toConfig.samlConfig.idpPrivateKey = (String)samlJson.get("idp_private_key");
            toConfig.samlConfig.spId = (String)samlJson.get("sp_id");
        }
        
        toConfig.validateConfigParams();
    }

    public ConfigInfo getFromConfig() {
        return fromConfig;
    }

    public ConfigInfo getToConfig() {
        return toConfig;
    }

    public static JamConfig getInstance() {
        return singleton;
    }

    // Load the configuration file and store as JSON
    public void load(final String configFilePath) throws FileNotFoundException {
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(configFilePath));
            String result = "";
            String line = null;

            while ((line = reader.readLine()) != null) {
                result += line;
            }
            reader.close();

            jsonConfig = new JSONObject(result);

            ParseConfig();
        } catch (final Exception e) {
            System.out.println("JamConfig failed to load json file with error: " + e.toString());

        }
    }
    
}
