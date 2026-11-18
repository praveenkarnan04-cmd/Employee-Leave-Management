package elms.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public final class UI {
    private UI() {}
    public static JPanel panel(Color color) {
        JPanel p = new JPanel();
        p.setBackground(color);
        return p;
    }
    public static JLabel label(String text, int size, boolean bold, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(size, bold));
        l.setForeground(color);
        return l;
    }
    public static JButton button(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFont(Theme.font(13, true));
        b.setForeground(primary ? Color.WHITE : Theme.NAVY);
        b.setBackground(primary ? Theme.BLUE : Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(new LineBorder(primary ? Theme.BLUE : Theme.BORDER, 1, true),
                new EmptyBorder(10, 15, 10, 15)));
        return b;
    }
    public static JTextField field(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(Theme.font(14, false));
        f.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(9, 10, 9, 10)));
        return f;
    }
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        return p;
    }
    public static void message(Component parent, String text, int type) {
        JOptionPane.showMessageDialog(parent, text, "ELMS", type);
    }
}
