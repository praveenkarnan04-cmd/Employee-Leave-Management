package elms.interfaces;

import elms.model.Employee;
import elms.model.LeaveApplication;

public interface LeaveOperations {
    LeaveApplication applyForLeave(Employee employee, String leaveType,
                                   String startDate, String endDate,
                                   String reason, String documentPath);
    void cancelApplication(Employee employee, String reference);
}
