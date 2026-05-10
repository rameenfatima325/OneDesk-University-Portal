package db.academic;

import business.academic.AddDropRequest;
import business.shared.DBConnection;

import java.sql.*;

public class AddDropDAO {

    public boolean saveAddDropRequest(AddDropRequest req) {
        String sql = "INSERT INTO add_drop_requests " +
                "(student_id, request_type, course_code, reason, supporting_doc_path) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, req.getStudentId());
            ps.setString(2, req.getRequestType().name());
            ps.setString(3, req.getCourseCode() == null ? "" : req.getCourseCode());
            ps.setString(4, req.getReason());
            ps.setString(5, req.getSupportingDocPath() == null ? "" : req.getSupportingDocPath());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyStudentExists(int studentId) {
        String sql = "SELECT COUNT(*) FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) { return false; }
    }
}
