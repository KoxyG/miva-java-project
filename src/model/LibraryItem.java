package model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstract base class for all library resources.
 * Implements Borrowable so every item can be checked out and returned uniformly.
 */
public abstract class LibraryItem implements Borrowable {
    protected String id;
    protected String title;
    protected String author;
    protected int year;
    protected boolean available;
    protected String currentBorrowerId;
    protected LocalDate dueDate;
    protected int borrowCount; // incremented each time the item is borrowed (for reports)

    public LibraryItem(String id, String title, String author, int year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.available = true;
        this.borrowCount = 0;
    }

    /** Subclasses must identify themselves (Book, Magazine, Journal). */
    public abstract String getItemType();

    /** Broad grouping used in category distribution reports. */
    public abstract String getCategory();

    @Override
    public boolean borrow(String userId, LocalDate dueDate) {
        if (!available) {
            return false; // already checked out
        }
        available = false;
        currentBorrowerId = userId;
        this.dueDate = dueDate;
        borrowCount++;
        return true;
    }

    @Override
    public boolean returnItem() {
        if (available) {
            return false; // nothing to return
        }
        available = true;
        currentBorrowerId = null;
        dueDate = null;
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getCurrentBorrowerId() {
        return currentBorrowerId;
    }

    @Override
    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(int borrowCount) {
        this.borrowCount = borrowCount;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setCurrentBorrowerId(String currentBorrowerId) {
        this.currentBorrowerId = currentBorrowerId;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /** True when borrowed and today's date is past the due date. */
    public boolean isOverdue() {
        return !available && dueDate != null && LocalDate.now().isAfter(dueDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LibraryItem that = (LibraryItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s by %s (%d) - %s",
                getItemType(), title, author, year, available ? "Available" : "Borrowed");
    }
}
