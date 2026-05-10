package db.document;

import business.document.EnrollmentLetterRequest;
import business.shared.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EnrollmentLetterDAO {

    public boolean saveEnrollmentLetterRequest(EnrollmentLetterRequest request) {
        String sql = "INSERT INTO enrollment_letter_requests (student_id, letter_type, addressed_to, purpose, language, copies) " + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, request.getStudentId());
            pstmt.setString(2, request.getLetterType());
            pstmt.setString(3, request.getAddressedTo());
            pstmt.setString(4, request.getPurpose());
            pstmt.setString(5, request.getLanguage());
            pstmt.setInt(6, request.getCopies());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Database error: Failed to save Enrollment Letter Request");
            e.printStackTrace();
            return false;
        }
    }
}