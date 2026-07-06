package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a library user with borrowing history.
 */
public class UserAccount {
    private final String userId;
    private String name;
    private String email;
    private final List<BorrowRecord> borrowingHistory;

    public UserAccount(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.borrowingHistory = new ArrayList<>();
    }

    public void addBorrowRecord(String itemId, LocalDate borrowDate, LocalDate dueDate) {
        borrowingHistory.add(new BorrowRecord(itemId, borrowDate, dueDate, null));
    }

    public void completeBorrowRecord(String itemId, LocalDate returnDate) {
        for (int i = borrowingHistory.size() - 1; i >= 0; i--) {
            BorrowRecord record = borrowingHistory.get(i);
            if (record.getItemId().equals(itemId) && record.getReturnDate() == null) {
                record.setReturnDate(returnDate);
                break;
            }
        }
    }

    public List<BorrowRecord> getBorrowingHistory() {
        return Collections.unmodifiableList(borrowingHistory);
    }

    public List<BorrowRecord> getActiveBorrows() {
        List<BorrowRecord> active = new ArrayList<>();
        for (BorrowRecord record : borrowingHistory) {
            if (record.getReturnDate() == null) {
                active.add(record);
            }
        }
        return active;
    }

    public boolean hasOverdueItems(List<LibraryItem> items) {
        for (BorrowRecord record : getActiveBorrows()) {
            if (record.getDueDate().isBefore(LocalDate.now())) {
                return true;
            }
        }
        for (LibraryItem item : items) {
            if (userId.equals(item.getCurrentBorrowerId()) && item.isOverdue()) {
                return true;
            }
        }
        return false;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", name, userId, email);
    }

    public static class BorrowRecord {
        private final String itemId;
        private final LocalDate borrowDate;
        private final LocalDate dueDate;
        private LocalDate returnDate;

        public BorrowRecord(String itemId, LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate) {
            this.itemId = itemId;
            this.borrowDate = borrowDate;
            this.dueDate = dueDate;
            this.returnDate = returnDate;
        }

        public String getItemId() {
            return itemId;
        }

        public LocalDate getBorrowDate() {
            return borrowDate;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public LocalDate getReturnDate() {
            return returnDate;
        }

        public void setReturnDate(LocalDate returnDate) {
            this.returnDate = returnDate;
        }

        public boolean isOverdue() {
            return returnDate == null && dueDate.isBefore(LocalDate.now());
        }
    }
}
