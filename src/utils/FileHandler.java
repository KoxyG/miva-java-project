package utils;

import model.Book;
import model.Journal;
import model.LibraryItem;
import model.Magazine;
import model.UserAccount;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence of library data to/from JSON-like text files.
 */
public class FileHandler {
    private FileHandler() {
    }

    public static void saveItems(List<LibraryItem> items, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("[\n");
            for (int i = 0; i < items.size(); i++) {
                writer.write(itemToJson(items.get(i)));
                if (i < items.size() - 1) {
                    writer.write(",\n");
                }
            }
            writer.write("\n]");
        }
    }

    public static List<LibraryItem> loadItems(File file) throws IOException {
        List<LibraryItem> items = new ArrayList<>();
        if (!file.exists()) {
            return items;
        }

        String content = readFile(file);
        content = content.trim();
        if (content.equals("[]") || content.isEmpty()) {
            return items;
        }

        content = content.substring(1, content.length() - 1).trim();
        if (content.isEmpty()) {
            return items;
        }

        String[] entries = splitJsonObjects(content);
        for (String entry : entries) {
            items.add(parseItem(entry.trim()));
        }
        return items;
    }

    public static void saveUsers(List<UserAccount> users, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("[\n");
            for (int i = 0; i < users.size(); i++) {
                writer.write(userToJson(users.get(i)));
                if (i < users.size() - 1) {
                    writer.write(",\n");
                }
            }
            writer.write("\n]");
        }
    }

    public static List<UserAccount> loadUsers(File file) throws IOException {
        List<UserAccount> users = new ArrayList<>();
        if (!file.exists()) {
            return users;
        }

        String content = readFile(file);
        content = content.trim();
        if (content.equals("[]") || content.isEmpty()) {
            return users;
        }

        content = content.substring(1, content.length() - 1).trim();
        if (content.isEmpty()) {
            return users;
        }

        String[] entries = splitJsonObjects(content);
        for (String entry : entries) {
            users.add(parseUser(entry.trim()));
        }
        return users;
    }

    private static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private static String[] splitJsonObjects(String content) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    objects.add(content.substring(start, i + 1));
                }
            }
        }
        return objects.toArray(new String[0]);
    }

    private static String itemToJson(LibraryItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"").append(item.getItemType()).append("\",");
        sb.append("\"id\":\"").append(escape(item.getId())).append("\",");
        sb.append("\"title\":\"").append(escape(item.getTitle())).append("\",");
        sb.append("\"author\":\"").append(escape(item.getAuthor())).append("\",");
        sb.append("\"year\":").append(item.getYear()).append(",");
        sb.append("\"available\":").append(item.isAvailable()).append(",");
        sb.append("\"borrowCount\":").append(item.getBorrowCount()).append(",");
        sb.append("\"borrower\":").append(item.getCurrentBorrowerId() == null ? "null"
                : "\"" + escape(item.getCurrentBorrowerId()) + "\"").append(",");
        sb.append("\"dueDate\":").append(item.getDueDate() == null ? "null"
                : "\"" + item.getDueDate() + "\"");

        if (item instanceof Book book) {
            sb.append(",\"isbn\":\"").append(escape(book.getIsbn())).append("\"");
            sb.append(",\"pages\":").append(book.getPages());
        } else if (item instanceof Magazine magazine) {
            sb.append(",\"issueNumber\":").append(magazine.getIssueNumber());
            sb.append(",\"publisher\":\"").append(escape(magazine.getPublisher())).append("\"");
        } else if (item instanceof Journal journal) {
            sb.append(",\"volume\":\"").append(escape(journal.getVolume())).append("\"");
            sb.append(",\"field\":\"").append(escape(journal.getField())).append("\"");
        }

        sb.append("}");
        return sb.toString();
    }

    private static LibraryItem parseItem(String json) {
        String type = getString(json, "type");
        String id = getString(json, "id");
        String title = getString(json, "title");
        String author = getString(json, "author");
        int year = getInt(json, "year");

        LibraryItem item;
        switch (type) {
            case "Book" -> item = new Book(id, title, author, year,
                    getString(json, "isbn"), getInt(json, "pages"));
            case "Magazine" -> item = new Magazine(id, title, author, year,
                    getInt(json, "issueNumber"), getString(json, "publisher"));
            case "Journal" -> item = new Journal(id, title, author, year,
                    getString(json, "volume"), getString(json, "field"));
            default -> throw new IllegalArgumentException("Unknown item type: " + type);
        }

        item.setAvailable(getBoolean(json, "available"));
        item.setBorrowCount(getInt(json, "borrowCount"));
        String borrower = getNullableString(json, "borrower");
        if (borrower != null) {
            item.setCurrentBorrowerId(borrower);
        }
        String dueDate = getNullableString(json, "dueDate");
        if (dueDate != null) {
            item.setDueDate(LocalDate.parse(dueDate));
        }
        return item;
    }

    private static String userToJson(UserAccount user) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"userId\":\"").append(escape(user.getUserId())).append("\",");
        sb.append("\"name\":\"").append(escape(user.getName())).append("\",");
        sb.append("\"email\":\"").append(escape(user.getEmail())).append("\",");
        sb.append("\"history\":[");
        List<UserAccount.BorrowRecord> history = user.getBorrowingHistory();
        for (int i = 0; i < history.size(); i++) {
            UserAccount.BorrowRecord r = history.get(i);
            sb.append("{");
            sb.append("\"itemId\":\"").append(escape(r.getItemId())).append("\",");
            sb.append("\"borrowDate\":\"").append(r.getBorrowDate()).append("\",");
            sb.append("\"dueDate\":\"").append(r.getDueDate()).append("\",");
            sb.append("\"returnDate\":").append(r.getReturnDate() == null ? "null"
                    : "\"" + r.getReturnDate() + "\"");
            sb.append("}");
            if (i < history.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private static UserAccount parseUser(String json) {
        UserAccount user = new UserAccount(
                getString(json, "userId"),
                getString(json, "name"),
                getString(json, "email"));

        String historyPart = extractArray(json, "history");
        if (historyPart != null && !historyPart.isEmpty()) {
            String[] records = splitJsonObjects(historyPart);
            for (String record : records) {
                String itemId = getString(record, "itemId");
                LocalDate borrowDate = LocalDate.parse(getString(record, "borrowDate"));
                LocalDate dueDate = LocalDate.parse(getString(record, "dueDate"));
                user.addBorrowRecord(itemId, borrowDate, dueDate);
                String returnDate = getNullableString(record, "returnDate");
                if (returnDate != null) {
                    user.completeBorrowRecord(itemId, LocalDate.parse(returnDate));
                }
            }
        }
        return user;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String getString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) {
            return "";
        }
        start += pattern.length();
        int end = start;
        while (end < json.length()) {
            if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') {
                break;
            }
            end++;
        }
        return json.substring(start, end).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String getNullableString(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) {
            return null;
        }
        start += pattern.length();
        if (json.substring(start).startsWith("null")) {
            return null;
        }
        return getString(json, key);
    }

    private static int getInt(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) {
            return 0;
        }
        start += pattern.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        return Integer.parseInt(json.substring(start, end));
    }

    private static boolean getBoolean(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) {
            return false;
        }
        start += pattern.length();
        return json.substring(start).startsWith("true");
    }

    private static String extractArray(String json, String key) {
        String pattern = "\"" + key + "\":[";
        int start = json.indexOf(pattern);
        if (start == -1) {
            return null;
        }
        start += pattern.length() - 1;
        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return json.substring(start + 1, i);
                }
            }
        }
        return null;
    }
}
