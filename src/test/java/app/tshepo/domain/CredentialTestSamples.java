package app.tshepo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CredentialTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Credential getCredentialSample1() {
        return new Credential()
            .id(1L)
            .holderLogin("holderLogin1")
            .title("title1")
            .purpose("purpose1")
            .issuerDid("issuerDid1")
            .claimsSummary("claimsSummary1")
            .vcRef("vcRef1");
    }

    public static Credential getCredentialSample2() {
        return new Credential()
            .id(2L)
            .holderLogin("holderLogin2")
            .title("title2")
            .purpose("purpose2")
            .issuerDid("issuerDid2")
            .claimsSummary("claimsSummary2")
            .vcRef("vcRef2");
    }

    public static Credential getCredentialRandomSampleGenerator() {
        return new Credential()
            .id(longCount.incrementAndGet())
            .holderLogin(UUID.randomUUID().toString())
            .title(UUID.randomUUID().toString())
            .purpose(UUID.randomUUID().toString())
            .issuerDid(UUID.randomUUID().toString())
            .claimsSummary(UUID.randomUUID().toString())
            .vcRef(UUID.randomUUID().toString());
    }
}
