package elms.model;

import java.io.Serializable;

public class LeaveType implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private double defaultEntitlement;
    private boolean active = true;

    public LeaveType(String name, double defaultEntitlement) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Leave type name is required.");
        if (defaultEntitlement < 0)
            throw new IllegalArgumentException("Entitlement cannot be negative.");
        this.name = name.trim();
        this.defaultEntitlement = defaultEntitlement;
    }

    public String getName() { return name; }
    public double getDefaultEntitlement() { return defaultEntitlement; }
    public void setDefaultEntitlement(double value) {
        if (value < 0) throw new IllegalArgumentException("Entitlement cannot be negative.");
        defaultEntitlement = value;
    }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
