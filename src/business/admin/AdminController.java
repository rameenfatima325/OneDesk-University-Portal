package business.admin;

import business.shared.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;


public class AdminController {

    public ObservableList<AdminRequestModel> fetchAllRequests() {
        ObservableList<AdminRequestModel> data = FXCollections.observableArrayList();

        String sql =
                "SELECT request_id,    student_id, 'ATTENDANCE'        AS type, submission_date, status, 0 AS is_collected, '' AS delivery_mode, COALESCE(CAST(admin_remarks AS NVARCHAR(MAX)),'') AS admin_remarks FROM attendance_requests "
                        + "UNION ALL "
                        + "SELECT issue_id      AS request_id, student_id, 'COURSE_ISSUE'  AS type, created_at      AS submission_date, status, 0 AS is_collected, '' AS delivery_mode, COALESCE(CAST(admin_remarks AS NVARCHAR(MAX)),'') AS admin_remarks FROM course_registration_issues "
                        + "UNION ALL "
                        + "SELECT request_id,    student_id, 'ADD_DROP'          AS type, submission_date, status, 0 AS is_collected, '' AS delivery_mode, COALESCE(CAST(admin_remarks AS NVARCHAR(MAX)),'') AS admin_remarks FROM add_drop_requests "
                        + "UNION ALL "
                        + "SELECT correction_id AS request_id, student_id, 'RECORD_CORRECTION' AS type, submission_date, status, 0 AS is_collected, '' AS delivery_mode, COALESCE(CAST(admin_remarks AS NVARCHAR(MAX)),'') AS admin_remarks FROM academic_record_corrections "
                        + "UNION ALL "
                        // ---- Rameen's document requests (UC9–UC12) ----
                        + "SELECT request_id, student_id, 'TRANSCRIPT'    AS type, submission_date, status, is_collected, COALESCE(delivery_mode,'') AS delivery_mode, COALESCE(CAST(admin_remarks AS NVARCHAR(MAX)),'') AS admin_remarks FROM transcript_requests "
                        + "UNION ALL "
                        + "SELECT request_id, student_id, 'ID_CARD'       AS type, submission_date, status, is_collected, COALESCE(delivery_mode,'') AS delivery_mode, COALESCE(CAST(admin_remarks AS NVARCHAR(MAX)),'') AS admin_remarks FROM id_card_requests "
                        + "UNION ALL "
                        + "SELECT request_id, student_id, 'ENROLL_LETTER' AS type, submission_date, status, is_collected, COALESCE(delivery_mode,'') AS delivery_mode, COALESCE(CAST(admin_remarks AS NVARCHAR(MAX)),'') AS admin_remarks FROM enrollment_letter_requests "
                        + "UNION ALL "
                        + "SELECT request_id, student_id, 'DEGREE_VER'    AS type, submission_date, status, is_collected, COALESCE(delivery_mode,'') AS delivery_mode, COALESCE(CAST(admin_remarks AS NVARCHAR(MAX)),'') AS admin_remarks FROM degree_verification_requests "
                        + "UNION ALL "
                        // ---- Aizah's support requests (UC5–UC8) ----
                        + "SELECT request_id, student_id, 'FEE_SUPPORT'  AS type, submission_date, status, 0 AS is_collected, '' AS delivery_mode, COALESCE(CAST(admin_remarks AS NVARCHAR(MAX)),'') AS admin_remarks FROM fee_support_requests "
                        + "UNION ALL "
                        + "SELECT item_id AS request_id, student_id, 'LOST_FOUND'  AS type, CAST(GETDATE() AS DATETIME) AS submission_date, status, 0 AS is_collected, '' AS delivery_mode, '' AS admin_remarks FROM lost_items "
                        + "UNION ALL "
                        + "SELECT query_id AS request_id, student_id, 'SCHOLARSHIP' AS type, submission_date, status, 0 AS is_collected, '' AS delivery_mode, COALESCE(CAST(admin_remarks AS NVARCHAR(MAX)),'') AS admin_remarks FROM scholarship_queries "
                        + "UNION ALL "
                        + "SELECT complaint_id AS request_id, student_id, 'COMPLAINT'   AS type, submission_date, status, 0 AS is_collected, '' AS delivery_mode, COALESCE(CAST(resolution_remarks AS NVARCHAR(MAX)),'') AS admin_remarks FROM general_complaints "
                        + "ORDER BY submission_date DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            while (rs.next()) {
                AdminRequestModel m = new AdminRequestModel(
                        rs.getInt("request_id"),
                        rs.getInt("student_id"),
                        rs.getString("type"),
                        rs.getTimestamp("submission_date").toString(),
                        rs.getString("status"),
                        rs.getInt("is_collected")
                );
                m.setDeliveryMode(rs.getString("delivery_mode"));
                m.setRemarks(rs.getString("admin_remarks"));
                data.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }


    public void enrichWithDetails(AdminRequestModel model) {
        String table     = getTableName(model.getType());
        String pkColumn  = getPrimaryKeyColumn(model.getType());
        if (table.isEmpty()) return;

        String sql = "SELECT * FROM " + table + " WHERE " + pkColumn + " = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, model.getRequestId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                model.setDeliveryMode(safeString(rs, "delivery_mode"));
                model.setPurpose(safeString(rs, "purpose"));
                String addr = safeString(rs, "postal_address");
                if (addr.isBlank()) addr = safeString(rs, "delivery_address");
                if (addr.isBlank()) addr = safeString(rs, "mailing_address");
                if (addr.isBlank()) addr = safeString(rs, "address");
                model.setAddress(addr);
                String copies = safeString(rs, "num_copies");
                if (copies.isBlank()) copies = safeString(rs, "copies");
                model.setCopies(copies);

                String t = model.getType() == null ? "" : model.getType().toUpperCase();
                switch (t) {
                    case "ATTENDANCE":
                        model.putExtra("Subject",      safeString(rs, "course_name"));
                        model.putExtra("Semester",     safeString(rs, "semester"));
                        model.putExtra("Class Date",   safeString(rs, "class_date"));
                        model.putExtra("Reason",       safeString(rs, "reason"));
                        break;
                    case "COURSE_ISSUE":
                        model.putExtra("Course Code",  safeString(rs, "course_code"));
                        model.putExtra("Issue Type",   safeString(rs, "issue_type"));
                        model.putExtra("Description",  safeString(rs, "description"));
                        break;
                    case "ADD_DROP":
                        model.putExtra("Course Code",  safeString(rs, "course_code"));
                        model.putExtra("Action",       safeString(rs, "request_type"));
                        model.putExtra("Reason",       safeString(rs, "reason"));
                        model.putExtra("Supporting Doc", safeString(rs, "supporting_document"));
                        break;
                    case "RECORD_CORRECTION":
                        model.putExtra("Record Type",   safeString(rs, "record_type"));
                        model.putExtra("Course Code",   safeString(rs, "course_code"));
                        model.putExtra("Current Value", safeString(rs, "current_value"));
                        model.putExtra("Requested Value", safeString(rs, "requested_value"));
                        model.putExtra("Justification", safeString(rs, "justification"));
                        break;
                    case "TRANSCRIPT":
                        model.putExtra("Transcript Type", safeString(rs, "transcript_type"));
                        break;
                    case "ID_CARD":
                        model.putExtra("Card Type",   safeString(rs, "card_type"));
                        model.putExtra("Reason",      safeString(rs, "reason"));
                        break;
                    case "ENROLL_LETTER":
                        model.putExtra("Letter Type", safeString(rs, "letter_type"));
                        model.putExtra("Recipient",   safeString(rs, "recipient_name"));
                        break;
                    case "DEGREE_VER":
                        model.putExtra("Degree Program",      safeString(rs, "degree_program"));
                        model.putExtra("Graduation Year",     safeString(rs, "graduation_year"));
                        model.putExtra("Verification Authority", safeString(rs, "verification_type"));
                        break;
                    case "FEE_SUPPORT":
                        model.putExtra("Fee Type",      safeString(rs, "fee_type"));
                        model.putExtra("Request Type",  safeString(rs, "request_type"));
                        model.putExtra("Amount",        safeString(rs, "outstanding_amount"));
                        model.putExtra("Justification", safeString(rs, "justification"));
                        break;
                    case "LOST_FOUND":
                        model.putExtra("Item Name",     safeString(rs, "item_name"));
                        model.putExtra("Category",      safeString(rs, "category"));
                        model.putExtra("Location",      safeString(rs, "location"));
                        model.putExtra("Report Date",   safeString(rs, "report_date"));
                        model.putExtra("Description",   safeString(rs, "description"));
                        model.putExtra("Contact",       safeString(rs, "contact"));
                        break;
                    case "SCHOLARSHIP":
                        model.putExtra("Scholarship Type", safeString(rs, "scholarship_type"));
                        model.putExtra("CGPA",             safeString(rs, "cgpa"));
                        model.putExtra("Family Income",    safeString(rs, "family_income"));
                        model.putExtra("Eligibility",      rs.getInt("eligibility_flag") == 1 ? "Eligible" : "Not Eligible");
                        model.putExtra("Query",            safeString(rs, "query_text"));
                        break;
                    case "COMPLAINT":
                        model.putExtra("Category",    safeString(rs, "category"));
                        model.putExtra("Severity",    safeString(rs, "severity"));
                        model.putExtra("Subject",     safeString(rs, "subject"));
                        model.putExtra("Description", safeString(rs, "description"));
                        break;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean updateRequestStatus(int id, String type, String newStatus,
                                       String remarks, LocalDate date,
                                       String adminUser, int isCollected) {
        String table     = getTableName(type);
        String pkColumn  = getPrimaryKeyColumn(type);
        String dateCol   = getDateColumnName(type);
        boolean hasCollectedCol = hasDeliveryColumns(type);
        boolean hasRemarksCol   = hasRemarksColumn(type);

        StringBuilder sb = new StringBuilder("UPDATE ").append(table).append(" SET status = ?");
        if (hasRemarksCol) {
            String remarksCol = getRemarksColumn(type);
            sb.append(", ").append(remarksCol).append(" = ?");
        }
        if (hasCollectedCol) sb.append(", is_collected = ?");
        if (dateCol != null) sb.append(", ").append(dateCol).append(" = ?");
        sb.append(" WHERE ").append(pkColumn).append(" = ?");
        String updateSql = sb.toString();

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    int idx = 1;
                    ps.setString(idx++, newStatus);
                    if (hasRemarksCol) ps.setString(idx++, remarks);
                    if (hasCollectedCol) ps.setInt(idx++, isCollected);
                    if (dateCol != null) {
                        if (date != null) ps.setDate(idx++, java.sql.Date.valueOf(date));
                        else              ps.setNull(idx++, Types.DATE);
                    }
                    ps.setInt(idx, id);
                    ps.executeUpdate();
                }

                String logSql = "INSERT INTO audit_log "
                        + "(admin_username, request_id, request_type, old_status, new_status, remarks) "
                        + "VALUES (?, ?, ?, 'N/A', ?, ?)";
                try (PreparedStatement lp = conn.prepareStatement(logSql)) {
                    lp.setString(1, adminUser);
                    lp.setInt   (2, id);
                    lp.setString(3, type);
                    lp.setString(4, newStatus);
                    lp.setString(5, remarks);
                    lp.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public ObservableList<AuditLogModel> fetchAuditLogs() {
        ObservableList<AuditLogModel> logs = FXCollections.observableArrayList();
        String sql = "SELECT admin_username, request_id, request_type, old_status, new_status, "
                + "action_timestamp, remarks FROM audit_log ORDER BY action_timestamp DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new AuditLogModel(
                        rs.getString("admin_username"),
                        rs.getInt("request_id"),
                        rs.getString("request_type"),
                        rs.getString("old_status"),
                        rs.getString("new_status"),
                        rs.getTimestamp("action_timestamp").toString(),
                        rs.getString("remarks")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return logs;
    }

    public String getAdminRemarks(int requestId, String type) {
        if (!hasRemarksColumn(type)) return "";
        String table    = getTableName(type);
        String pkColumn = getPrimaryKeyColumn(type);
        String remarksCol = getRemarksColumn(type);
        if (table.isEmpty()) return "";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT " + remarksCol + " AS admin_remarks FROM " + table + " WHERE " + pkColumn + " = ?")) {
            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String r = rs.getString("admin_remarks");
                return (r == null) ? "" : r;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "";
    }

    public ObservableList<AdminRequestModel> fetchStudentHistory(String rollNumber) {
        ObservableList<AdminRequestModel> all = fetchAllRequests();
        ObservableList<AdminRequestModel> out = FXCollections.observableArrayList();
        for (AdminRequestModel m : all) {
            String sid = String.valueOf(m.getStudentId());
            if (sid.contains(rollNumber) || rollNumber.contains(sid)) out.add(m);
        }
        return out;
    }


    private String getTableName(String type) {
        if (type == null) return "";
        return switch (type.toUpperCase()) {
            case "ATTENDANCE"        -> "attendance_requests";
            case "COURSE_ISSUE"      -> "course_registration_issues";
            case "ADD_DROP"          -> "add_drop_requests";
            case "RECORD_CORRECTION" -> "academic_record_corrections";
            case "TRANSCRIPT"        -> "transcript_requests";
            case "ID_CARD"           -> "id_card_requests";
            case "ENROLL_LETTER"     -> "enrollment_letter_requests";
            case "DEGREE_VER"        -> "degree_verification_requests";
            case "FEE_SUPPORT"       -> "fee_support_requests";
            case "LOST_FOUND"        -> "lost_items";
            case "SCHOLARSHIP"       -> "scholarship_queries";
            case "COMPLAINT"         -> "general_complaints";
            default -> "";
        };
    }

    private String getPrimaryKeyColumn(String type) {
        if (type == null) return "request_id";
        return switch (type.toUpperCase()) {
            case "COURSE_ISSUE"      -> "issue_id";
            case "RECORD_CORRECTION" -> "correction_id";
            case "SCHOLARSHIP"       -> "query_id";
            case "COMPLAINT"         -> "complaint_id";
            case "LOST_FOUND"        -> "item_id";
            default                  -> "request_id";
        };
    }

    private String getRemarksColumn(String type) {
        if (type == null) return "admin_remarks";
        return "COMPLAINT".equalsIgnoreCase(type) ? "resolution_remarks" : "admin_remarks";
    }

    private boolean hasRemarksColumn(String type) {
        return type != null && !"LOST_FOUND".equalsIgnoreCase(type);
    }

    private boolean hasDeliveryColumns(String type) {
        if (type == null) return false;
        return switch (type.toUpperCase()) {
            case "TRANSCRIPT", "ID_CARD", "ENROLL_LETTER", "DEGREE_VER" -> true;
            default                                                    -> false;
        };
    }

    private String getDateColumnName(String type) {
        if (type == null) return null;
        return switch (type.toUpperCase()) {
            case "TRANSCRIPT"    -> "expected_date";
            case "ID_CARD"       -> "dispatch_date";
            case "ENROLL_LETTER" -> "ready_date";
            case "ADD_DROP"      -> "processed_date";     // academic — processed_date only
            default              -> null;
        };
    }

    private String safeString(ResultSet rs, String column) {
        try {
            String val = rs.getString(column);
            return val == null ? "" : val;
        } catch (SQLException e) {
            return "";
        }
    }
}