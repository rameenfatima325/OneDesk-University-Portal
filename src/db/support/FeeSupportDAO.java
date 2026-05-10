package db.support;

import business.shared.DBConnection;
import business.shared.RequestStatus;
import business.support.FeeSupportRequest;
import business.support.FeeType;
import business.support.FeeRequestType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class FeeSupportDAO {


    public void insert(FeeSupportRequest req) throws SQLException {
        String sql = "INSERT INTO fee_support_requests "
                   + "(student_id, fee_type, outstanding_amount, request_type, "
                   + " justification, doc_path, status, submission_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1, req.getStudentId());
            ps.setString(2, req.getFeeType().name());
            ps.setDouble(3, req.getOutstandingAmount());
            ps.setString(4, req.getRequestType().name());
            ps.setString(5, req.getJustification());
            ps.setString(6, req.getDocPath());
            ps.setString(7, req.getStatus().name());
            ps.setTimestamp(8, new Timestamp(req.getSubmissionDate().getTime()));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) req.setRequestId(keys.getInt(1));
            }
        }
    }


    public List<FeeSupportRequest> selectByStudentId(int studentId) throws SQLException {
        String sql = "SELECT * FROM fee_support_requests WHERE student_id = ? "
                   + "ORDER BY submission_date DESC";
        List<FeeSupportRequest> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }


    public List<FeeSupportRequest> selectAll() throws SQLException {
        String sql = "SELECT * FROM fee_support_requests ORDER BY submission_date DESC";
        List<FeeSupportRequest> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }


    public void updateStatus(int requestId, RequestStatus status,
                             String adminRemarks) throws SQLException {
        String sql = "UPDATE fee_support_requests "
                   + "SET status = ?, admin_remarks = ? "
                   + "WHERE request_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setString(2, adminRemarks);
            ps.setInt   (3, requestId);
            ps.executeUpdate();
        }
    }


    public void delete(int requestId) throws SQLException {
        String sql = "DELETE FROM fee_support_requests WHERE request_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }


    private FeeSupportRequest mapRow(ResultSet rs) throws SQLException {
        FeeSupportRequest req = new FeeSupportRequest(rs.getInt("student_id"));
        req.setRequestId(rs.getInt("request_id"));
        req.setFeeType(FeeType.valueOf(rs.getString("fee_type")));
        try {
            req.setOutstandingAmount(rs.getDouble("outstanding_amount"));
        } catch (Exception e) {
            // should never happen — DB has valid data
        }
        req.setRequestType(FeeRequestType.valueOf(rs.getString("request_type")));
        req.setJustification(rs.getString("justification"));
        req.setDocPath(rs.getString("doc_path"));
        req.updateStatus(RequestStatus.valueOf(rs.getString("status")));
        req.setAdminRemarks(rs.getString("admin_remarks"));
        Timestamp ts = rs.getTimestamp("submission_date");
        if (ts != null) req.setSubmissionDate(new java.util.Date(ts.getTime()));
        return req;
    }
}
