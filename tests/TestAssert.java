/**
 * Lightweight assertion helpers for running tests without external libraries.
 */
public class TestAssert {

    private static int passed = 0;
    private static int failed = 0;

    public static void reset() {
        passed = 0;
        failed = 0;
    }

    public static void assertTrue(boolean condition, String message) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.err.println("  FAIL: " + message);
        }
    }

    public static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        assertTrue(expected == null ? actual == null : expected.equals(actual),
                message + " (expected=" + expected + ", actual=" + actual + ")");
    }

    public static void assertEquals(int expected, int actual, String message) {
        assertTrue(expected == actual, message + " (expected=" + expected + ", actual=" + actual + ")");
    }

    public static void assertEquals(double expected, double actual, double delta, String message) {
        assertTrue(Math.abs(expected - actual) <= delta,
                message + " (expected=" + expected + ", actual=" + actual + ")");
    }

    public static int getPassed() {
        return passed;
    }

    public static int getFailed() {
        return failed;
    }
}
