package gui;

import controller.LibraryManager;
import model.LibraryItem;
import model.UserAccount;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for borrowing and returning library items.
 */
public class BorrowPanel extends JPanel {
    private final LibraryManager manager;
    private final ViewItemsPanel viewItemsPanel;
    private final Runnable statusUpdater;

    private JComboBox<UserAccount> userCombo;
    private JComboBox<LibraryItem> itemCombo;
    private JTextArea queueArea;

    public BorrowPanel(LibraryManager manager, ViewItemsPanel viewItemsPanel, Runnable statusUpdater) {
        this.manager = manager;
        this.viewItemsPanel = viewItemsPanel;
        this.statusUpdater = statusUpdater;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        refreshCombos();
    }

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Select User:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        userCombo = new JComboBox<>();
        userCombo.setToolTipText("Choose the user borrowing or returning an item");
        add(userCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        add(new JLabel("Select Item:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        itemCombo = new JComboBox<>();
        itemCombo.setToolTipText("Choose the library item");
        add(itemCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        JButton borrowBtn = new JButton("Borrow Item");
        borrowBtn.setMnemonic('B');
        borrowBtn.setToolTipText("Borrow selected item for 14 days (Alt+B)");
        borrowBtn.addActionListener(e -> borrowItem());

        JButton returnBtn = new JButton("Return Item");
        returnBtn.setMnemonic('T');
        returnBtn.setToolTipText("Return the selected item (Alt+T)");
        returnBtn.addActionListener(e -> returnItem());

        JButton reserveBtn = new JButton("Reserve (Waitlist)");
        reserveBtn.setToolTipText("Join the waitlist for a borrowed item");
        reserveBtn.addActionListener(e -> reserveItem());

        btnPanel.add(borrowBtn);
        btnPanel.add(returnBtn);
        btnPanel.add(reserveBtn);
        add(btnPanel, gbc);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        queueArea = new JTextArea(8, 40);
        queueArea.setEditable(false);
        queueArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane queueScroll = new JScrollPane(queueArea);
        queueScroll.setBorder(BorderFactory.createTitledBorder("Reservation Queue"));
        add(queueScroll, gbc);
    }

    public void refreshCombos() {
        userCombo.removeAllItems();
        for (UserAccount user : manager.getDatabase().getUsers()) {
            userCombo.addItem(user);
        }

        itemCombo.removeAllItems();
        for (LibraryItem item : manager.getDatabase().getItems()) {
            itemCombo.addItem(item);
        }

        queueArea.setText(manager.getBorrowController().getReservationQueueStatus());
    }

    private void borrowItem() {
        UserAccount user = (UserAccount) userCombo.getSelectedItem();
        LibraryItem item = (LibraryItem) itemCombo.getSelectedItem();

        if (user == null || item == null) {
            showValidationError("Please select both a user and an item.");
            return;
        }

        try {
            if (manager.getBorrowController().borrowItem(item.getId(), user.getUserId())) {
                JOptionPane.showMessageDialog(this,
                        String.format("'%s' borrowed by %s.\nDue: %s",
                                item.getTitle(), user.getName(), item.getDueDate()),
                        "Borrow Successful", JOptionPane.INFORMATION_MESSAGE);
            } else if (!item.isAvailable()) {
                manager.getBorrowController().reserveItem(item.getId(), user.getUserId());
                JOptionPane.showMessageDialog(this,
                        "Item is not available. You have been added to the waitlist.",
                        "Added to Waitlist", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showValidationError("Could not borrow item. Check user and item IDs.");
            }
        } catch (Exception ex) {
            showValidationError("Error borrowing item: " + ex.getMessage());
        }

        refreshAll();
    }

    private void returnItem() {
        LibraryItem item = (LibraryItem) itemCombo.getSelectedItem();
        if (item == null) {
            showValidationError("Please select an item to return.");
            return;
        }

        if (item.isAvailable()) {
            showValidationError("This item is not currently borrowed.");
            return;
        }

        try {
            if (manager.getBorrowController().returnItem(item.getId())) {
                if (item.isOverdue()) {
                    double charge = manager.computeOverdueCharge(item, java.time.LocalDate.now(), 0.50);
                    JOptionPane.showMessageDialog(this,
                            String.format("Item returned.\nOverdue charge: $%.2f", charge),
                            "Return Complete", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Item returned successfully.",
                            "Return Complete", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                showValidationError("Could not return item.");
            }
        } catch (Exception ex) {
            showValidationError("Error returning item: " + ex.getMessage());
        }

        refreshAll();
    }

    private void reserveItem() {
        UserAccount user = (UserAccount) userCombo.getSelectedItem();
        LibraryItem item = (LibraryItem) itemCombo.getSelectedItem();

        if (user == null || item == null) {
            showValidationError("Please select both a user and an item.");
            return;
        }

        if (manager.getBorrowController().reserveItem(item.getId(), user.getUserId())) {
            JOptionPane.showMessageDialog(this, "Added to waitlist successfully.",
                    "Reservation", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showValidationError("Could not reserve. Item may not exist or already on waitlist.");
        }
        refreshAll();
    }

    private void refreshAll() {
        refreshCombos();
        viewItemsPanel.refreshTable();
        if (statusUpdater != null) {
            statusUpdater.run();
        }
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}
