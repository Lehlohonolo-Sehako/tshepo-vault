package app.tshepo.integration.investec;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "investec")
public class InvestecProperties {

    private String clientId;
    private String clientSecret;
    private String apiKey;
    private String apiBase = "https://openapisandbox.investec.com";
    private String tokenUri = "https://openapisandbox.investec.com/identity/v2/oauth2/token";
    private String authorizeUri = "https://openapisandbox.investec.com/identity/v2/oauth2/authorize";
    private String redirectUri = "http://localhost:3000/oauth-callback";

    public String getAuthorizeUri() {
        return authorizeUri;
    }

    public void setAuthorizeUri(String authorizeUri) {
        this.authorizeUri = authorizeUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiBase() {
        return apiBase;
    }

    public void setApiBase(String apiBase) {
        this.apiBase = apiBase;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}
