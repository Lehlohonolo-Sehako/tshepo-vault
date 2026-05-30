package app.tshepo.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tshepo")
public class TshepoProperties {

    private String issuerDid = "did:web:localhost";
    private int credentialValidityDays = 180;

    public String getIssuerDid() {
        return issuerDid;
    }

    public void setIssuerDid(String issuerDid) {
        this.issuerDid = issuerDid;
    }

    public int getCredentialValidityDays() {
        return credentialValidityDays;
    }

    public void setCredentialValidityDays(int credentialValidityDays) {
        this.credentialValidityDays = credentialValidityDays;
    }
}
