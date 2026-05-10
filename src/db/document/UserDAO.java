package db.document;

import business.shared.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public String authenticate(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password_hash = ? AND is_active = 1";

        try {
            Connection con = DBConnection.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public int getStudentIdByUsername(String username) {
        String sql = "SELECT s.student_id FROM users u " +
                     "JOIN students s ON s.student_id = u.user_id " +
                     "WHERE u.username = ? AND u.role = 'STUDENT' AND u.is_active = 1";
        try {
            Connection con = DBConnection.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("student_id");
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}