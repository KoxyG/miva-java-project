package controller;

import model.Book;
import model.Journal;
import model.LibraryDatabase;
import model.LibraryItem;
import model.Magazine;
import model.UserAccount;
import utils.FileHandler;
import utils.IDGenerator;
import utils.ItemCache;
import utils.SortAlgorithms;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Central controller coordinating library operations, reports, and persistence.
 * Acts as the facade between the GUI and the model/controller layers.
 */
public class LibraryManager {
    private final LibraryDatabase database;
    private final SearchEngine searchEngine;
    private final BorrowController borrowController;
    private final ItemCache itemCache;
    private boolean sortedByTitle; // tracks whether binary search is safe to use

    public LibraryManager() {
        this(true);
    }

    /** @param withSampleData when false, starts with an empty catalogue (used by unit tests) */
    public LibraryManager(boolean withSampleData) {
        this.database = new LibraryDatabase();
        this.searchEngine = new SearchEngine();
        this.borrowController = new BorrowController(database);
        this.itemCache = new ItemCache();
        this.sortedByTitle = false;
        if (withSampleData) {
            loadSampleData();
        }
    }

    public LibraryDatabase getDatabase() {
        return database;
    }

    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    public BorrowController getBorrowController() {
        return borrowController;
    }

    public ItemCache getItemCache() {
        return itemCache;
    }

    public boolean isSortedByTitle() {
        return sortedByTitle;
    }

    /**
     * Polymorphic processing — demonstrates runtime type dispatch via instanceof.
     * Each subclass contributes its own type-specific details to the output string.
     */
    public String processItem(LibraryItem item) {
        itemCache.recordAccess(item); // track this access in the fixed-size array cache
        StringBuilder info = new StringBuilder();
        info.append("Processing ").append(item.getItemType()).append(": ").append(item.getTitle()).append("\n");
        info.append("  Category: ").append(item.getCategory()).append("\n");
        info.append("  Author: ").append(item.getAuthor()).append("\n");
        info.append("  Year: ").append(item.getYear()).append("\n");
        info.append("  Status: ").append(item.isAvailable() ? "Available" : "Borrowed").append("\n");

        // Polymorphism: each subclass exposes different extra fields
        if (item instanceof Book book) {
            info.append("  ISBN: ").append(book.getIsbn()).append("\n");
            info.append("  Pages: ").append(book.getPages()).append("\n");
        } else if (item instanceof Magazine magazine) {
            info.append("  Issue: ").append(magazine.getIssueNumber()).append("\n");
            info.append("  Publisher: ").append(magazine.getPublisher()).append("\n");
        } else if (item instanceof Journal journal) {
            info.append("  Volume: ").append(journal.getVolume()).append("\n");
            info.append("  Field: ").append(journal.getField()).append("\n");
        }

        if (item.isOverdue()) {
            double charge = computeOverdueCharge(item, LocalDate.now(), 0.50);
            info.append(String.format("  OVERDUE - Charge: $%.2f\n", charge));
        }

        return info.toString();
    }

    /**
     * Recursive overdue charge: adds dailyRate for each day past the due date.
     * Base case: currentDate is on or before the due date (no more days to charge).
     */
    public double computeOverdueCharge(LibraryItem item, LocalDate currentDate, double dailyRate) {
        if (item.isAvailable() || item.getDueDate() == null) {
            return 0.0; // not borrowed — nothing to charge
        }
        if (!currentDate.isAfter(item.getDueDate())) {
            return 0.0; // base case: reached the due date, stop recursing
        }
        // Recursive step: charge one day, then check the previous day
        return dailyRate + computeOverdueCharge(item, currentDate.minusDays(1), dailyRate);
    }

    /**
     * Recursively counts items belonging to a category.
     * Base case: index reaches list size (no more items to inspect).
     */
    public int countByCategory(List<LibraryItem> items, String category, int index) {
        if (index >= items.size()) {
            return 0; // base case
        }
        int count = items.get(index).getCategory().equals(category) ? 1 : 0;
        return count + countByCategory(items, category, index + 1); // recursive step
    }

    /** Records an add-item action on the undo Stack so admin can reverse it. */
    public void addItem(LibraryItem item) {
        database.addItem(item);
        database.pushUndoAction(new LibraryDatabase.AdminAction(
                LibraryDatabase.AdminAction.ActionType.ADD_ITEM, item, null));
    }

    public void deleteItem(LibraryItem item) {
        database.removeItem(item);
        database.pushUndoAction(new LibraryDatabase.AdminAction(
                LibraryDatabase.AdminAction.ActionType.DELETE_ITEM, item, null));
    }

    public void addUser(UserAccount user) {
        database.addUser(user);
        database.pushUndoAction(new LibraryDatabase.AdminAction(
                LibraryDatabase.AdminAction.ActionType.ADD_USER, null, user));
    }

    public void deleteUser(UserAccount user) {
        database.removeUser(user);
        database.pushUndoAction(new LibraryDatabase.AdminAction(
                LibraryDatabase.AdminAction.ActionType.DELETE_USER, null, user));
    }

    /** Pops the most recent admin action from the Stack and reverses it. */
    public boolean undoLastAction() {
        LibraryDatabase.AdminAction action = database.popUndoAction();
        if (action == null) {
            return false;
        }
        switch (action.getType()) {
            case ADD_ITEM -> database.removeItem(action.getItem());
            case DELETE_ITEM -> database.addItem(action.getItem());
            case ADD_USER -> database.removeUser(action.getUser());
            case DELETE_USER -> database.addUser(action.getUser());
        }
        return true;
    }

    public void sortItems(SortAlgorithms.SortField field, SortAlgorithms.Algorithm algorithm) {
        SortAlgorithms.sort(database.getItems(), field, algorithm);
        sortedByTitle = (field == SortAlgorithms.SortField.TITLE);
    }

    /** Routes to the appropriate search algorithm based on the GUI dropdown selection. */
    public List<LibraryItem> search(String query, String searchType) {
        List<LibraryItem> items = database.getItems();

        return switch (searchType) {
            case "Title (Linear)" -> searchEngine.linearSearchByTitle(items, query);
            case "Title (Binary)" -> {
                // Binary search only valid when list is sorted by title
                if (sortedByTitle) {
                    yield searchEngine.binarySearchByTitle(items, query);
                }
                yield searchEngine.linearSearchByTitle(items, query);
            }
            case "Title (Recursive)" -> searchEngine.recursiveSearchByTitle(items, query, 0);
            case "Author" -> searchEngine.linearSearchByAuthor(items, query);
            case "Type" -> searchEngine.searchByType(items, query);
            default -> searchEngine.linearSearchByTitle(items, query);
        };
    }

    public String generateMostBorrowedReport() {
        List<LibraryItem> sorted = new ArrayList<>(database.getItems());
        sorted.sort(Comparator.comparingInt(LibraryItem::getBorrowCount).reversed());

        StringBuilder sb = new StringBuilder("=== Most Borrowed Items ===\n");
        int limit = Math.min(10, sorted.size());
        for (int i = 0; i < limit; i++) {
            LibraryItem item = sorted.get(i);
            sb.append(String.format("%d. %s (%s) - %d borrows\n",
                    i + 1, item.getTitle(), item.getItemType(), item.getBorrowCount()));
        }
        if (sorted.isEmpty()) {
            sb.append("No items in library.\n");
        }
        return sb.toString();
    }

    public String generateOverdueUsersReport() {
        StringBuilder sb = new StringBuilder("=== Users with Overdue Items ===\n");
        boolean found = false;
        for (UserAccount user : database.getUsers()) {
            if (user.hasOverdueItems(database.getItems())) {
                found = true;
                sb.append(user.getName()).append(" (").append(user.getUserId()).append(")\n");
                for (LibraryItem item : database.getItems()) {
                    if (user.getUserId().equals(item.getCurrentBorrowerId()) && item.isOverdue()) {
                        double charge = computeOverdueCharge(item, LocalDate.now(), 0.50);
                        sb.append(String.format("  - %s (Due: %s, Charge: $%.2f)\n",
                                item.getTitle(), item.getDueDate(), charge));
                    }
                }
            }
        }
        if (!found) {
            sb.append("No overdue items.\n");
        }
        return sb.toString();
    }

    public String generateCategoryDistributionReport() {
        StringBuilder sb = new StringBuilder("=== Category Distribution ===\n");
        Map<String, Integer> counts = new HashMap<>();
        for (LibraryItem item : database.getItems()) {
            counts.merge(item.getCategory(), 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            int recursiveCount = countByCategory(database.getItems(), entry.getKey(), 0);
            sb.append(String.format("%s: %d items (verified: %d)\n",
                    entry.getKey(), entry.getValue(), recursiveCount));
        }
        if (counts.isEmpty()) {
            sb.append("No items in library.\n");
        }
        return sb.toString();
    }

    public void saveData(File itemsFile, File usersFile) throws IOException {
        FileHandler.saveItems(database.getItems(), itemsFile);
        FileHandler.saveUsers(database.getUsers(), usersFile);
    }

    public void loadData(File itemsFile, File usersFile) throws IOException {
        database.getItems().clear();
        database.getUsers().clear();
        database.getItems().addAll(FileHandler.loadItems(itemsFile));
        database.getUsers().addAll(FileHandler.loadUsers(usersFile));
        itemCache.clear();
    }

    public List<String> getOverdueNotifications() {
        List<String> notifications = new ArrayList<>();
        for (LibraryItem item : database.getItems()) {
            if (item.isOverdue()) {
                UserAccount user = database.findUserById(item.getCurrentBorrowerId());
                String userName = user != null ? user.getName() : item.getCurrentBorrowerId();
                notifications.add(String.format("OVERDUE: '%s' borrowed by %s (due %s)",
                        item.getTitle(), userName, item.getDueDate()));
            }
        }
        return notifications;
    }

    private void loadSampleData() {
        addItem(new Book(IDGenerator.generateItemId("BK"), "Introduction to Algorithms",
                "Cormen", 2022, "978-0262046305", 1312));
        addItem(new Book(IDGenerator.generateItemId("BK"), "Clean Code",
                "Robert Martin", 2008, "978-0132350884", 464));
        addItem(new Magazine(IDGenerator.generateItemId("MG"), "National Geographic",
                "Various", 2024, 156, "NatGeo Media"));
        addItem(new Journal(IDGenerator.generateItemId("JN"), "Nature",
                "Various", 2024, "Vol 629", "Science"));
        addItem(new Book(IDGenerator.generateItemId("BK"), "Design Patterns",
                "Gamma", 1994, "978-0201633610", 395));

        addUser(new UserAccount(IDGenerator.generateUserId(), "Alice Johnson", "alice@university.edu"));
        addUser(new UserAccount(IDGenerator.generateUserId(), "Bob Smith", "bob@university.edu"));
        addUser(new UserAccount(IDGenerator.generateUserId(), "Carol Williams", "carol@university.edu"));

        database.getUndoStack().clear();
    }

    public LibraryItem createItem(String type, String title, String author, int year,
                                  String extra1, String extra2) {
        return switch (type) {
            case "Book" -> new Book(IDGenerator.generateItemId("BK"), title, author, year,
                    extra1, Integer.parseInt(extra2));
            case "Magazine" -> new Magazine(IDGenerator.generateItemId("MG"), title, author, year,
                    Integer.parseInt(extra1), extra2);
            case "Journal" -> new Journal(IDGenerator.generateItemId("JN"), title, author, year,
                    extra1, extra2);
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}
