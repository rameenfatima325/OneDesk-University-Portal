package db.support;

import business.shared.DBConnection;
import business.shared.RequestStatus;
import business.support.ItemCategory;
import business.support.LostFoundRequest;
import business.support.LostFoundType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class LostFoundDAO {


    public void insertLostItem(LostFoundRequest req) throws SQLException {
        String sql = "INSERT INTO lost_items "
                + "(student_id, item_name, category, location, report_date, "
                + " description, contact, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1, req.getStudentId());
            ps.setString(2, req.getItemName());
            ps.setString(3, req.getCategory());
            ps.setString(4, req.getLocation());
            ps.setDate  (5, new java.sql.Date(req.getReportDate().getTime()));
            ps.setString(6, req.getDescription());
            ps.setString(7, req.getContact());
            ps.setString(8, req.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) req.setRequestId(keys.getInt(1));
            }
        }
    }


    public void insertFoundItem(LostFoundRequest req) throws SQLException {
        String sql = "INSERT INTO found_items "
                + "(student_id, item_name, category, location, report_date, "
                + " storage_location, contact, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1, req.getStudentId());
            ps.setString(2, req.getItemName());
            ps.setString(3, req.getCategory());
            ps.setString(4, req.getLocation());
            ps.setDate  (5, new java.sql.Date(req.getReportDate().getTime()));
            ps.setString(6, req.getStorageLocation());
            ps.setString(7, req.getContact());
            ps.setString(8, req.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) req.setRequestId(keys.getInt(1));
            }
        }
    }


    public List<LostFoundRequest> selectLostByStudentId(int studentId) throws SQLException {
        String sql = "SELECT *, 'LOST' AS req_type FROM lost_items "
                + "WHERE student_id = ? ORDER BY report_date DESC";
        List<LostFoundRequest> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLostRow(rs));
            }
        }
        return list;
    }


    public List<LostFoundRequest> selectAllLostItems() throws SQLException {
        String sql = "SELECT *, 'LOST' AS req_type FROM lost_items ORDER BY report_date DESC";
        List<LostFoundRequest> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapLostRow(rs));
        }
        return list;
    }


    public List<LostFoundRequest> selectAllFoundItems() throws SQLException {
        String sql = "SELECT *, 'FOUND' AS req_type FROM found_items ORDER BY report_date DESC";
        List<LostFoundRequest> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapFoundRow(rs));
        }
        return list;
    }


    public List<LostFoundRequest> searchMatches(String itemName,
                                                ItemCategory category) throws SQLException {
        String sql = "SELECT *, 'FOUND' AS req_type FROM found_items "
                + "WHERE LOWER(item_name) = LOWER(?) AND category = ? "
                + "AND status = 'PENDING'";
        List<LostFoundRequest> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemName.trim());
            ps.setString(2, category.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapFoundRow(rs));
            }
        }
        return list;
    }


    public void updateStatus(int itemId, LostFoundType type,
                             RequestStatus status, String remarks) throws SQLException {
        String table = (type == LostFoundType.LOST) ? "lost_items" : "found_items";
        String sql   = "UPDATE " + table + " SET status = ? WHERE item_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt   (2, itemId);
            ps.executeUpdate();
        }
    }


    private LostFoundRequest mapLostRow(ResultSet rs) throws SQLException {
        LostFoundRequest req = new LostFoundRequest(rs.getInt("student_id"));
        req.setRequestId(rs.getInt("item_id"));
        req.setRequestType(LostFoundType.LOST);
        req.setItemName(rs.getString("item_name"));
        req.setCategory(ItemCategory.valueOf(rs.getString("category")));
        req.setLocation(rs.getString("location"));
        java.sql.Date d = rs.getDate("report_date");
        if (d != null) req.setReportDate(new java.util.Date(d.getTime()));
        req.setDescription(rs.getString("description"));
        req.setContact(rs.getString("contact"));
        req.updateStatus(RequestStatus.valueOf(rs.getString("status")));
        return req;
    }

    private LostFoundRequest mapFoundRow(ResultSet rs) throws SQLException {
        LostFoundRequest req = new LostFoundRequest(rs.getInt("student_id"));
        req.setRequestId(rs.getInt("item_id"));
        req.setRequestType(LostFoundType.FOUND);
        req.setItemName(rs.getString("item_name"));
        req.setCategory(ItemCategory.valueOf(rs.getString("category")));
        req.setLocation(rs.getString("location"));
        java.sql.Date d = rs.getDate("report_date");
        if (d != null) req.setReportDate(new java.util.Date(d.getTime()));
        req.setStorageLocation(rs.getString("storage_location"));
        req.setContact(rs.getString("contact"));
        req.updateStatus(RequestStatus.valueOf(rs.getString("status")));
        return req;
    }
}