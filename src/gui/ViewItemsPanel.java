package gui;

import controller.LibraryManager;
import model.LibraryItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel for viewing all library items in a table.
 */
public class ViewItemsPanel extends JPanel {
    private final LibraryManager manager;
    private final Runnable statusUpdater;
    private JTable itemsTable;
    private DefaultTableModel tableModel;

    public ViewItemsPanel(LibraryManager manager, Runnable statusUpdater) {
        this.manager = manager;
        this.statusUpdater = statusUpdater;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        refreshTable();
    }

    private void buildUI() {
        String[] columns = {"ID", "Type", "Title", "Author", "Year", "Status", "Borrows"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable = new JTable(tableModel);
        itemsTable.setRowHeight(28);
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemsTable.setDefaultRenderer(Object.class, new LibraryTableRenderer());
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Library Catalogue"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setMnemonic('R');
        refreshBtn.setToolTipText("Refresh the items list (Alt+R)");
        refreshBtn.addActionListener(e -> refreshTable());

        JButton detailsBtn = new JButton("View Details");
        detailsBtn.setToolTipText("Show detailed information about the selected item");
        detailsBtn.addActionListener(e -> showItemDetails());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(detailsBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (LibraryItem item : manager.getDatabase().getItems()) {
            String status;
            if (item.isOverdue()) {
                status = "Overdue";
            } else if (item.isAvailable()) {
                status = "Available";
            } else {
                status = "Borrowed";
            }
            tableModel.addRow(new Object[]{
                    item.getId(), item.getItemType(), item.getTitle(),
                    item.getAuthor(), item.getYear(), status, item.getBorrowCount()
            });
        }
        if (statusUpdater != null) {
            statusUpdater.run();
        }
    }

    private void showItemDetails() {
        int row = itemsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        LibraryItem item = manager.getDatabase().findItemById(id);
        if (item != null) {
            JTextArea area = new JTextArea(manager.processItem(item));
            area.setEditable(false);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(450, 250));
            JOptionPane.showMessageDialog(this, scroll, "Item Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public String getSelectedItemId() {
        int row = itemsTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (String) tableModel.getValueAt(row, 0);
    }
}
