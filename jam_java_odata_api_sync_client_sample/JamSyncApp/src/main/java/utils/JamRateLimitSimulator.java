package utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

class JamRateLimitSimulator {
    private static JamRateLimitSimulator singleton = new JamRateLimitSimulator();

    public boolean useFakeRateLimit = false;              //  Set to true to simulate rate limit

    private static int SIMULATE_RESET_NEXT_IN_SEC = 20;         // Modify to test next rate limit reset
    private static int SIMULATE_TOTAL_LIMIT = 800;              // Modify to test total rate limit total
    private static int SIMULATE_TOTAL_REMAINING = 3;          // Modify to test remaining limits
    
    static class FakeRateLimit {
        public int rateLimitTotal = SIMULATE_TOTAL_LIMIT;
        public int rateLimitRemaining = SIMULATE_TOTAL_REMAINING;
        public int rateLimitReset = SIMULATE_RESET_NEXT_IN_SEC;
        public Date nextUpdateTime = new Date();
    }

    public static JamRateLimitSimulator getInstance() {
        return singleton;
    }

    private JamRateLimitSimulator() {}
    
    public void update( String host ) {

        FakeRateLimit frl = fakeRateLimit.get("host");
        System.out.println("     [JAMRATELIMITSIMULATOR] updating similator for: " + host);

        if(useFakeRateLimit && frl !=null) {
            Calendar cal = Calendar.getInstance();
            Date dateNow = cal.getTime();
            System.out.println("                                now:                    " + dateNow.toLocaleString());
            System.out.println("                                nextUpdateTime:         " + frl.nextUpdateTime.toLocaleString());
            
            double timeDiff = (double)((frl.nextUpdateTime.getTime() - dateNow.getTime()) / 1000.0);

            System.out.println("     [JAMRATELIMITSIMULATOR] Next reset " + timeDiff);

            // Check for time to reset
            if(timeDiff < 0 ) {
                System.out.println("     [JAMRATELIMITSIMULATOR] LIMIT RESET!!");

                cal.setTime(dateNow);
                cal.add(Calendar.SECOND, frl.rateLimitReset);
                frl.rateLimitRemaining = SIMULATE_TOTAL_REMAINING;

                frl.nextUpdateTime = cal.getTime();
                timeDiff = (double)((frl.nextUpdateTime.getTime() - dateNow.getTime()) / 1000.0);
            }

            frl.rateLimitReset = (int)(timeDiff+0.5);  
            frl.rateLimitRemaining = SIMULATE_TOTAL_REMAINING;

        }
    }

    public void setFakeRateLimit(String host) {
        if(useFakeRateLimit) {
            System.out.println("     [JAMRATELIMITSIMULATOR] RESET FAKE LIMITS: " + host );
            
            if(!fakeRateLimit.containsKey(host)) {
                FakeRateLimit frl = new FakeRateLimit();
                
                frl.rateLimitTotal = SIMULATE_TOTAL_LIMIT;       
                frl.rateLimitRemaining = SIMULATE_TOTAL_REMAINING;  
                frl.rateLimitReset = SIMULATE_RESET_NEXT_IN_SEC;
                
                Calendar cal = Calendar.getInstance(); // creates calendar
                Date dateNow = cal.getTime();

                cal.setTime(dateNow);
                cal.add(Calendar.SECOND, frl.rateLimitReset);
                frl.nextUpdateTime = cal.getTime();
                
                fakeRateLimit.put(host, frl);
                
                System.out.println("     [JAMRATELIMITSIMULATOR]  Create rate limit for :" + host );
             //   update(host);

            } else {
                FakeRateLimit frl = fakeRateLimit.get(host);
                frl.rateLimitRemaining -= 1;
                frl.rateLimitReset -= 1;
            }
        }
    }
    
    public HashMap<String, FakeRateLimit> fakeRateLimit = new HashMap<String, FakeRateLimit>();
}