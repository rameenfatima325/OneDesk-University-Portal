package db.academic;

import business.academic.CourseRegistrationIssue;
import business.shared.DBConnection;

import java.sql.*;

public class CourseIssueDAO {

    public boolean saveCourseIssue(CourseRegistrationIssue req) {
        String sql = "INSERT INTO course_registration_issues " +
                "(student_id, course_code, section, semester, issue_type, description, urgency) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, req.getStudentId());
            ps.setString(2, req.getCourseCode());
            ps.setString(3, req.getSection() == null ? "" : req.getSection());
            ps.setString(4, req.getSemester());
            ps.setString(5, req.getIssueType().name());
            ps.setString(6, req.getIssueDescription());
            ps.setString(7, req.getUrgency().name());

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
