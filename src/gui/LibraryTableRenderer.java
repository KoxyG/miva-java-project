package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom renderer highlighting availability and overdue status in the items table.
 */
public class LibraryTableRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (!isSelected) {
            String status = (String) table.getValueAt(row, 5);
            if ("Overdue".equals(status)) {
                c.setBackground(new Color(255, 220, 220));
                c.setForeground(Color.DARK_GRAY);
            } else if ("Available".equals(status)) {
                c.setBackground(new Color(220, 255, 220));
                c.setForeground(Color.DARK_GRAY);
            } else if ("Borrowed".equals(status)) {
                c.setBackground(new Color(255, 255, 210));
                c.setForeground(Color.DARK_GRAY);
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }
        }

        setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        return c;
    }
}
