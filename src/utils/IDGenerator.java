package utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique IDs for library items and users.
 */
public class IDGenerator {
    private static final AtomicInteger itemCounter = new AtomicInteger(1000);
    private static final AtomicInteger userCounter = new AtomicInteger(100);

    private IDGenerator() {
    }

    public static String generateItemId(String prefix) {
        return prefix + "-" + itemCounter.incrementAndGet();
    }

    public static String generateUserId() {
        return "U-" + userCounter.incrementAndGet();
    }

    public static void resetCounters(int itemStart, int userStart) {
        itemCounter.set(itemStart);
        userCounter.set(userStart);
    }
}
