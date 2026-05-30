package app.tshepo.integration.investec;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "investec")
public class InvestecProperties {

    private String clientId;
    private String clientSecret;
    private String apiBase = "https://openapi.investec.com";
    private String tokenUri = "https://identity.investec.com/am/oauth2/access_token";
    private String authorizeUri = "https://identity.investec.com/am/oauth2/alpha/authorize";

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
}
