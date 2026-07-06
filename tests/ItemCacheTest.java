import model.Book;
import utils.ItemCache;

/** Tests the fixed-size array cache for tracking frequently accessed items. */
public class ItemCacheTest {

    public void runAll() {
        System.out.println("ItemCacheTest");
        testRecordAndIncrementAccess();
        testMostFrequentOrdering();
        testClear();
    }

    private void testRecordAndIncrementAccess() {
        ItemCache cache = new ItemCache();
        Book book = new Book("BK-1", "Popular Book", "Author", 2020, "123", 100);

        cache.recordAccess(book);
        cache.recordAccess(book);
        cache.recordAccess(book);

        String report = cache.getCacheReport();
        TestAssert.assertTrue(report.contains("Popular Book"), "cache report lists accessed item");
        TestAssert.assertTrue(report.contains("3 accesses"), "cache tracks access count");
    }

    private void testMostFrequentOrdering() {
        ItemCache cache = new ItemCache();
        Book a = new Book("A", "Book A", "X", 2020, "1", 10);
        Book b = new Book("B", "Book B", "Y", 2021, "2", 20);

        cache.recordAccess(a);
        cache.recordAccess(b);
        cache.recordAccess(b);
        cache.recordAccess(b);

        var frequent = cache.getMostFrequentItems();
        TestAssert.assertEquals("Book B", frequent[0].getTitle(), "most frequent item ranked first");
    }

    private void testClear() {
        ItemCache cache = new ItemCache();
        cache.recordAccess(new Book("C", "Book C", "Z", 2022, "3", 30));
        cache.clear();
        TestAssert.assertTrue(cache.getCacheReport().contains("No items accessed yet"), "clear resets cache");
    }
}
