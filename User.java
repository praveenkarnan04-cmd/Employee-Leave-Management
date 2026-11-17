package elms.model;

import java.io.Serializable;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String name;
    private final String username;
    private final String password;
    private final String email;
    private final String department;
    private boolean active = true;

    protected User(String id, String name, String username, String password,
                   String email, String department) {
        this.id = id == null ? "" : id.trim();
        this.name = name == null ? "" : name.trim();
        this.username = username == null ? "" : username.trim();
        this.password = password == null ? "" : password;
        this.email = email == null ? "" : email.trim();
        this.department = department == null ? "" : department.trim();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public abstract String getRole();
    public abstract String getDashboardTitle();
}
