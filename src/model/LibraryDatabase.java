package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

/**
 * Central data store using composition to manage items, users, reservations, and undo stack.
 *
 * Data structures used (as required by the assignment):
 *   ArrayList  — items and users
 *   Queue      — reservation / waitlist (FIFO)
 *   Stack      — undo history for admin operations (LIFO)
 */
public class LibraryDatabase {
    private final ArrayList<LibraryItem> items;
    private final ArrayList<UserAccount> users;
    private final Queue<String> reservationQueue;       // global queue of "userId:itemId" pairs
    private final Stack<AdminAction> undoStack;           // LIFO undo for admin panel
    private final Map<String, Queue<String>> itemWaitlists; // per-item waitlists

    public LibraryDatabase() {
        this.items = new ArrayList<>();
        this.users = new ArrayList<>();
        this.reservationQueue = new LinkedList<>();
        this.undoStack = new Stack<>();
        this.itemWaitlists = new HashMap<>();
    }

    public ArrayList<LibraryItem> getItems() {
        return items;
    }

    public ArrayList<UserAccount> getUsers() {
        return users;
    }

    public Queue<String> getReservationQueue() {
        return reservationQueue;
    }

    public Stack<AdminAction> getUndoStack() {
        return undoStack;
    }

    public Map<String, Queue<String>> getItemWaitlists() {
        return itemWaitlists;
    }

    public void addItem(LibraryItem item) {
        items.add(item);
    }

    public void removeItem(LibraryItem item) {
        items.remove(item);
    }

    public void addUser(UserAccount user) {
        users.add(user);
    }

    public void removeUser(UserAccount user) {
        users.remove(user);
    }

    public LibraryItem findItemById(String id) {
        for (LibraryItem item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    public UserAccount findUserById(String id) {
        for (UserAccount user : users) {
            if (user.getUserId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    /** Creates (or retrieves) the FIFO waitlist queue for a specific item. */
    public Queue<String> getWaitlistForItem(String itemId) {
        return itemWaitlists.computeIfAbsent(itemId, k -> new LinkedList<>());
    }

    /** Pushes an admin action onto the undo Stack. */
    public void pushUndoAction(AdminAction action) {
        undoStack.push(action);
    }

    public AdminAction popUndoAction() {
        if (undoStack.isEmpty()) {
            return null;
        }
        return undoStack.pop();
    }

    /**
     * Represents an admin action that can be undone.
     */
    public static class AdminAction {
        public enum ActionType { ADD_ITEM, DELETE_ITEM, ADD_USER, DELETE_USER }

        private final ActionType type;
        private final LibraryItem item;
        private final UserAccount user;

        public AdminAction(ActionType type, LibraryItem item, UserAccount user) {
            this.type = type;
            this.item = item;
            this.user = user;
        }

        public ActionType getType() {
            return type;
        }

        public LibraryItem getItem() {
            return item;
        }

        public UserAccount getUser() {
            return user;
        }
    }
}
