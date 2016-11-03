package utils;

import java.lang.Thread;
import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.MalformedURLException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONObject;

import com.sap.jam.api.security.OAuth2SAMLUtil;

import utils.JamNetworkResult;
import utils.JamRateLimitSimulator;

public class JamNetworkManager {
    private static JamNetworkManager singleton = new JamNetworkManager();

    static class RateLimit {
        public String host = "";
        public Date nextUpdateTime = new Date();
        public int rateLimitRemaining = 800;
        public int rateLimitTotal = 800;
        public int rateLimitNextResetInSecs = 0;
        public float backOffTimeBetweenCalls = 1;
        public float backOffMinimumWaitForNextCall = 1;
        public float backoffExponentValue = 2;
        public float backOffLevel = 0;
        
        public RateLimit(String hostName) {
            host = hostName;
        }
    }

 
    static class ResponseData {
            
        public boolean isValid() {
            return connection != null;
        }        
        
        public void setConnection(HttpURLConnection con) throws IOException {
            connection = con;
            
            if (con != null) {
                System.out.println("            CONNECTON HEADERS : " + connection.getHeaderFields().toString());
                
                String host = con.getURL().getHost();
                
                if(JamRateLimitSimulator.getInstance().useFakeRateLimit) {
                    JamRateLimitSimulator.getInstance().setFakeRateLimit(host);
                    rateLimit = JamRateLimitSimulator.getInstance().fakeRateLimit.get(host).rateLimitTotal;
                    rateRemaining = JamRateLimitSimulator.getInstance().fakeRateLimit.get(host).rateLimitRemaining;
                    rateReset = JamRateLimitSimulator.getInstance().fakeRateLimit.get(host).rateLimitReset;                    
                } else {
                    rateLimit = connection.getHeaderFieldInt("X-RateLimit-Limit", 0);
                    rateRemaining = connection.getHeaderFieldInt("X-RateLimit-Remaining", 0);
                    rateReset = connection.getHeaderFieldInt("X-RateLimit-Reset", 0);
                }


                code = connection.getResponseCode();
                print();
            }
        }

        public ResponseData(HttpURLConnection con) throws IOException {

            System.out.println("          [RESPONSE]   Responsed with Rate Limit info:");
            setConnection(con);
        }

        public int rateLimit() {
            return rateLimit;
        }

        public int rateLimitRemaining() {
            return rateRemaining;
        }

        public int rateLimitReset() {
            return rateReset;
        }

        public int getResponseCode() {
            return code;
        }

        public void print() {
            if (connection != null) {
                System.out.println("                 [RATELIMIT] Response from " + connection.getURL().getHost() + " Rate Limit Info");
                System.out.println("                    Limit:      " + rateLimit());
                System.out.println("                    Remaining:  " + rateLimitRemaining());
                System.out.println("                    Reset:      " + rateLimitReset());
            }
        }


        private HttpURLConnection connection = null;
        private int code;
        private int rateLimit;
        private int rateRemaining;
        private int rateReset;

    }

    private HashMap<String, RateLimit> rateLimits= new HashMap<String, RateLimit>();
    private Proxy currentProxy = null;
    private String currentOAuthToken = null;


    private ResponseData lastResponseData = null;

    public JamNetworkManager() {
        try {
            lastResponseData = new ResponseData(null);            
        } catch (final Exception e) {
            throw new RuntimeException("Response connection object failed:" + e.toString(), e);
        }
    }

    public static JamNetworkManager getInstance() {
        return singleton;
    }

    public String buildSignedSAML2Assertion(
        final String idpId,
        final String destinationUri,
        final String subjectNameId,
        final String subjectNameIdFormat,
        final String subjectNameIdQualifier,

        final PrivateKey idpPrivateKey,
        final X509Certificate idpCertificate,
        final String spName,
        final Map<String, List<Object>> attributes) throws Exception {
        return OAuth2SAMLUtil.buildSignedSAML2Assertion(idpId,
            destinationUri,
            subjectNameId,
            subjectNameIdFormat,
            subjectNameIdQualifier,
            idpPrivateKey,
            idpCertificate,
            spName,
            attributes);
    }

    private static class DefaultTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    // Create a connection to url via post or get through a proxy / no proxy
    public HttpURLConnection createConnection(final String urlString, final String method /* [POST, GET, PATCH] */, final Proxy proxy) {
        HttpURLConnection con = null;

        while(con == null) {
            try {
                // Create a get GET url request with Authorization header
                final URL urlObj = new URL(urlString);

                // Disallow making any connections when this failed!
                while (!CalculateBackOff(urlObj,true)) {
                    System.out.println("[RATELIMIT]: Waiting for host:" + urlObj.getHost() + " to allow next call and respecting rate limits");
    
                    Thread.sleep(1000);
                }
    
                if (urlObj.getProtocol().equals("https")) {
                    // http://stackoverflow.com/questions/1828775/httpclient-and-ssl
                    // Nice trick (for non-production code) to create a SSL Context
                    // that accepts any cert.
                    // This lets us avoid configuring the self-signed Cheetah
                    // certificate.
                    final SSLContext ctx = SSLContext.getInstance("TLS");
                    ctx.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
                    SSLContext.setDefault(ctx);
    
                    con = (HttpsURLConnection)urlObj.openConnection(proxy);
                    ((HttpsURLConnection)con).setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(final String arg0, final SSLSession arg1) {
                            return true;
                        }
                    });
                } else {
                    con = (HttpURLConnection)urlObj.openConnection();
                }
                
                if( method.equals("PATCH") ) {
                    con.setRequestProperty("X-HTTP-Method-Override", method);
                    con.setRequestMethod("POST");   
                } else {
                    con.setRequestMethod(method);
                }
            } catch (final Exception e) {
                System.out.println("[NetworkMan]: Create connection to :" +urlString+ " failed.  Will re-try again");
    
            }
        }
        return con;
    }

    public void SetProxy(final Proxy proxy) {
        currentProxy = proxy;
    }

    public void SetOAuthToken(final String token) {
        currentOAuthToken = token;
    }

    public void LogNetworkStatus(final HttpURLConnection httpConn) {
        try {
            final int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED
                || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                // reads server's response
                final BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                final String response = reader.readLine();
                System.out.println("      Success: Server's response: " + response);
            } else {
                System.out.println("       Failure: Server's code: " + responseCode + " with the following error");

                final BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("           " + line);
                }
            }
        } catch (final Exception e) {
            System.out.println(" Network Error: " + e.toString());
        }
    }

    public float ThrottleWaitAmountInSeconds(float limit, float totalLimit, float resetLimitInSec, float minTime_in_sec, float maxTime_in_sec) {        
        if (totalLimit == 0 || limit == 0) {
            return 0;
        }

        float timeThreshold = 0.5f;
        float throttle = ((totalLimit - limit) / totalLimit);
        
        System.out.println("        [RATELIMIT]     Calculate ThrottleWaitAmountInSeconds => percentage: " + throttle);
        float waitTime = (resetLimitInSec / limit);

        System.out.println("        [RATELIMIT]     Calculate ThrottleWaitAmountInSeconds => wait amount: " + waitTime);
        throttle *= waitTime;

        System.out.println("        [RATELIMIT]     Calculate ThrottleWaitAmountInSeconds => per * wait: " + throttle);

        throttle = Math.min(throttle, maxTime_in_sec);
        throttle = Math.max(throttle, minTime_in_sec);

        if(limit < 0 ) {
            throttle = resetLimitInSec;
        }

        throttle += timeThreshold;

        System.out.println("        [RATELIMIT]     Calculate ThrottleWaitAmountInSeconds => final Wait: " + throttle);

        return (float)(Math.max(throttle, 1.0));
    }

    public float BackOffAmount(float basePower, float level, float minTime_in_sec, float maxTime_in_sec) {
        float backOff = (float)(Math.pow(basePower, level));
        backOff = Math.min(backOff, maxTime_in_sec);
        backOff = Math.max(backOff, minTime_in_sec);

        return backOff;
    }

    public void PrintStatus(RateLimit rateLimit) {
        Calendar cal = Calendar.getInstance(); // creates calendar
        Date dateNow = cal.getTime();
        double timeDiff = (double)((rateLimit.nextUpdateTime.getTime() - dateNow.getTime()) / 1000.0);
        
        System.out.println("\n---------------------------------------------------------------------------------------");
        System.out.println("       [NETWORKMAN]  Rate Limit info for host:     " + rateLimit.host);
        System.out.println("          current   Time:                          " + Calendar.getInstance().getTime().toLocaleString());
        System.out.println("         nextUpdate Time:                          " + rateLimit.nextUpdateTime.toLocaleString());
        System.out.println("              total diff:                          " + timeDiff + " seconds");
        System.out.println("                  LIMITS:");
        System.out.println("                     rateLimitTotal:               " + rateLimit.rateLimitTotal);
        System.out.println("                     rateLimitRemaining:           " + rateLimit.rateLimitRemaining);
        System.out.println("                     rateLimitNextResetInSecs:     " + rateLimit.rateLimitNextResetInSecs);
        
        System.out.println("                 BACKOFFS:");
        System.out.println("                    backOffTimeBetweenCalls:       " + rateLimit.backOffTimeBetweenCalls);
        System.out.println("                    backOffMinimumWaitForNextCall: " + rateLimit.backOffMinimumWaitForNextCall);
        System.out.println("                    backoffExponentValue:          " + rateLimit.backoffExponentValue);
        System.out.println("                    backOffLevel:                  " + rateLimit.backOffLevel);

        lastResponseData.print();
        
        System.out.println("---------------------------------------------------------------------------------------\n");
    }

    public boolean CalculateBackOff( URL url, boolean throttle ) throws IOException {
        ResponseData response = lastResponseData;
        
        String host = url.getHost();

        if(!rateLimits.containsKey(host)) {
            rateLimits.put(host, new RateLimit(host));
        }
        
        RateLimit rateLimit = rateLimits.get(host);
        
        // Update Rate Simulator
        if(response.isValid()) {
            JamRateLimitSimulator.getInstance().update(host);
        }        

        Calendar cal = Calendar.getInstance(); // creates calendar
        Date dateNow = cal.getTime();
        
        double timeDiff = (double)((rateLimit.nextUpdateTime.getTime() - dateNow.getTime()) / 1000.0);
        rateLimit.backOffTimeBetweenCalls = (float)(Math.max(timeDiff, 0.0));

        System.out.println("     [RATELIMIT] CalculateBackOff host: " + host  +"   diff time: " + timeDiff);

        if (timeDiff > 0.0) {
            System.out.println("     [RATELIMIT] CalculateBackOff WAITING TO MAKE call to host: " + host  +"   diff time: " + timeDiff);
            PrintStatus(rateLimit);
            return false;
        }

        if (response != null) {
            // If response fail:
            if (response.getResponseCode() == 429) {
                rateLimit.backOffLevel++;

                float throttleTime = BackOffAmount(2, rateLimit.backOffLevel, rateLimit.backOffMinimumWaitForNextCall, response.rateLimit());
                float backOff = BackOffAmount(2, rateLimit.backOffLevel, response.rateLimitReset(), response.rateLimit());
                backOff = Math.max(throttleTime, backOff);

                cal.setTime(dateNow);
                cal.add(Calendar.SECOND, (int)backOff);
                rateLimit.nextUpdateTime = cal.getTime();
            } else {
                rateLimit.backOffLevel = 0;
                    float throttleTime = ThrottleWaitAmountInSeconds(response.rateLimitRemaining(),
                        response.rateLimit(),
                        response.rateLimitReset(),
                        rateLimit.backOffMinimumWaitForNextCall,
                        response.rateLimitReset());

                    System.out.println("     [RATELIMIT] CalculateBackOff  calculate THROTTLE for: " + throttleTime + " seconds");

                    cal.setTime(dateNow); // sets calendar time/date
                    cal.add(Calendar.SECOND, (int)throttleTime);
                    rateLimit.nextUpdateTime = cal.getTime();
            }
            rateLimit.rateLimitRemaining = response.rateLimitRemaining();
            rateLimit.rateLimitNextResetInSecs = response.rateLimitReset();
            rateLimit.rateLimitTotal = response.rateLimit();
        }

        PrintStatus(rateLimit);
        response.setConnection(null);

        return true;
    }
    
    private JSONObject UploadRequest(final String urlString, final JamNetworkParam params, final InputStream inputStream, final String method) throws MalformedURLException {
        JSONObject jsonResult = null;

        try {
            System.out.println("\n       ["+method+"][TOKEN:"+currentOAuthToken+"]  Sending '"+method+"' request to URL : " + urlString);

            // Create a get URL POST request
            final HttpURLConnection httpConn = createConnection(urlString, method, currentProxy);

            for (final Map.Entry<String, String> entry : params.getParams().entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                System.out.println("          ["+method+"] Header:" + key + " : " + value);
                httpConn.setRequestProperty(key, value);
            }

            // Set some headers for accepting json response and set the OAuth token
            httpConn.setRequestProperty("Accept", "application/json");
            if (currentOAuthToken != null) {
                httpConn.setRequestProperty("Authorization", "OAuth " + currentOAuthToken);
            }

            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);

            if (inputStream != null) {
                final OutputStream outputStream = httpConn.getOutputStream();

                final byte[] buffer = new byte[1024];
                int bytesRead = -1;

                System.out.println("          ["+method+"] Start writing data...");

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                System.out.println("          ["+method+"] Data was written.");
                outputStream.close();
                inputStream.close();
            }

            // always check HTTP response code from server
            final int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED
                || responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {

                // reads server's response
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));

                String resultStr = "";
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    resultStr += line;
                }
                bufferedReader.close();

                if (!resultStr.equals("")) {
                    System.out.println("        JSON response: '" + resultStr + "'");
                    jsonResult = new JSONObject(resultStr);
                }
            } else {
                System.out.println("       Failure: Server's code: " + responseCode + " with the following error");

                final BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("           " + line);
                }
            }

            lastResponseData.setConnection(httpConn);

            httpConn.disconnect();

        } catch (final Exception e) {
            System.out.println("Error while uploading file: " + e.toString());
        }

        return jsonResult;
    }
    

    public JSONObject PatchRequest(final String urlString, final JamNetworkParam params, final InputStream inputStream) throws MalformedURLException {
        return UploadRequest(urlString, params, inputStream, "PATCH");
    }
        
    public JSONObject PostRequest(final String urlString, final JamNetworkParam params, final InputStream inputStream) throws MalformedURLException {
        return UploadRequest(urlString, params, inputStream, "POST");
    }

    public JSONObject GetRequest(final String urlString, final JamNetworkParam params) {
        JSONObject jsonResult = null;
        try {
            System.out.println("\n       [GET][TOKEN:"+currentOAuthToken+"] Sending 'GET' request to URL : " + urlString);

            // Create a get GET url request with Authorization header
            final HttpURLConnection con = createConnection(urlString, "GET", currentProxy);

            if (currentOAuthToken != null) {
                con.setRequestProperty("Authorization", "OAuth " + currentOAuthToken);
            }

            con.setRequestProperty("Accept", "application/json");

            System.out.println("      Header:");
            for (final Map.Entry<String, String> entry : params.getParams().entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                System.out.println("          " + key + " : " + value);
                con.setRequestProperty(key, value);
            }

            // Read response and parse to JSON
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            final int responseCode = con.getResponseCode();
            String resultStr = "";

            lastResponseData.setConnection(con);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED
                || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                // Add response data to string
                if (bufferedReader != null) {

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        resultStr += line;
                    }
                    bufferedReader.close();
                    System.out.println("        JSON response: '" + resultStr + "'");
                    jsonResult = new JSONObject(resultStr);
                }
            } else {
                // If it's an error, output the error to console
                System.out.println("      Failure: Server's code: " + responseCode + " with the following error");

                final BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("        " + line);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
//            throw new RuntimeException("Exception while making Get URL Request:" + e.toString(), e);
        }

        return jsonResult;
    }

    public JamNetworkResult GetRequestWithResult(final String urlString, final JamNetworkParam params) {
        JamNetworkResult result = null;
        try {
            System.out.println("\n       [GET][TOKEN:"+currentOAuthToken+"] Sending 'GET' request to URL : " + urlString);

            // Create a get GET url request with Authorization header
            final HttpURLConnection con = createConnection(urlString, "GET", currentProxy);

            if (currentOAuthToken != null) {
                con.setRequestProperty("Authorization", "OAuth " + currentOAuthToken);
            }

            System.out.println("      Header:");
            for (final Map.Entry<String, String> entry : params.getParams().entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                System.out.println("          " + key + " : " + value);
                con.setRequestProperty(key, value);
            }

            // Read response and parse to JSON
            final int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED
                || responseCode == HttpURLConnection.HTTP_ACCEPTED) {

                result = new JamNetworkResult();
                result.inputStream = con.getInputStream();
            } else {
                // If it's an error, output the error to console
                System.out.println("      Failure: Server's code: " + responseCode + " with the following error");
                final BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("        " + line);
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException("Exception while making Get URL Request:" + e.toString(), e);
        }

        return result;
    }
}
