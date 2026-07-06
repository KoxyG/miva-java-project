package gui;

import controller.LibraryManager;
import model.LibraryItem;
import utils.SortAlgorithms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel for searching and sorting library items.
 */
public class SearchSortPanel extends JPanel {
    private final LibraryManager manager;
    private final Runnable statusUpdater;

    private JTextField searchField;
    private JComboBox<String> searchTypeCombo;
    private JComboBox<String> sortFieldCombo;
    private JComboBox<String> sortAlgoCombo;
    private JTable resultsTable;
    private DefaultTableModel resultsModel;
    private JTextArea searchInfoArea;

    public SearchSortPanel(LibraryManager manager, Runnable statusUpdater) {
        this.manager = manager;
        this.statusUpdater = statusUpdater;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(new JLabel("Search:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        searchField = new JTextField(20);
        searchField.setToolTipText("Enter search query");
        topPanel.add(searchField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        searchTypeCombo = new JComboBox<>(new String[]{
                "Title (Linear)", "Title (Binary)", "Title (Recursive)", "Author", "Type"
        });
        searchTypeCombo.setToolTipText("Choose search algorithm/type");
        topPanel.add(searchTypeCombo, gbc);

        gbc.gridx = 3;
        JButton searchBtn = new JButton("Search");
        searchBtn.setMnemonic('S');
        searchBtn.addActionListener(e -> performSearch());
        topPanel.add(searchBtn, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        topPanel.add(new JLabel("Sort by:"), gbc);
        gbc.gridx = 1;
        sortFieldCombo = new JComboBox<>(new String[]{"Title", "Author", "Year"});
        topPanel.add(sortFieldCombo, gbc);

        gbc.gridx = 2;
        topPanel.add(new JLabel("Algorithm:"), gbc);
        gbc.gridx = 3;
        sortAlgoCombo = new JComboBox<>(new String[]{
                "Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort"
        });
        sortAlgoCombo.setToolTipText("Select sorting algorithm to apply");
        topPanel.add(sortAlgoCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton sortBtn = new JButton("Sort All Items");
        sortBtn.addActionListener(e -> performSort());
        topPanel.add(sortBtn, gbc);

        searchInfoArea = new JTextArea(3, 40);
        searchInfoArea.setEditable(false);
        searchInfoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        JScrollPane infoScroll = new JScrollPane(searchInfoArea);
        infoScroll.setBorder(BorderFactory.createTitledBorder("Search Info"));

        String[] columns = {"ID", "Type", "Title", "Author", "Year", "Status"};
        resultsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(resultsModel);
        resultsTable.setDefaultRenderer(Object.class, new LibraryTableRenderer());
        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Search Results"));

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(infoScroll, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a search query.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String searchType = (String) searchTypeCombo.getSelectedItem();
        if (searchType.contains("Binary") && !manager.isSortedByTitle()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Binary search works best on title-sorted data.\nSort by title first?",
                    "Not Sorted", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                manager.sortItems(SortAlgorithms.SortField.TITLE, SortAlgorithms.Algorithm.MERGE_SORT);
            }
        }

        List<LibraryItem> results = manager.search(query, searchType);
        displayResults(results);

        searchInfoArea.setText(String.format("Search: '%s' | Type: %s | Results: %d\n%s",
                query, searchType, results.size(),
                searchType.contains("Binary") ? "Note: Binary search used (title-sorted list)." :
                        searchType.contains("Recursive") ? "Note: Recursive search algorithm used." :
                                "Note: Linear search algorithm used."));
    }

    private void performSort() {
        SortAlgorithms.SortField field = switch ((String) sortFieldCombo.getSelectedItem()) {
            case "Author" -> SortAlgorithms.SortField.AUTHOR;
            case "Year" -> SortAlgorithms.SortField.YEAR;
            default -> SortAlgorithms.SortField.TITLE;
        };

        SortAlgorithms.Algorithm algo = switch ((String) sortAlgoCombo.getSelectedItem()) {
            case "Insertion Sort" -> SortAlgorithms.Algorithm.INSERTION_SORT;
            case "Merge Sort" -> SortAlgorithms.Algorithm.MERGE_SORT;
            case "Quick Sort" -> SortAlgorithms.Algorithm.QUICK_SORT;
            default -> SortAlgorithms.Algorithm.SELECTION_SORT;
        };

        manager.sortItems(field, algo);
        displayResults(manager.getDatabase().getItems());

        searchInfoArea.setText(String.format("Sorted %d items by %s using %s.",
                manager.getDatabase().getItems().size(),
                sortFieldCombo.getSelectedItem(), sortAlgoCombo.getSelectedItem()));

        if (statusUpdater != null) {
            statusUpdater.run();
        }
    }

    private void displayResults(List<LibraryItem> results) {
        resultsModel.setRowCount(0);
        for (LibraryItem item : results) {
            String status = item.isOverdue() ? "Overdue" :
                    item.isAvailable() ? "Available" : "Borrowed";
            resultsModel.addRow(new Object[]{
                    item.getId(), item.getItemType(), item.getTitle(),
                    item.getAuthor(), item.getYear(), status
            });
        }
    }
}
