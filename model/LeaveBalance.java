package elms.model;

import java.io.Serializable;

public class LeaveBalance implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String employeeId;
    private final String leaveType;
    private final int year;
    private double entitlement;
    private double used;

    public LeaveBalance(String employeeId, String leaveType, int year, double entitlement) {
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.year = year;
        this.entitlement = entitlement;
        this.used = 0;
    }
    public String getEmployeeId() { return employeeId; }
    public String getLeaveType() { return leaveType; }
    public int getYear() { return year; }
    public double getEntitlement() { return entitlement; }
    public double getUsed() { return used; }
    public double getRemaining() { return entitlement - used; }
    public void deduct(double days) {
        if (days <= 0 || days > getRemaining())
            throw new IllegalArgumentException("Insufficient leave balance.");
        used += days;
    }
    public void setEntitlement(double entitlement) {
        if (entitlement < used) throw new IllegalArgumentException("Entitlement cannot be below used leave.");
        this.entitlement = entitlement;
    }
}
