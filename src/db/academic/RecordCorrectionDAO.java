package db.academic;

import business.academic.AcademicRecordCorrection;
import business.shared.DBConnection;

import java.sql.*;

public class RecordCorrectionDAO {

    public boolean saveRecordCorrection(AcademicRecordCorrection req) {
        String sql = "INSERT INTO academic_record_corrections " +
                "(student_id, record_type, incorrect_value, correct_value, justification, evidence_path, priority) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, req.getStudentId());
            ps.setString(2, req.getRecordType().name());
            ps.setString(3, req.getIncorrectValue());
            ps.setString(4, req.getCorrectValue());
            ps.setString(5, req.getJustification());
            ps.setString(6, req.getEvidencePath() == null ? "" : req.getEvidencePath());
            ps.setString(7, req.getPriorityLevel().name());

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
