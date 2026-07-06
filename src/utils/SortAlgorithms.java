package utils;

import model.LibraryItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Student-implemented sorting algorithms for library items.
 * Each algorithm sorts in-place using the shared compare() helper.
 */
public class SortAlgorithms {

    public enum SortField { TITLE, AUTHOR, YEAR }
    public enum Algorithm { SELECTION_SORT, INSERTION_SORT, MERGE_SORT, QUICK_SORT }

    private SortAlgorithms() {
    }

    /** Dispatches to the chosen algorithm — called from the GUI sort dropdown. */
    public static void sort(List<LibraryItem> items, SortField field, Algorithm algorithm) {
        switch (algorithm) {
            case SELECTION_SORT -> selectionSort(items, field);
            case INSERTION_SORT -> insertionSort(items, field);
            case MERGE_SORT -> mergeSort(items, field);
            case QUICK_SORT -> quickSort(items, 0, items.size() - 1, field);
        }
    }

    /** O(n²) — repeatedly finds the minimum element and swaps it forward. */
    public static void selectionSort(List<LibraryItem> items, SortField field) {
        for (int i = 0; i < items.size() - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < items.size(); j++) {
                if (compare(items.get(j), items.get(minIdx), field) < 0) {
                    minIdx = j; // track index of smallest remaining element
                }
            }
            // Swap current position with the minimum found
            LibraryItem temp = items.get(i);
            items.set(i, items.get(minIdx));
            items.set(minIdx, temp);
        }
    }

    /** O(n²) best on nearly-sorted data — shifts larger elements right to insert key. */
    public static void insertionSort(List<LibraryItem> items, SortField field) {
        for (int i = 1; i < items.size(); i++) {
            LibraryItem key = items.get(i);
            int j = i - 1;
            while (j >= 0 && compare(items.get(j), key, field) > 0) {
                items.set(j + 1, items.get(j)); // shift element one position right
                j--;
            }
            items.set(j + 1, key); // insert key at its sorted position
        }
    }

    /** O(n log n) divide-and-conquer — splits, sorts halves, then merges. */
    public static void mergeSort(List<LibraryItem> items, SortField field) {
        if (items.size() <= 1) {
            return; // base case: 0 or 1 element is already sorted
        }
        int mid = items.size() / 2;
        List<LibraryItem> left = new ArrayList<>(items.subList(0, mid));
        List<LibraryItem> right = new ArrayList<>(items.subList(mid, items.size()));
        mergeSort(left, field);
        mergeSort(right, field);
        merge(items, left, right, field);
    }

    /** Merges two sorted sub-lists back into the original list. */
    private static void merge(List<LibraryItem> target, List<LibraryItem> left,
                            List<LibraryItem> right, SortField field) {
        int i = 0, j = 0, k = 0;
        while (i < left.size() && j < right.size()) {
            if (compare(left.get(i), right.get(j), field) <= 0) {
                target.set(k++, left.get(i++));
            } else {
                target.set(k++, right.get(j++));
            }
        }
        while (i < left.size()) {
            target.set(k++, left.get(i++));
        }
        while (j < right.size()) {
            target.set(k++, right.get(j++));
        }
    }

    /** O(n log n) average — partitions around a pivot then recurses on both sides. */
    public static void quickSort(List<LibraryItem> items, int low, int high, SortField field) {
        if (low < high) {
            int pivotIndex = partition(items, low, high, field);
            quickSort(items, low, pivotIndex - 1, field);
            quickSort(items, pivotIndex + 1, high, field);
        }
    }

    /** Places pivot in final position; elements left are ≤ pivot, right are > pivot. */
    private static int partition(List<LibraryItem> items, int low, int high, SortField field) {
        LibraryItem pivot = items.get(high); // choose last element as pivot
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (compare(items.get(j), pivot, field) <= 0) {
                i++;
                LibraryItem temp = items.get(i);
                items.set(i, items.get(j));
                items.set(j, temp);
            }
        }
        LibraryItem temp = items.get(i + 1);
        items.set(i + 1, items.get(high));
        items.set(high, temp);
        return i + 1; // pivot's final index
    }

    /** Shared comparator used by all four sorting algorithms. */
    private static int compare(LibraryItem a, LibraryItem b, SortField field) {
        return switch (field) {
            case TITLE -> a.getTitle().compareToIgnoreCase(b.getTitle());
            case AUTHOR -> a.getAuthor().compareToIgnoreCase(b.getAuthor());
            case YEAR -> Integer.compare(a.getYear(), b.getYear());
        };
    }

    public static boolean isSortedByTitle(List<LibraryItem> items) {
        for (int i = 1; i < items.size(); i++) {
            if (items.get(i - 1).getTitle().compareToIgnoreCase(items.get(i).getTitle()) > 0) {
                return false;
            }
        }
        return true;
    }
}
