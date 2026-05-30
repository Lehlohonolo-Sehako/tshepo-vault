package app.tshepo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class VerifierApiKeyTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static VerifierApiKey getVerifierApiKeySample1() {
        return new VerifierApiKey().id(1L).ownerLogin("ownerLogin1").label("label1").keyHash("keyHash1").callCount(1L);
    }

    public static VerifierApiKey getVerifierApiKeySample2() {
        return new VerifierApiKey().id(2L).ownerLogin("ownerLogin2").label("label2").keyHash("keyHash2").callCount(2L);
    }

    public static VerifierApiKey getVerifierApiKeyRandomSampleGenerator() {
        return new VerifierApiKey()
            .id(longCount.incrementAndGet())
            .ownerLogin(UUID.randomUUID().toString())
            .label(UUID.randomUUID().toString())
            .keyHash(UUID.randomUUID().toString())
            .callCount(longCount.incrementAndGet());
    }
}
