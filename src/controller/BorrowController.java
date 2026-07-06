package controller;

import model.LibraryDatabase;
import model.LibraryItem;
import model.UserAccount;

import java.time.LocalDate;
import java.util.Queue;

/**
 * Handles borrow, return, and reservation queue operations.
 * Uses a Queue (FIFO) so the first person to reserve gets the item next.
 */
public class BorrowController {
    private static final int DEFAULT_LOAN_DAYS = 14;

    private final LibraryDatabase database;

    public BorrowController(LibraryDatabase database) {
        this.database = database;
    }

    /**
     * Attempts to borrow an item for the given user.
     * If the item is already out, the user is silently added to the waitlist.
     */
    public boolean borrowItem(String itemId, String userId) {
        LibraryItem item = database.findItemById(itemId);
        UserAccount user = database.findUserById(userId);

        if (item == null || user == null) {
            return false; // invalid IDs — caller should validate input in GUI
        }

        if (!item.isAvailable()) {
            // Item checked out — offer waitlist instead of failing silently
            Queue<String> waitlist = database.getWaitlistForItem(itemId);
            if (!waitlist.contains(userId)) {
                waitlist.offer(userId); // FIFO queue: first reserved, first served
                database.getReservationQueue().offer(userId + ":" + itemId);
            }
            return false;
        }

        LocalDate dueDate = LocalDate.now().plusDays(DEFAULT_LOAN_DAYS);
        if (item.borrow(userId, dueDate)) {
            user.addBorrowRecord(itemId, LocalDate.now(), dueDate);
            return true;
        }
        return false;
    }

    /** Marks an item returned and automatically assigns it to the next waitlisted user. */
    public boolean returnItem(String itemId) {
        LibraryItem item = database.findItemById(itemId);
        if (item == null || item.isAvailable()) {
            return false;
        }

        String borrowerId = item.getCurrentBorrowerId();
        if (!item.returnItem()) {
            return false;
        }

        UserAccount user = database.findUserById(borrowerId);
        if (user != null) {
            user.completeBorrowRecord(itemId, LocalDate.now());
        }

        processWaitlist(itemId); // auto-borrow for next person in queue, if any
        return true;
    }

    /** Polls the FIFO waitlist and immediately borrows the item for the next user. */
    private void processWaitlist(String itemId) {
        Queue<String> waitlist = database.getWaitlistForItem(itemId);
        if (!waitlist.isEmpty()) {
            String nextUserId = waitlist.poll();
            UserAccount nextUser = database.findUserById(nextUserId);
            LibraryItem item = database.findItemById(itemId);
            if (nextUser != null && item != null && item.isAvailable()) {
                LocalDate dueDate = LocalDate.now().plusDays(DEFAULT_LOAN_DAYS);
                item.borrow(nextUserId, dueDate);
                nextUser.addBorrowRecord(itemId, LocalDate.now(), dueDate);
            }
        }
    }

    /** Explicitly join the waitlist without attempting a borrow first. */
    public boolean reserveItem(String itemId, String userId) {
        LibraryItem item = database.findItemById(itemId);
        UserAccount user = database.findUserById(userId);
        if (item == null || user == null) {
            return false;
        }

        Queue<String> waitlist = database.getWaitlistForItem(itemId);
        if (waitlist.contains(userId)) {
            return false;
        }
        waitlist.offer(userId);
        database.getReservationQueue().offer(userId + ":" + itemId);
        return true;
    }

    /** Builds a human-readable snapshot of all pending reservations. */
    public String getReservationQueueStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Global Reservation Queue:\n");
        if (database.getReservationQueue().isEmpty()) {
            sb.append("  (empty)\n");
        } else {
            for (String entry : database.getReservationQueue()) {
                String[] parts = entry.split(":");
                UserAccount user = database.findUserById(parts[0]);
                LibraryItem item = database.findItemById(parts[1]);
                sb.append(String.format("  %s waiting for '%s'\n",
                        user != null ? user.getName() : parts[0],
                        item != null ? item.getTitle() : parts[1]));
            }
        }
        return sb.toString();
    }
}
