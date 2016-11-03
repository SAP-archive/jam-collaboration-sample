package utils;

public class JamNetworkUrl {
    private StringBuilder sb = null;
    private String baseUrl = "";

    public JamNetworkUrl(final String urlStr) {
        baseUrl = urlStr;
    }

    public void add(final String key, final String value) {
        if (sb != null) {
            sb = new StringBuilder();
        } else {
            sb.append("&");
        }

        sb.append(key).append("=").append(value);
    }

    @Override
    public String toString() {
        if (sb != null) {
            return baseUrl + "\\" + sb.toString();
        }

        return baseUrl;
    }
}
