package app.tshepo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class VerificationEventTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static VerificationEvent getVerificationEventSample1() {
        return new VerificationEvent().id(1L).credentialRef("credentialRef1");
    }

    public static VerificationEvent getVerificationEventSample2() {
        return new VerificationEvent().id(2L).credentialRef("credentialRef2");
    }

    public static VerificationEvent getVerificationEventRandomSampleGenerator() {
        return new VerificationEvent().id(longCount.incrementAndGet()).credentialRef(UUID.randomUUID().toString());
    }
}
