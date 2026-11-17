package elms.service;

import elms.model.*;
import java.io.Serializable;
import java.util.ArrayList;

public class ELMSData implements Serializable {
    private static final long serialVersionUID = 1L;
    public ArrayList<User> users = new ArrayList<>();
    public ArrayList<LeaveType> leaveTypes = new ArrayList<>();
    public ArrayList<LeaveApplication> applications = new ArrayList<>();
    public ArrayList<LeaveBalance> balances = new ArrayList<>();
    public ArrayList<Attendance> attendance = new ArrayList<>();
    public ArrayList<Notification> notifications = new ArrayList<>();
}
