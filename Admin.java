package elms.model;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin(String id, String name, String username, String password,
                 String email, String department) {
        super(id, name, username, password, email, department);
    }

    @Override public String getRole() { return "ADMIN"; }
    @Override public String getDashboardTitle() { return "HR Administration"; }
}
