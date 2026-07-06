package controller;

import model.LibraryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Student-implemented search algorithms for the library catalogue.
 * Linear search scans every item; binary search requires a sorted list;
 * recursive search walks the list via divide-and-conquer style recursion.
 */
public class SearchEngine {

    /** Scans the entire list — O(n), works on unsorted data, supports partial matches. */
    public List<LibraryItem> linearSearchByTitle(List<LibraryItem> items, String query) {
        List<LibraryItem> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (LibraryItem item : items) {
            if (item.getTitle().toLowerCase().contains(lowerQuery)) {
                results.add(item);
            }
        }
        return results;
    }

    /** Same linear approach but matches against the author field instead of title. */
    public List<LibraryItem> linearSearchByAuthor(List<LibraryItem> items, String query) {
        List<LibraryItem> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (LibraryItem item : items) {
            if (item.getAuthor().toLowerCase().contains(lowerQuery)) {
                results.add(item);
            }
        }
        return results;
    }

    /** Filters items whose getItemType() matches (Book, Magazine, Journal). */
    public List<LibraryItem> searchByType(List<LibraryItem> items, String type) {
        List<LibraryItem> results = new ArrayList<>();
        for (LibraryItem item : items) {
            if (item.getItemType().equalsIgnoreCase(type)) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * Binary search for exact title match — O(log n), requires list sorted by title.
     * Falls back to linear partial-match scan when no exact hit is found.
     */
    public List<LibraryItem> binarySearchByTitle(List<LibraryItem> items, String query) {
        List<LibraryItem> results = new ArrayList<>();
        int left = 0;
        int right = items.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2; // avoids integer overflow
            int cmp = items.get(mid).getTitle().compareToIgnoreCase(query);

            if (cmp == 0) {
                results.add(items.get(mid));
                // Expand outward to collect duplicate titles at adjacent indices
                int lo = mid - 1;
                while (lo >= 0 && items.get(lo).getTitle().equalsIgnoreCase(query)) {
                    results.add(0, items.get(lo));
                    lo--;
                }
                int hi = mid + 1;
                while (hi < items.size() && items.get(hi).getTitle().equalsIgnoreCase(query)) {
                    results.add(items.get(hi));
                    hi++;
                }
                return results;
            } else if (cmp < 0) {
                left = mid + 1; // target is in the right half
            } else {
                right = mid - 1; // target is in the left half
            }
        }

        // No exact match — degrade gracefully to partial linear search
        for (LibraryItem item : items) {
            if (item.getTitle().toLowerCase().contains(query.toLowerCase())) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * Recursive search by title — base case is index >= size (empty result).
     * Processes the tail first, then prepends a match from the current index.
     */
    public List<LibraryItem> recursiveSearchByTitle(List<LibraryItem> items, String query, int index) {
        if (index >= items.size()) {
            return new ArrayList<>(); // base case: end of list
        }

        List<LibraryItem> results = recursiveSearchByTitle(items, query, index + 1);
        if (items.get(index).getTitle().toLowerCase().contains(query.toLowerCase())) {
            results.add(0, items.get(index)); // prepend to preserve original order
        }
        return results;
    }

    /** Same recursive pattern as title search, but matches on author field. */
    public List<LibraryItem> recursiveSearchByAuthor(List<LibraryItem> items, String query, int index) {
        if (index >= items.size()) {
            return new ArrayList<>();
        }

        List<LibraryItem> results = recursiveSearchByAuthor(items, query, index + 1);
        if (items.get(index).getAuthor().toLowerCase().contains(query.toLowerCase())) {
            results.add(0, items.get(index));
        }
        return results;
    }
}
