import controller.BorrowController;
import model.Book;
import model.LibraryDatabase;
import model.UserAccount;

/** Tests borrow, return, waitlist queue, and auto-assignment to next user. */
public class BorrowControllerTest {

    private LibraryDatabase database;
    private BorrowController controller;

    public void runAll() {
        System.out.println("BorrowControllerTest");
        testBorrowAvailableItem();
        testBorrowUnavailableAddsToWaitlist();
        testReturnItemFreesBookWhenNoWaitlist();
        testWaitlistAutoAssignOnReturn();
        testReserveDuplicateRejected();
    }

    /** Fresh database with one book and two users for each test. */
    private void freshSetup() {
        database = new LibraryDatabase();
        controller = new BorrowController(database);
        database.addItem(new Book("BK-1", "Test Book", "Author", 2020, "123", 100));
        database.addUser(new UserAccount("U-1", "Alice", "alice@test.com"));
        database.addUser(new UserAccount("U-2", "Bob", "bob@test.com"));
    }

    private void testBorrowAvailableItem() {
        freshSetup();
        boolean result = controller.borrowItem("BK-1", "U-1");
        TestAssert.assertTrue(result, "borrow succeeds when item is available");
        TestAssert.assertFalse(database.findItemById("BK-1").isAvailable(), "item marked unavailable after borrow");
        TestAssert.assertEquals("U-1", database.findItemById("BK-1").getCurrentBorrowerId(), "borrower ID recorded");
    }

    private void testBorrowUnavailableAddsToWaitlist() {
        freshSetup();
        controller.borrowItem("BK-1", "U-1");
        boolean result = controller.borrowItem("BK-1", "U-2");
        TestAssert.assertFalse(result, "borrow fails when item unavailable");
        TestAssert.assertEquals(1, database.getWaitlistForItem("BK-1").size(), "user added to item waitlist");
    }

    private void testReturnItemFreesBookWhenNoWaitlist() {
        freshSetup();
        controller.borrowItem("BK-1", "U-1");
        boolean result = controller.returnItem("BK-1");
        TestAssert.assertTrue(result, "return succeeds for borrowed item");
        TestAssert.assertTrue(database.findItemById("BK-1").isAvailable(), "item available when waitlist is empty");
    }

    private void testWaitlistAutoAssignOnReturn() {
        freshSetup();
        controller.borrowItem("BK-1", "U-1");
        controller.reserveItem("BK-1", "U-2");
        controller.returnItem("BK-1");

        TestAssert.assertFalse(database.findItemById("BK-1").isAvailable(), "item auto-assigned to waitlisted user");
        TestAssert.assertEquals("U-2", database.findItemById("BK-1").getCurrentBorrowerId(),
                "waitlisted user becomes new borrower");
    }

    private void testReserveDuplicateRejected() {
        freshSetup();
        boolean first = controller.reserveItem("BK-1", "U-1");
        boolean duplicate = controller.reserveItem("BK-1", "U-1");
        TestAssert.assertTrue(first, "first reservation succeeds");
        TestAssert.assertFalse(duplicate, "duplicate reservation is rejected");
    }
}
