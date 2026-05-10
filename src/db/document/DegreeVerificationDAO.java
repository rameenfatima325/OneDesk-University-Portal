package db.document;

import business.document.DegreeVerificationRequest;
import business.shared.DBConnection;
import java.sql.*;

public class DegreeVerificationDAO {

    public boolean saveDegreeRequest(DegreeVerificationRequest request) {
        String sql = "INSERT INTO degree_verification_requests " + "(student_id, graduation_year, degree_program, verification_type, purpose, copies, is_urgent) " + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, request.getStudentId());
            pstmt.setInt(2, request.getGraduationYear()); // Java field matches graduation_year
            pstmt.setString(3, request.getDegreeProgram());
            pstmt.setString(4, request.getVerificationType());
            pstmt.setString(5, request.getPurpose());
            pstmt.setInt(6, request.getCopies());
            pstmt.setBoolean(7, request.isUrgent());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database Error in UC10:");
            e.printStackTrace();
            return false;
        }
    }
}