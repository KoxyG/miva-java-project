import model.Book;
import model.LibraryItem;
import utils.SortAlgorithms;

import java.util.ArrayList;
import java.util.List;

/** Tests that each sorting algorithm produces correctly ordered results. */
public class SortAlgorithmsTest {

    private List<LibraryItem> items;

    public void runAll() {
        System.out.println("SortAlgorithmsTest");
        testSelectionSortByTitle();
        testInsertionSortByYear();
        testMergeSortByAuthor();
        testQuickSortByTitle();
    }

    private List<LibraryItem> freshItems() {
        List<LibraryItem> list = new ArrayList<>();
        list.add(new Book("1", "Zebra Book", "Zed", 2020, "a", 100));
        list.add(new Book("2", "Alpha Book", "Amy", 2010, "b", 200));
        list.add(new Book("3", "Middle Book", "Mike", 2015, "c", 300));
        return list;
    }

    private void testSelectionSortByTitle() {
        items = freshItems();
        SortAlgorithms.selectionSort(items, SortAlgorithms.SortField.TITLE);
        TestAssert.assertEquals("Alpha Book", items.get(0).getTitle(), "selection sort: first title");
        TestAssert.assertEquals("Zebra Book", items.get(2).getTitle(), "selection sort: last title");
        TestAssert.assertTrue(SortAlgorithms.isSortedByTitle(items), "selection sort leaves list sorted by title");
    }

    private void testInsertionSortByYear() {
        items = freshItems();
        SortAlgorithms.insertionSort(items, SortAlgorithms.SortField.YEAR);
        TestAssert.assertEquals(2010, items.get(0).getYear(), "insertion sort: earliest year first");
        TestAssert.assertEquals(2020, items.get(2).getYear(), "insertion sort: latest year last");
    }

    private void testMergeSortByAuthor() {
        items = freshItems();
        SortAlgorithms.mergeSort(items, SortAlgorithms.SortField.AUTHOR);
        TestAssert.assertEquals("Amy", items.get(0).getAuthor(), "merge sort: first author");
        TestAssert.assertEquals("Zed", items.get(2).getAuthor(), "merge sort: last author");
    }

    private void testQuickSortByTitle() {
        items = freshItems();
        SortAlgorithms.quickSort(items, 0, items.size() - 1, SortAlgorithms.SortField.TITLE);
        TestAssert.assertTrue(SortAlgorithms.isSortedByTitle(items), "quick sort leaves list sorted by title");
    }
}
