package db.academic;

import business.shared.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public String authenticate(String username, String password) {
        String sql = "SELECT role FROM users "
                   + "WHERE username = ? AND password_hash = ? AND is_active = 1";
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            System.err.println("DB error during login: " + e.getMessage());
        }
        return null;
    }

    public int getUserId(String username) {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("DB error fetching user_id: " + e.getMessage());
        }
        return -1;
    }

    public String getFullName(String username) {
        String sql = "SELECT full_name FROM users WHERE username = ?";
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("full_name");
                }
            }
        } catch (SQLException e) {
            System.err.println("DB error fetching full_name: " + e.getMessage());
        }
        return username;
    }
}
