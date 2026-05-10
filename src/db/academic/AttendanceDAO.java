package db.academic;

import business.academic.AttendanceCorrectionRequest;
import business.shared.DBConnection;

import java.sql.*;

public class AttendanceDAO {

    public boolean saveAttendanceRequest(AttendanceCorrectionRequest req) {
        String sql = "INSERT INTO attendance_requests " +
                "(student_id, course_name, semester, class_date, reason, supporting_doc_path) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, req.getStudentId());
            ps.setString(2, req.getCourseName());
            ps.setString(3, req.getSemester());
            ps.setDate  (4, java.sql.Date.valueOf(req.getClassDate()));
            ps.setString(5, req.getReason());
            ps.setString(6, req.getSupportingDocPath() == null ? "" : req.getSupportingDocPath());

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
