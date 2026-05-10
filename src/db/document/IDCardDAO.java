package db.document;

import business.document.IDCardRequest;
import business.shared.DBConnection;
import java.sql.*;

public class IDCardDAO {

    public boolean saveIDCardRequest(IDCardRequest request) {
        String sql = "INSERT INTO id_card_requests (student_id, request_type, replacement_reason, delivery_address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, request.getStudentId());
            pstmt.setString(2, request.getRequestType());
            pstmt.setString(3, request.getReplacementReason());
            pstmt.setString(4, request.getDeliveryAddress());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}