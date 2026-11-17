package elms.ui;

import elms.model.User;
import elms.service.ELMSService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final ELMSService service;
    private final JTextField username = UI.field(20);
    private final JPasswordField password = new JPasswordField(20);

    public LoginFrame(ELMSService service) {
        this.service = service;
        setTitle("ELMS | Sign in");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850, 540);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridLayout(1, 2));
        JPanel left = new JPanel();
        left.setBackground(Theme.NAVY);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(65, 42, 40, 35));
        JLabel brand = UI.label("ELMS", 36, true, Color.WHITE);
        JLabel tagline = UI.label("Employee Leave Management", 21, true, Color.WHITE);
        JLabel detail = UI.label("<html>One place to manage leave,<br>approvals and attendance.</html>", 15, false, new Color(203,213,225));
        left.add(brand); left.add(Box.createVerticalStrut(24));
        left.add(tagline); left.add(Box.createVerticalStrut(15)); left.add(detail);
        left.add(Box.createVerticalGlue());
        left.add(UI.label("SMART  •  SIMPLE  •  ORGANIZED", 11, true, Theme.SIDEBAR_MUTED));

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Theme.BG);
        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(30, 32, 30, 32)));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(UI.label("Welcome back", 25, true, Theme.TEXT));
        form.add(Box.createVerticalStrut(7));
        form.add(UI.label("Sign in to continue to your workspace.", 13, false, Theme.MUTED));
        form.add(Box.createVerticalStrut(25));
        form.add(UI.label("Username", 12, true, Theme.TEXT));
        form.add(Box.createVerticalStrut(7)); form.add(username);
        form.add(Box.createVerticalStrut(15));
        form.add(UI.label("Password", 12, true, Theme.TEXT));
        form.add(Box.createVerticalStrut(7));
        password.setFont(Theme.font(14, false));
        password.setBorder(username.getBorder());
        form.add(password);
        form.add(Box.createVerticalStrut(22));
        JButton signIn = Theme.button("Sign in", true);
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        signIn.addActionListener(e -> authenticate());
        form.add(signIn);
        form.add(Box.createVerticalStrut(18));
        form.add(UI.label("Demo: admin / admin123", 11, false, Theme.MUTED));
        form.add(UI.label("manager / manager123  •  employee / employee123", 11, false, Theme.MUTED));
        form.add(Box.createVerticalStrut(12));
        JButton reset = Theme.button("Reset demo data", false);
        reset.setAlignmentX(Component.LEFT_ALIGNMENT);
        reset.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "This replaces all saved records with original demo data. Continue?",
                    "Reset data", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                try { service.resetDemoData(); UI.message(this, "Demo data restored.", JOptionPane.INFORMATION_MESSAGE); }
                catch (RuntimeException ex) { UI.message(this, ex.getMessage(), JOptionPane.ERROR_MESSAGE); }
            }
        });
        form.add(reset);
        right.add(form);
        root.add(left); root.add(right);
        setContentPane(root);
        getRootPane().setDefaultButton(signIn);
    }

    private void authenticate() {
        User user = service.login(username.getText(), new String(password.getPassword()));
        if (user == null) {
            UI.message(this, "Invalid username or password, or the account is inactive.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new DashboardFrame(service, user).setVisible(true);
        dispose();
    }
}
