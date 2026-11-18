package elms.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveApplication implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Status { PENDING, APPROVED, REJECTED, CANCELLED }

    private final String reference;
    private final String employeeId;
    private final String leaveType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String reason;
    private final String documentPath;
    private final LocalDateTime submittedAt;
    private Status status;
    private String decisionBy = "";
    private String decisionReason = "";
    private LocalDateTime decidedAt;
    private double chargeableDays;
    private boolean balanceDeducted;

    public LeaveApplication(String reference, String employeeId, String leaveType,
            LocalDate startDate, LocalDate endDate, String reason, String documentPath,
            double chargeableDays) {
        this.reference = reference;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.documentPath = documentPath == null ? "" : documentPath;
        this.chargeableDays = chargeableDays;
        this.submittedAt = LocalDateTime.now();
        this.status = Status.PENDING;
    }
    public String getReference() { return reference; }
    public String getEmployeeId() { return employeeId; }
    public String getLeaveType() { return leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getReason() { return reason; }
    public String getDocumentPath() { return documentPath; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public Status getStatus() { return status; }
    public String getDecisionBy() { return decisionBy; }
    public String getDecisionReason() { return decisionReason; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public double getChargeableDays() { return chargeableDays; }
    public boolean isBalanceDeducted() { return balanceDeducted; }
    public void decide(Status status, String approver, String reason) {
        this.status = status;
        this.decisionBy = approver;
        this.decisionReason = reason == null ? "" : reason;
        this.decidedAt = LocalDateTime.now();
    }
    public void setBalanceDeducted(boolean value) { balanceDeducted = value; }
}
