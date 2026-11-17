package elms.model;

public class Manager extends User {
    private static final long serialVersionUID = 1L;

    public Manager(String id, String name, String username, String password,
                   String email, String department) {
        super(id, name, username, password, email, department);
    }

    @Override public String getRole() { return "MANAGER"; }
    @Override public String getDashboardTitle() { return "Manager Dashboard"; }
}
