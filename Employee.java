package elms.model;

public class Employee extends User {
    private static final long serialVersionUID = 1L;
    private String managerId;

    public Employee(String id, String name, String username, String password,
                    String email, String department, String managerId) {
        super(id, name, username, password, email, department);
        this.managerId = managerId == null ? "" : managerId;
    }

    @Override public String getRole() { return "EMPLOYEE"; }
    @Override public String getDashboardTitle() { return "Employee Self-Service"; }
    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId == null ? "" : managerId; }
}
