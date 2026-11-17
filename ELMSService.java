package elms.service;

import elms.exceptions.LeaveException;
import elms.interfaces.Authenticatable;
import elms.interfaces.LeaveOperations;
import elms.model.*;
import elms.storage.FileHandler;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class ELMSService implements Authenticatable, LeaveOperations {
    private static final String DATA_FILE = "data/elms-data.ser";
    private ELMSData data;

    public ELMSService() {
        data = FileHandler.load(DATA_FILE, ELMSData.class);
        if (data == null) {
            data = new ELMSData();
            seed();
            save();
        }
    }

    private void seed() {
        Admin admin = new Admin("A001", "System Administrator", "admin", "admin123",
                "admin@elms.local", "Human Resources");
        Manager manager = new Manager("M001", "Demo Manager", "manager", "manager123",
                "manager@elms.local", "Information Technology");
        Employee employee = new Employee("E001", "Demo Employee", "employee", "employee123",
                "employee@elms.local", "Information Technology", "M001");
        data.users.add(admin);
        data.users.add(manager);
        data.users.add(employee);
        data.leaveTypes.add(new LeaveType("Annual", 14));
        data.leaveTypes.add(new LeaveType("Casual", 7));
        data.leaveTypes.add(new LeaveType("Medical", 14));
        for (LeaveType type : data.leaveTypes)
            data.balances.add(new LeaveBalance(employee.getId(), type.getName(),
                    LocalDate.now().getYear(), type.getDefaultEntitlement()));
    }

    public void resetDemoData() {
        data = new ELMSData();
        seed();
        save();
    }

    private void save() { FileHandler.save(data, DATA_FILE); }

    @Override
    public User login(String username, String password) {
        if (username == null || password == null) return null;
        return data.users.stream()
                .filter(User::isActive)
                .filter(u -> u.getUsername().equalsIgnoreCase(username.trim()) && u.getPassword().equals(password))
                .findFirst().orElse(null);
    }

    public ArrayList<User> getUsers() { return new ArrayList<>(data.users); }
    public ArrayList<Employee> getEmployees() {
        return data.users.stream().filter(Employee.class::isInstance)
                .map(Employee.class::cast).collect(Collectors.toCollection(ArrayList::new));
    }
    public ArrayList<Manager> getManagers() {
        return data.users.stream().filter(Manager.class::isInstance)
                .map(Manager.class::cast).collect(Collectors.toCollection(ArrayList::new));
    }
    public ArrayList<LeaveType> getLeaveTypes() {
        return data.leaveTypes.stream().filter(LeaveType::isActive)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    public ArrayList<LeaveApplication> getApplications() { return new ArrayList<>(data.applications); }
    public ArrayList<Attendance> getAttendance() { return new ArrayList<>(data.attendance); }
    public ArrayList<Notification> getNotifications(String userId) {
        return data.notifications.stream().filter(n -> n.getRecipientId().equals(userId))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    public String employeeName(String id) {
        return data.users.stream().filter(u -> u.getId().equals(id)).map(User::getName).findFirst().orElse(id);
    }
    public User findUser(String id) {
        return data.users.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
    }

    public void addEmployee(String id, String name, String username, String password,
                            String email, String department, String managerId) {
        if (id == null || id.isBlank() || data.users.stream().anyMatch(u -> u.getId().equalsIgnoreCase(id)))
            throw new LeaveException("Employee ID is required and must be unique.");
        if (data.users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username)))
            throw new LeaveException("Username already exists.");
        Employee employee = new Employee(id.trim(), name, username, password, email, department, managerId);
        data.users.add(employee);
        for (LeaveType type : getLeaveTypes())
            data.balances.add(new LeaveBalance(employee.getId(), type.getName(),
                    LocalDate.now().getYear(), type.getDefaultEntitlement()));
        save();
    }

    public void addLeaveType(String name, double entitlement) {
        if (data.leaveTypes.stream().anyMatch(t -> t.getName().equalsIgnoreCase(name)))
            throw new LeaveException("That leave type already exists.");
        LeaveType type = new LeaveType(name, entitlement);
        data.leaveTypes.add(type);
        for (Employee e : getEmployees())
            data.balances.add(new LeaveBalance(e.getId(), type.getName(),
                    LocalDate.now().getYear(), entitlement));
        save();
    }

    public LeaveBalance getBalance(String employeeId, String type) {
        int year = LocalDate.now().getYear();
        return data.balances.stream()
                .filter(b -> b.getEmployeeId().equals(employeeId) && b.getLeaveType().equals(type) && b.getYear() == year)
                .findFirst().orElse(null);
    }

    public ArrayList<LeaveBalance> getBalances(String employeeId) {
        return data.balances.stream().filter(b -> b.getEmployeeId().equals(employeeId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private double workingDays(LocalDate start, LocalDate end) {
        double count = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) count++;
        }
        return count;
    }

    @Override
    public LeaveApplication applyForLeave(Employee employee, String leaveType,
            String startDate, String endDate, String reason, String documentPath) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            if (end.isBefore(start)) throw new LeaveException("End date cannot be before start date.");
            if (start.isBefore(LocalDate.now())) throw new LeaveException("Leave cannot start in the past.");
            if (reason == null || reason.isBlank()) throw new LeaveException("Please enter a reason.");
            if (getLeaveTypes().stream().noneMatch(t -> t.getName().equals(leaveType)))
                throw new LeaveException("Select an active leave type.");
            double days = workingDays(start, end);
            if (days <= 0) throw new LeaveException("The selected dates contain no working days.");
            LeaveBalance balance = getBalance(employee.getId(), leaveType);
            if (balance == null || balance.getRemaining() < days)
                throw new LeaveException("Insufficient " + leaveType + " leave balance. Required: " + days + " day(s).");
            boolean overlap = data.applications.stream().anyMatch(a ->
                    a.getEmployeeId().equals(employee.getId())
                    && (a.getStatus() == LeaveApplication.Status.PENDING || a.getStatus() == LeaveApplication.Status.APPROVED)
                    && !end.isBefore(a.getStartDate()) && !start.isAfter(a.getEndDate()));
            if (overlap) throw new LeaveException("These dates overlap an existing pending or approved application.");
            String ref = "LV-" + (10000 + data.applications.size() + 1);
            LeaveApplication app = new LeaveApplication(ref, employee.getId(), leaveType,
                    start, end, reason.trim(), documentPath, days);
            data.applications.add(app);
            for (Employee e : getEmployees()) {
                if (e.getId().equals(employee.getId())) {
                    data.notifications.add(new Notification(e.getManagerId(),
                            "New leave request " + ref + " from " + employee.getName() + "."));
                }
            }
            save();
            return app;
        } catch (DateTimeParseException ex) {
            throw new LeaveException("Enter dates in YYYY-MM-DD format.");
        }
    }

    @Override
    public void cancelApplication(Employee employee, String reference) {
        LeaveApplication app = findApplication(reference);
        if (app == null || !app.getEmployeeId().equals(employee.getId()))
            throw new LeaveException("Application not found.");
        if (app.getStatus() != LeaveApplication.Status.PENDING)
            throw new LeaveException("Only pending applications can be cancelled.");
        app.decide(LeaveApplication.Status.CANCELLED, employee.getId(), "Cancelled by employee");
        save();
    }

    private LeaveApplication findApplication(String reference) {
        return data.applications.stream().filter(a -> a.getReference().equalsIgnoreCase(reference))
                .findFirst().orElse(null);
    }

    public ArrayList<LeaveApplication> applicationsFor(Employee employee) {
        return data.applications.stream().filter(a -> a.getEmployeeId().equals(employee.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<LeaveApplication> pendingFor(Manager manager) {
        return data.applications.stream().filter(a -> a.getStatus() == LeaveApplication.Status.PENDING)
                .filter(a -> findUser(a.getEmployeeId()) instanceof Employee)
                .filter(a -> ((Employee)findUser(a.getEmployeeId())).getManagerId().equals(manager.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void decide(Manager manager, String reference, boolean approve, String reason) {
        LeaveApplication app = findApplication(reference);
        if (app == null || app.getStatus() != LeaveApplication.Status.PENDING)
            throw new LeaveException("This application is no longer pending.");
        User user = findUser(app.getEmployeeId());
        if (!(user instanceof Employee) || !((Employee)user).getManagerId().equals(manager.getId()))
            throw new LeaveException("You are not authorized to decide this application.");
        if (!approve && (reason == null || reason.isBlank()))
            throw new LeaveException("A rejection reason is required.");
        if (approve) {
            LeaveBalance balance = getBalance(app.getEmployeeId(), app.getLeaveType());
            if (balance == null || balance.getRemaining() < app.getChargeableDays())
                throw new LeaveException("Insufficient balance at approval time.");
            if (!app.isBalanceDeducted()) {
                balance.deduct(app.getChargeableDays());
                app.setBalanceDeducted(true);
            }
            app.decide(LeaveApplication.Status.APPROVED, manager.getId(), "Approved");
            for (LocalDate d = app.getStartDate(); !d.isAfter(app.getEndDate()); d = d.plusDays(1)) {
                if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                    upsertAttendance(app.getEmployeeId(), d, "On Leave", manager.getId());
            }
        } else {
            app.decide(LeaveApplication.Status.REJECTED, manager.getId(), reason.trim());
        }
        data.notifications.add(new Notification(app.getEmployeeId(),
                "Your request " + reference + " was " + (approve ? "approved." : "rejected: " + reason)));
        save();
    }

    private void upsertAttendance(String employeeId, LocalDate date, String status, String recorder) {
        Attendance existing = data.attendance.stream()
                .filter(a -> a.getEmployeeId().equals(employeeId) && a.getDate().equals(date))
                .findFirst().orElse(null);
        if (existing == null) data.attendance.add(new Attendance(employeeId, date, status, recorder));
        else existing.setStatus(status);
    }

    public void markAttendance(String employeeId, String dateText, String status, String recordedBy) {
        try {
            LocalDate date = LocalDate.parse(dateText);
            if (findUser(employeeId) == null) throw new LeaveException("Employee not found.");
            if (!List.of("Present", "Absent", "On Leave").contains(status))
                throw new LeaveException("Choose a valid attendance status.");
            upsertAttendance(employeeId, date, status, recordedBy);
            save();
        } catch (DateTimeParseException ex) {
            throw new LeaveException("Enter date as YYYY-MM-DD.");
        }
    }

    public long countStatus(LeaveApplication.Status status) {
        return data.applications.stream().filter(a -> a.getStatus() == status).count();
    }
    public long employeesOnLeaveToday() {
        LocalDate today = LocalDate.now();
        return data.applications.stream().filter(a -> a.getStatus() == LeaveApplication.Status.APPROVED)
                .filter(a -> !today.isBefore(a.getStartDate()) && !today.isAfter(a.getEndDate())).count();
    }
    public void markNotificationsRead(String userId) {
        getNotifications(userId).forEach(Notification::markRead);
        save();
    }
    public String reportSummary() {
        StringBuilder sb = new StringBuilder("EMPLOYEE LEAVE MANAGEMENT REPORT\n");
        sb.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        sb.append("Employees: ").append(getEmployees().size()).append("\n");
        for (LeaveApplication.Status s : LeaveApplication.Status.values())
            sb.append(s).append(": ").append(countStatus(s)).append("\n");
        sb.append("\nDepartment-wise employees:\n");
        Map<String, Long> departments = getEmployees().stream()
                .collect(Collectors.groupingBy(User::getDepartment, TreeMap::new, Collectors.counting()));
        departments.forEach((k,v) -> sb.append(k).append(": ").append(v).append("\n"));
        return sb.toString();
    }
}
