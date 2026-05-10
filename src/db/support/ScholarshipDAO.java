package db.support;

import business.shared.DBConnection;
import business.shared.RequestStatus;
import business.support.ScholarshipQuery;
import business.support.ScholarshipType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScholarshipDAO {


    public void insert(ScholarshipQuery query) throws SQLException {
        String sql = "INSERT INTO scholarship_queries "
                   + "(student_id, scholarship_type, cgpa, family_income, query_text, "
                   + " doc_path, eligibility_flag, status, submission_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt    (1, query.getStudentId());
            ps.setString (2, query.getScholarshipType().name());
            ps.setDouble (3, query.getCgpa());
            ps.setDouble (4, query.getFamilyIncome());
            ps.setString (5, query.getQueryText());
            ps.setString (6, query.getDocPath());
            ps.setBoolean(7, query.isEligibilityFlag());
            ps.setString (8, query.getStatus().name());
            ps.setTimestamp(9, new Timestamp(query.getSubmissionDate().getTime()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) query.setRequestId(keys.getInt(1));
            }
        }
    }


    public List<ScholarshipQuery> selectByStudentId(int studentId) throws SQLException {
        String sql = "SELECT * FROM scholarship_queries WHERE student_id = ? "
                   + "ORDER BY submission_date DESC";
        List<ScholarshipQuery> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }


    public List<ScholarshipQuery> selectAll() throws SQLException {
        String sql = "SELECT * FROM scholarship_queries ORDER BY submission_date DESC";
        List<ScholarshipQuery> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }


    public void updateStatus(int queryId, RequestStatus status,
                             String adminRemarks) throws SQLException {
        String sql = "UPDATE scholarship_queries "
                   + "SET status = ?, admin_remarks = ? "
                   + "WHERE query_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setString(2, adminRemarks);
            ps.setInt   (3, queryId);
            ps.executeUpdate();
        }
    }


    private ScholarshipQuery mapRow(ResultSet rs) throws SQLException {
        ScholarshipQuery q = new ScholarshipQuery(rs.getInt("student_id"));
        q.setRequestId(rs.getInt("query_id"));
        q.setScholarshipType(ScholarshipType.valueOf(rs.getString("scholarship_type")));

        try {
            q.setCgpa(rs.getDouble("cgpa"));
            q.setFamilyIncome(rs.getDouble("family_income"));
        } catch (IllegalArgumentException ignored) {}

        q.setQueryText(rs.getString("query_text"));
        q.setDocPath(rs.getString("doc_path"));
        q.updateStatus(RequestStatus.valueOf(rs.getString("status")));
        q.setAdminRemarks(rs.getString("admin_remarks"));
        Timestamp ts = rs.getTimestamp("submission_date");
        if (ts != null) q.setSubmissionDate(new java.util.Date(ts.getTime()));
        return q;
    }
}
