/**
 * Runs all SLCAS unit tests and prints a summary.
 * Usage: java -cp out:tests TestRunner
 */
public class TestRunner {

    public static void main(String[] args) {
        TestAssert.reset();

        System.out.println("=== SLCAS Unit Tests ===\n");

        new SearchEngineTest().runAll();
        new SortAlgorithmsTest().runAll();
        new BorrowControllerTest().runAll();
        new ItemCacheTest().runAll();
        new LibraryManagerTest().runAll();

        int passed = TestAssert.getPassed();
        int failed = TestAssert.getFailed();
        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");

        System.exit(failed > 0 ? 1 : 0);
    }
}
