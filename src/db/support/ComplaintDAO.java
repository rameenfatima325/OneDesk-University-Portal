package db.support;

import business.shared.DBConnection;
import business.shared.RequestStatus;
import business.support.ComplaintCategory;
import business.support.GeneralComplaint;
import business.support.Severity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAO {


    public void insert(GeneralComplaint complaint) throws SQLException {
        String sql = "INSERT INTO general_complaints "
                   + "(student_id, category, severity, subject, description, "
                   + " is_anonymous, status, submission_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            // If anonymous, student_id will be 0 (set by AnonymousComplaintDecorator)
            int sid = complaint.isAnonymous() ? 0 : complaint.getStudentId();
            if (complaint.isAnonymous()) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, sid);
            }
            ps.setString (2, complaint.getCategory());
            ps.setString (3, complaint.getSeverity());
            ps.setString (4, complaint.getSubject());
            ps.setString (5, complaint.getDescription());
            ps.setBoolean(6, complaint.isAnonymous());
            ps.setString (7, complaint.getStatus().name());
            ps.setTimestamp(8, new Timestamp(complaint.getSubmissionDate().getTime()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) complaint.setRequestId(keys.getInt(1));
            }
        }
    }


    public List<GeneralComplaint> selectByStudentId(int studentId) throws SQLException {
        String sql = "SELECT * FROM general_complaints WHERE student_id = ? "
                   + "ORDER BY submission_date DESC";
        List<GeneralComplaint> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }



    public List<GeneralComplaint> selectAll() throws SQLException {
        String sql = "SELECT * FROM general_complaints ORDER BY submission_date DESC";
        List<GeneralComplaint> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }


    public void updateStatus(int complaintId, RequestStatus status,
                             String resolutionRemarks) throws SQLException {
        String sql = "UPDATE general_complaints "
                   + "SET status = ?, resolution_remarks = ? "
                   + "WHERE complaint_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setString(2, resolutionRemarks);
            ps.setInt   (3, complaintId);
            ps.executeUpdate();
        }
    }


    private GeneralComplaint mapRow(ResultSet rs) throws SQLException {
        int sid = rs.getInt("student_id");
        if (rs.wasNull()) sid = 0;

        GeneralComplaint c = new GeneralComplaint(sid);
        c.setRequestId(rs.getInt("complaint_id"));
        c.setCategory(ComplaintCategory.valueOf(rs.getString("category")));
        c.setSeverity(Severity.valueOf(rs.getString("severity")));
        c.setSubject(rs.getString("subject"));
        c.setDescription(rs.getString("description"));
        c.setAnonymous(rs.getBoolean("is_anonymous"));
        c.updateStatus(RequestStatus.valueOf(rs.getString("status")));
        c.setAdminRemarks(rs.getString("resolution_remarks"));
        Timestamp ts = rs.getTimestamp("submission_date");
        if (ts != null) c.setSubmissionDate(new java.util.Date(ts.getTime()));
        return c;
    }
}
