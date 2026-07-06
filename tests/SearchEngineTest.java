import controller.SearchEngine;
import model.Book;
import model.Journal;
import model.LibraryItem;
import model.Magazine;
import utils.SortAlgorithms;

import java.util.ArrayList;
import java.util.List;

/** Tests for linear, binary, and recursive search algorithms. */
public class SearchEngineTest {

    private final SearchEngine engine = new SearchEngine();
    private List<LibraryItem> items;

    public void runAll() {
        System.out.println("SearchEngineTest");
        setupItems();

        testLinearSearchByTitle();
        testLinearSearchByAuthor();
        testSearchByType();
        testBinarySearchExactMatch();
        testRecursiveSearchByTitle();
    }

    private void setupItems() {
        items = new ArrayList<>();
        items.add(new Book("BK-1", "Clean Code", "Robert Martin", 2008, "111", 400));
        items.add(new Book("BK-2", "Design Patterns", "Gamma", 1994, "222", 395));
        items.add(new Book("BK-3", "Introduction to Algorithms", "Cormen", 2022, "333", 1312));
        items.add(new Magazine("MG-1", "National Geographic", "Various", 2024, 1, "NatGeo"));
        items.add(new Journal("JN-1", "Nature", "Various", 2024, "Vol 1", "Science"));
    }

    private void testLinearSearchByTitle() {
        List<LibraryItem> results = engine.linearSearchByTitle(items, "code");
        TestAssert.assertEquals(1, results.size(), "linear search finds 'Clean Code' by partial title");
        TestAssert.assertEquals("Clean Code", results.get(0).getTitle(), "linear search returns correct title");
    }

    private void testLinearSearchByAuthor() {
        List<LibraryItem> results = engine.linearSearchByAuthor(items, "gamma");
        TestAssert.assertEquals(1, results.size(), "linear author search finds Gamma");
    }

    private void testSearchByType() {
        List<LibraryItem> books = engine.searchByType(items, "Book");
        TestAssert.assertEquals(3, books.size(), "type search returns all books");
        List<LibraryItem> journals = engine.searchByType(items, "Journal");
        TestAssert.assertEquals(1, journals.size(), "type search returns journal");
    }

    private void testBinarySearchExactMatch() {
        // Binary search requires title-sorted list
        SortAlgorithms.sort(items, SortAlgorithms.SortField.TITLE, SortAlgorithms.Algorithm.MERGE_SORT);
        List<LibraryItem> results = engine.binarySearchByTitle(items, "Design Patterns");
        TestAssert.assertEquals(1, results.size(), "binary search finds exact title match");
        TestAssert.assertEquals("Design Patterns", results.get(0).getTitle(), "binary search returns correct item");
    }

    private void testRecursiveSearchByTitle() {
        List<LibraryItem> results = engine.recursiveSearchByTitle(items, "algorithms", 0);
        TestAssert.assertEquals(1, results.size(), "recursive search finds 'Algorithms' in title");
    }
}
