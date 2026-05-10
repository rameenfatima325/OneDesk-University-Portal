package business.academic;

import db.document.UserDAO;

public class AuthController {
    private final UserDAO userDAO = new UserDAO();

    public String login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        return userDAO.authenticate(username.trim(), password);
    }
}