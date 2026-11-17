package elms.interfaces;

import elms.model.User;

public interface Authenticatable {
    User login(String username, String password);
}
