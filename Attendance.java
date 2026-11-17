package elms.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Attendance implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String employeeId;
    private final LocalDate date;
    private String status;
    private final String recordedBy;

    public Attendance(String employeeId, LocalDate date, String status, String recordedBy) {
        this.employeeId = employeeId;
        this.date = date;
        this.status = status;
        this.recordedBy = recordedBy;
    }
    public String getEmployeeId() { return employeeId; }
    public LocalDate getDate() { return date; }
    public String getStatus() { return status; }
    public String getRecordedBy() { return recordedBy; }
    public void setStatus(String status) { this.status = status; }
}
