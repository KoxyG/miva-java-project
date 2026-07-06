package gui;

import controller.LibraryManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Panel for generating reports and managing file import/export.
 */
public class ReportsPanel extends JPanel {
    private final LibraryManager manager;
    private final Runnable statusUpdater;

    private JTextArea reportArea;

    public ReportsPanel(LibraryManager manager, Runnable statusUpdater) {
        this.manager = manager;
        this.statusUpdater = statusUpdater;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton mostBorrowedBtn = new JButton("Most Borrowed Items");
        mostBorrowedBtn.addActionListener(e -> showReport(manager.generateMostBorrowedReport()));

        JButton overdueBtn = new JButton("Overdue Users");
        overdueBtn.addActionListener(e -> showReport(manager.generateOverdueUsersReport()));

        JButton categoryBtn = new JButton("Category Distribution");
        categoryBtn.addActionListener(e -> showReport(manager.generateCategoryDistributionReport()));

        JButton cacheBtn = new JButton("Frequent Access Cache");
        cacheBtn.addActionListener(e -> showReport(manager.getItemCache().getCacheReport()));

        JButton saveBtn = new JButton("Save Data...");
        saveBtn.setToolTipText("Export library data to JSON files");
        saveBtn.addActionListener(e -> saveData());

        JButton loadBtn = new JButton("Load Data...");
        loadBtn.setToolTipText("Import library data from JSON files");
        loadBtn.addActionListener(e -> loadData());

        btnPanel.add(mostBorrowedBtn);
        btnPanel.add(overdueBtn);
        btnPanel.add(categoryBtn);
        btnPanel.add(cacheBtn);
        btnPanel.add(Box.createHorizontalStrut(20));
        btnPanel.add(saveBtn);
        btnPanel.add(loadBtn);

        reportArea = new JTextArea(20, 60);
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(reportArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Reports"));

        add(btnPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void showReport(String report) {
        reportArea.setText(report);
    }

    private void saveData() {
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir"), "data"));
        chooser.setDialogTitle("Select folder to save data");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File dir = chooser.getSelectedFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                manager.saveData(new File(dir, "items.json"), new File(dir, "users.json"));
                JOptionPane.showMessageDialog(this, "Data saved to " + dir.getAbsolutePath(),
                        "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadData() {
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir"), "data"));
        chooser.setDialogTitle("Select folder containing items.json and users.json");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File dir = chooser.getSelectedFile();
                manager.loadData(new File(dir, "items.json"), new File(dir, "users.json"));
                showReport("Data loaded from " + dir.getAbsolutePath() + "\n\n"
                        + manager.generateCategoryDistributionReport());
                if (statusUpdater != null) {
                    statusUpdater.run();
                }
                JOptionPane.showMessageDialog(this, "Data loaded successfully.",
                        "Load Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
