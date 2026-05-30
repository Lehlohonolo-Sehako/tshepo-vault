package app.tshepo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class BankConnectionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static BankConnection getBankConnectionSample1() {
        return new BankConnection().id(1L).holderLogin("holderLogin1").maskedAccount("maskedAccount1").accountType("accountType1");
    }

    public static BankConnection getBankConnectionSample2() {
        return new BankConnection().id(2L).holderLogin("holderLogin2").maskedAccount("maskedAccount2").accountType("accountType2");
    }

    public static BankConnection getBankConnectionRandomSampleGenerator() {
        return new BankConnection()
            .id(longCount.incrementAndGet())
            .holderLogin(UUID.randomUUID().toString())
            .maskedAccount(UUID.randomUUID().toString())
            .accountType(UUID.randomUUID().toString());
    }
}
