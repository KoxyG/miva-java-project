package model;

import java.time.LocalDate;

/**
 * Interface for items that can be borrowed from the library.
 */
public interface Borrowable {
    boolean borrow(String userId, LocalDate dueDate);

    boolean returnItem();

    boolean isAvailable();

    String getCurrentBorrowerId();

    LocalDate getDueDate();
}
