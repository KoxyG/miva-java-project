import controller.LibraryManager;
import model.Book;
import model.UserAccount;

import java.time.LocalDate;
import java.util.List;

/** Tests undo stack, recursive charge/count, and polymorphic item processing. */
public class LibraryManagerTest {

    private LibraryManager manager;

    public void runAll() {
        System.out.println("LibraryManagerTest");
        manager = new LibraryManager(false);

        testAddAndUndoItem();
        testRecursiveOverdueCharge();
        testRecursiveCategoryCount();
        testPolymorphicProcessItem();
        testSortAndSearchIntegration();
    }

    private void testAddAndUndoItem() {
        Book book = new Book("BK-99", "Undo Test", "Author", 2020, "999", 50);
        manager.addItem(book);
        TestAssert.assertEquals(1, manager.getDatabase().getItems().size(), "item added to database");

        boolean undone = manager.undoLastAction();
        TestAssert.assertTrue(undone, "undo reverses last admin action");
        TestAssert.assertEquals(0, manager.getDatabase().getItems().size(), "item removed after undo");
    }

    private void testRecursiveOverdueCharge() {
        Book book = new Book("BK-OD", "Overdue Book", "Author", 2020, "111", 100);
        book.setAvailable(false);
        book.setDueDate(LocalDate.now().minusDays(3)); // 3 days overdue

        // $0.50 per day × 3 days = $1.50
        double charge = manager.computeOverdueCharge(book, LocalDate.now(), 0.50);
        TestAssert.assertEquals(1.50, charge, 0.01, "recursive overdue charge: 3 days at $0.50");
    }

    private void testRecursiveCategoryCount() {
        manager.addItem(new Book("1", "B1", "A", 2020, "x", 1));
        manager.addItem(new Book("2", "B2", "A", 2021, "y", 2));
        manager.addItem(new model.Magazine("3", "M1", "A", 2022, 1, "Pub"));

        int bookCount = manager.countByCategory(manager.getDatabase().getItems(), "Books", 0);
        TestAssert.assertEquals(2, bookCount, "recursive count finds all books");
    }

    private void testPolymorphicProcessItem() {
        Book book = new Book("BK-P", "Poly Book", "Author", 2020, "978-000", 200);
        String info = manager.processItem(book);
        TestAssert.assertTrue(info.contains("Book"), "processItem identifies Book type");
        TestAssert.assertTrue(info.contains("ISBN"), "processItem includes book-specific fields");
    }

    private void testSortAndSearchIntegration() {
        manager.addItem(new Book("A", "Zulu", "Z", 2020, "1", 1));
        manager.addItem(new Book("B", "Alpha", "A", 2021, "2", 2));

        manager.sortItems(utils.SortAlgorithms.SortField.TITLE, utils.SortAlgorithms.Algorithm.MERGE_SORT);
        List<model.LibraryItem> results = manager.search("Alpha", "Title (Binary)");
        TestAssert.assertEquals(1, results.size(), "sort + binary search integration works");
    }
}
