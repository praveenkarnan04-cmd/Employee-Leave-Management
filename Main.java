package elms;

import elms.service.ELMSService;
import elms.ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // The application can still run with the default Swing look and feel.
        }
        SwingUtilities.invokeLater(() -> {
            ELMSService service = new ELMSService();
            new LoginFrame(service).setVisible(true);
        });
    }
}
