package app.tshepo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class IssuedClaimTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static IssuedClaim getIssuedClaimSample1() {
        return new IssuedClaim().id(1L).currency("currency1").periodMonths(1);
    }

    public static IssuedClaim getIssuedClaimSample2() {
        return new IssuedClaim().id(2L).currency("currency2").periodMonths(2);
    }

    public static IssuedClaim getIssuedClaimRandomSampleGenerator() {
        return new IssuedClaim()
            .id(longCount.incrementAndGet())
            .currency(UUID.randomUUID().toString())
            .periodMonths(intCount.incrementAndGet());
    }
}
