package generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomData {

    public static final double MIN_TRANSFER_AMOUNT = 0.01;
    public static final double MAX_DEPOSIT_AMOUNT = 5000.0;
    public static final double MONEY_TOLERANCE = 0.01;

    private static final long NON_EXISTENT_ID_RANGE = 1_000_000L;

    private RandomData() {
    }

    public static String getUsername() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static String getPassword() {
        return RandomStringUtils.randomAlphanumeric(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(5).toLowerCase() +
                RandomStringUtils.randomNumeric(3) + "%$";
    }

    private static double getRandomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static double getValidDepositAmount() {
        return ThreadLocalRandom.current()
                .nextDouble(0.01, 5000.0);
    }

    public static double getValidTransferAmount() {
        return ThreadLocalRandom.current()
                .nextDouble(0.01, 10000.0);
    }

    public static double getSmallTransferAmount() {
        return getRandomDouble(MIN_TRANSFER_AMOUNT, 5000.0);
    }

    public static long getNonExistentSenderId() {
        return ThreadLocalRandom.current()
                .nextLong(Long.MAX_VALUE - NON_EXISTENT_ID_RANGE, Long.MAX_VALUE);
    }

    public static long getNonExistentReceiverId() {
        return ThreadLocalRandom.current()
                .nextLong(Long.MAX_VALUE - 2 * NON_EXISTENT_ID_RANGE,
                Long.MAX_VALUE - NON_EXISTENT_ID_RANGE);
    }

    public static long getNonExistentAccountId() {
        return ThreadLocalRandom.current()
                .nextLong(Long.MAX_VALUE - NON_EXISTENT_ID_RANGE, Long.MAX_VALUE);
    }
}
