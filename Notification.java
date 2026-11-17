package elms.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String recipientId;
    private final String message;
    private final LocalDateTime createdAt;
    private boolean read;

    public Notification(String recipientId, String message) {
        this.recipientId = recipientId == null ? "" : recipientId;
        this.message = message == null ? "" : message;
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    public String getRecipientId() { return recipientId; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }
    public void markRead() { read = true; }
}
