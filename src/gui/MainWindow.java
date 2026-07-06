package gui;

import controller.LibraryManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Main application window with tabbed interface and status bar.
 */
public class MainWindow extends JFrame {
    private final LibraryManager manager;
    private JLabel statusLabel;
    private ViewItemsPanel viewItemsPanel;
    private BorrowPanel borrowPanel;
    private AdminPanel adminPanel;
    private SearchSortPanel searchSortPanel;
    private ReportsPanel reportsPanel;
    private Timer overdueTimer;

    public MainWindow() {
        manager = new LibraryManager();
        initializeUI();
        setupMenuBar();
        setupKeyboardShortcuts();
        startOverdueTimer();
    }

    private void initializeUI() {
        setTitle("Smart Library Circulation & Automation System (SLCAS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);

        statusLabel = new JLabel(" Ready");
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        Runnable statusUpdater = this::updateStatusBar;

        viewItemsPanel = new ViewItemsPanel(manager, statusUpdater);
        borrowPanel = new BorrowPanel(manager, viewItemsPanel, statusUpdater);
        adminPanel = new AdminPanel(manager, viewItemsPanel, borrowPanel, statusUpdater);
        searchSortPanel = new SearchSortPanel(manager, statusUpdater);
        reportsPanel = new ReportsPanel(manager, () -> {
            updateStatusBar();
            viewItemsPanel.refreshTable();
            borrowPanel.refreshCombos();
        });

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setToolTipText("Navigate between library modules");
        tabbedPane.addTab("View Items", null, viewItemsPanel, "Browse all library items");
        tabbedPane.addTab("Borrow/Return", null, borrowPanel, "Borrow, return, or reserve items");
        tabbedPane.addTab("Admin", null, adminPanel, "Add/delete items and users");
        tabbedPane.addTab("Search & Sort", null, searchSortPanel, "Search and sort the catalogue");
        tabbedPane.addTab("Reports", null, reportsPanel, "Generate reports and save/load data");

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == borrowPanel) {
                borrowPanel.refreshCombos();
            }
            updateStatusBar();
        });

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        updateStatusBar();
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        JMenuItem aboutItem = new JMenuItem("About SLCAS");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Smart Library Circulation & Automation System\n" +
                        "MIVA Open University Project\n\n" +
                        "Features: OOP, Data Structures, Sorting/Searching,\n" +
                        "Recursion, Event-Driven GUI, File Persistence",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void setupKeyboardShortcuts() {
        JPanel content = (JPanel) getContentPane();
        InputMap im = content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = content.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refresh");
        am.put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                viewItemsPanel.refreshTable();
                borrowPanel.refreshCombos();
                updateStatusBar();
            }
        });
    }

    private void startOverdueTimer() {
        overdueTimer = new Timer(30000, e -> checkOverdueItems());
        overdueTimer.start();
    }

    private void checkOverdueItems() {
        List<String> notifications = manager.getOverdueNotifications();
        if (!notifications.isEmpty()) {
            statusLabel.setText(" ⚠ " + notifications.size() + " overdue item(s) - check Reports tab");
            statusLabel.setForeground(Color.RED);

            if (notifications.size() <= 3) {
                StringBuilder msg = new StringBuilder("Overdue Items Reminder:\n\n");
                for (String n : notifications) {
                    msg.append(n).append("\n");
                }
                Toolkit.getDefaultToolkit().beep();
            }
        } else {
            updateStatusBar();
        }
    }

    private void updateStatusBar() {
        int items = manager.getDatabase().getItems().size();
        int users = manager.getDatabase().getUsers().size();
        int borrowed = 0;
        for (var item : manager.getDatabase().getItems()) {
            if (!item.isAvailable()) {
                borrowed++;
            }
        }
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setText(String.format(
                " Items: %d | Users: %d | Borrowed: %d | Queue: %d | Undo Stack: %d ",
                items, users, borrowed,
                manager.getDatabase().getReservationQueue().size(),
                manager.getDatabase().getUndoStack().size()));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
