package gui;

import controller.LibraryManager;
import model.LibraryItem;
import model.UserAccount;
import utils.IDGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin panel with dynamic form fields for adding items and users, plus undo support.
 */
public class AdminPanel extends JPanel {
    private final LibraryManager manager;
    private final ViewItemsPanel viewItemsPanel;
    private final BorrowPanel borrowPanel;
    private final Runnable statusUpdater;

    private CardLayout cardLayout;
    private JPanel formCardPanel;
    private JComboBox<String> itemTypeCombo;
    private final List<JComponent> dynamicFields = new ArrayList<>();

    private JTextField titleField;
    private JTextField authorField;
    private JTextField yearField;
    private JPanel dynamicFieldPanel;

    private JTextField userNameField;
    private JTextField userEmailField;

    public AdminPanel(LibraryManager manager, ViewItemsPanel viewItemsPanel,
                      BorrowPanel borrowPanel, Runnable statusUpdater) {
        this.manager = manager;
        this.viewItemsPanel = viewItemsPanel;
        this.borrowPanel = borrowPanel;
        this.statusUpdater = statusUpdater;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
        cardLayout = new CardLayout();
        formCardPanel = new JPanel(cardLayout);

        formCardPanel.add(buildAddItemForm(), "addItem");
        formCardPanel.add(buildAddUserForm(), "addUser");

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addItemNav = new JButton("Add Item");
        addItemNav.addActionListener(e -> cardLayout.show(formCardPanel, "addItem"));
        JButton addUserNav = new JButton("Add User");
        addUserNav.addActionListener(e -> cardLayout.show(formCardPanel, "addUser"));
        navPanel.add(addItemNav);
        navPanel.add(addUserNav);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton undoBtn = new JButton("Undo Last Action");
        undoBtn.setMnemonic('U');
        undoBtn.setToolTipText("Undo the last admin add/delete operation (Alt+U)");
        undoBtn.addActionListener(e -> undoLastAction());

        JButton deleteItemBtn = new JButton("Delete Selected Item");
        deleteItemBtn.setToolTipText("Delete the item selected in View Items tab");
        deleteItemBtn.addActionListener(e -> deleteSelectedItem());

        actionPanel.add(undoBtn);
        actionPanel.add(deleteItemBtn);

        add(navPanel, BorderLayout.NORTH);
        add(formCardPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private JPanel buildAddItemForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New Library Item"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        itemTypeCombo = new JComboBox<>(new String[]{"Book", "Magazine", "Journal"});
        itemTypeCombo.setToolTipText("Select the type of library item");
        itemTypeCombo.addActionListener(e -> updateDynamicFields());
        panel.add(itemTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        titleField = new JTextField(20);
        titleField.setToolTipText("Enter the item title");
        panel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        titleField = titleField;
        authorField = new JTextField(20);
        authorField.setToolTipText("Enter the author name");
        panel.add(authorField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        yearField = new JTextField(10);
        yearField.setToolTipText("Publication year (e.g. 2024)");
        panel.add(yearField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        dynamicFieldPanel = new JPanel(new GridBagLayout());
        dynamicFieldPanel.setBorder(BorderFactory.createTitledBorder("Type-Specific Fields"));
        panel.add(dynamicFieldPanel, gbc);

        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton addBtn = new JButton("Add Item");
        addBtn.setMnemonic('A');
        addBtn.addActionListener(e -> addItem());
        panel.add(addBtn, gbc);

        updateDynamicFields();
        return panel;
    }

    private void updateDynamicFields() {
        dynamicFieldPanel.removeAll();
        dynamicFields.clear();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String type = (String) itemTypeCombo.getSelectedItem();
        gbc.gridx = 0;
        gbc.gridy = 0;

        switch (type) {
            case "Book" -> {
                dynamicFieldPanel.add(new JLabel("ISBN:"), gbc);
                gbc.gridx = 1;
                JTextField isbn = new JTextField(15);
                dynamicFields.add(isbn);
                dynamicFieldPanel.add(isbn, gbc);
                gbc.gridx = 0;
                gbc.gridy = 1;
                dynamicFieldPanel.add(new JLabel("Pages:"), gbc);
                gbc.gridx = 1;
                JTextField pages = new JTextField(8);
                dynamicFields.add(pages);
                dynamicFieldPanel.add(pages, gbc);
            }
            case "Magazine" -> {
                dynamicFieldPanel.add(new JLabel("Issue Number:"), gbc);
                gbc.gridx = 1;
                JTextField issue = new JTextField(8);
                dynamicFields.add(issue);
                dynamicFieldPanel.add(issue, gbc);
                gbc.gridx = 0;
                gbc.gridy = 1;
                dynamicFieldPanel.add(new JLabel("Publisher:"), gbc);
                gbc.gridx = 1;
                JTextField publisher = new JTextField(15);
                dynamicFields.add(publisher);
                dynamicFieldPanel.add(publisher, gbc);
            }
            case "Journal" -> {
                dynamicFieldPanel.add(new JLabel("Volume:"), gbc);
                gbc.gridx = 1;
                JTextField volume = new JTextField(10);
                dynamicFields.add(volume);
                dynamicFieldPanel.add(volume, gbc);
                gbc.gridx = 0;
                gbc.gridy = 1;
                dynamicFieldPanel.add(new JLabel("Field:"), gbc);
                gbc.gridx = 1;
                JTextField field = new JTextField(15);
                dynamicFields.add(field);
                dynamicFieldPanel.add(field, gbc);
            }
        }
        dynamicFieldPanel.revalidate();
        dynamicFieldPanel.repaint();
    }

    private JPanel buildAddUserForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New User"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        userNameField = new JTextField(20);
        panel.add(userNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        userEmailField = new JTextField(20);
        panel.add(userEmailField, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JButton addBtn = new JButton("Add User");
        addBtn.addActionListener(e -> addUser());
        panel.add(addBtn, gbc);

        return panel;
    }

    private void addItem() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String yearStr = yearField.getText().trim();
            String type = (String) itemTypeCombo.getSelectedItem();

            if (title.isEmpty() || author.isEmpty() || yearStr.isEmpty()) {
                showValidationError("Title, author, and year are required.");
                return;
            }

            int year = Integer.parseInt(yearStr);
            if (year < 1000 || year > 2100) {
                showValidationError("Please enter a valid year (1000-2100).");
                return;
            }

            String extra1 = ((JTextField) dynamicFields.get(0)).getText().trim();
            String extra2 = ((JTextField) dynamicFields.get(1)).getText().trim();

            if (extra1.isEmpty() || extra2.isEmpty()) {
                showValidationError("All type-specific fields are required.");
                return;
            }

            LibraryItem item = manager.createItem(type, title, author, year, extra1, extra2);
            manager.addItem(item);

            JOptionPane.showMessageDialog(this,
                    "Item added: " + item.getId(), "Success", JOptionPane.INFORMATION_MESSAGE);

            titleField.setText("");
            authorField.setText("");
            yearField.setText("");
            for (JComponent field : dynamicFields) {
                ((JTextField) field).setText("");
            }
            refreshAll();
        } catch (NumberFormatException ex) {
            showValidationError("Invalid number format. Check year and numeric fields.");
        } catch (Exception ex) {
            showValidationError("Error adding item: " + ex.getMessage());
        }
    }

    private void addUser() {
        try {
            String name = userNameField.getText().trim();
            String email = userEmailField.getText().trim();

            if (name.isEmpty() || email.isEmpty()) {
                showValidationError("Name and email are required.");
                return;
            }
            if (!email.contains("@")) {
                showValidationError("Please enter a valid email address.");
                return;
            }

            UserAccount user = new UserAccount(IDGenerator.generateUserId(), name, email);
            manager.addUser(user);

            JOptionPane.showMessageDialog(this,
                    "User added: " + user.getUserId(), "Success", JOptionPane.INFORMATION_MESSAGE);

            userNameField.setText("");
            userEmailField.setText("");
            refreshAll();
        } catch (Exception ex) {
            showValidationError("Error adding user: " + ex.getMessage());
        }
    }

    private void deleteSelectedItem() {
        String itemId = viewItemsPanel.getSelectedItemId();
        if (itemId == null) {
            showValidationError("Please select an item in the View Items tab first.");
            return;
        }

        LibraryItem item = manager.getDatabase().findItemById(itemId);
        if (item == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete '" + item.getTitle() + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.deleteItem(item);
            refreshAll();
            JOptionPane.showMessageDialog(this, "Item deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void undoLastAction() {
        if (manager.undoLastAction()) {
            refreshAll();
            JOptionPane.showMessageDialog(this, "Last action undone.", "Undo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showValidationError("Nothing to undo.");
        }
    }

    private void refreshAll() {
        viewItemsPanel.refreshTable();
        borrowPanel.refreshCombos();
        if (statusUpdater != null) {
            statusUpdater.run();
        }
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}
