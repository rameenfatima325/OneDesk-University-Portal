package db.document;

import business.document.TranscriptRequest;
import business.shared.DBConnection;
import java.sql.*;

public class TranscriptDAO {

    public boolean saveTranscriptRequest(TranscriptRequest request) {
        String sql = "INSERT INTO transcript_requests (student_id, transcript_type, purpose, copies, delivery_mode, mailing_address) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, request.getStudentId());
            pstmt.setString(2, request.getTranscriptType());
            pstmt.setString(3, request.getPurpose());
            pstmt.setInt(4, request.getCopies());
            pstmt.setString(5, request.getDeliveryMode());
            pstmt.setString(6, request.getMailingAddress() == null ? "" : request.getMailingAddress());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyStudentStatus(int studentId) {
        String sql = "SELECT COUNT(*) FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean verifyNoAcademicHold(int studentId) {
        String sql = "SELECT COUNT(*) FROM students WHERE student_id = ? AND enrollment_status = 'ACTIVE'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
    }
}