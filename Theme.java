package elms.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class Theme {
    private Theme() {}

    // Core palette (existing names kept so other classes still compile)
    public static final Color NAVY       = new Color(15, 23, 42);     // sidebar / brand panel
    public static final Color BLUE       = new Color(79, 70, 229);    // primary (indigo)
    public static final Color LIGHT_BLUE = new Color(238, 242, 255);  // table header / selection
    public static final Color BG         = new Color(244, 246, 251);  // page background
    public static final Color TEXT       = new Color(17, 24, 39);
    public static final Color MUTED      = new Color(100, 116, 139);
    public static final Color BORDER     = new Color(226, 232, 240);
    public static final Color WHITE      = Color.WHITE;

    // New: button / status colours
    public static final Color PRIMARY       = BLUE;
    public static final Color PRIMARY_HOVER = new Color(67, 56, 202);
    public static final Color SUCCESS       = new Color(22, 163, 74);
    public static final Color SUCCESS_HOVER = new Color(21, 128, 61);
    public static final Color DANGER        = new Color(220, 38, 38);
    public static final Color DANGER_HOVER  = new Color(185, 28, 28);
    public static final Color NAV_HOVER     = new Color(51, 65, 105);
    public static final Color SIDEBAR_MUTED = new Color(148, 163, 184);

    public enum Kind { PRIMARY, SUCCESS, DANGER, SECONDARY, NAV }

    public static Font font(int size, boolean bold) {
        return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
    }

    /** Drop-in replacement for UI.button(text, primary). */
    public static JButton button(String text, boolean primary) {
        return button(text, primary ? Kind.PRIMARY : Kind.SECONDARY);
    }

    public static JButton button(String text, Kind kind) {
        return new ThemedButton(text, kind);
    }

    private static final class ThemedButton extends JButton {
        private final Kind kind;
        private boolean hover;

        ThemedButton(String text, Kind kind) {
            super(text);
            this.kind = kind;
            setFont(font(13, true));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (kind == Kind.NAV) {
                setHorizontalAlignment(SwingConstants.LEFT);
                setMaximumSize(new Dimension(195, 42));
                setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 8));
            } else {
                setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            }
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), arc = 12;
            Color fg = Color.WHITE;
            boolean on = isEnabled();
            switch (kind) {
                case PRIMARY -> fill(g2, w, h, arc, hover ? PRIMARY_HOVER : PRIMARY);
                case SUCCESS -> fill(g2, w, h, arc, hover ? SUCCESS_HOVER : SUCCESS);
                case DANGER  -> fill(g2, w, h, arc, hover ? DANGER_HOVER : DANGER);
                case SECONDARY -> {
                    fill(g2, w, h, arc, hover ? LIGHT_BLUE : Color.WHITE);
                    g2.setColor(PRIMARY);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
                    fg = PRIMARY;
                }
                case NAV -> { if (hover) fill(g2, w, h, arc, NAV_HOVER); }
            }
            if (!on) { g2.setColor(new Color(255, 255, 255, 120)); g2.fillRoundRect(0, 0, w, h, arc, arc); }
            g2.dispose();
            setForeground(fg);
            super.paintComponent(g);
        }

        private void fill(Graphics2D g2, int w, int h, int arc, Color c) {
            g2.setColor(c);
            g2.fillRoundRect(0, 0, w, h, arc, arc);
        }
    }
}
