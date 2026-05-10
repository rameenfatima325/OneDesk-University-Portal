package business.admin;

import javafx.beans.property.*;

public class AuditLogModel {
    private final StringProperty adminUsername;
    private final IntegerProperty requestId;
    private final StringProperty type;
    private final StringProperty oldStatus;
    private final StringProperty newStatus;
    private final StringProperty timestamp;
    private final StringProperty remarks;

    public AuditLogModel(String user, int id, String type, String oldS, String newS, String time, String rem) {
        this.adminUsername = new SimpleStringProperty(user);
        this.requestId = new SimpleIntegerProperty(id);
        this.type = new SimpleStringProperty(type);
        this.oldStatus = new SimpleStringProperty(oldS);
        this.newStatus = new SimpleStringProperty(newS);
        this.timestamp = new SimpleStringProperty(time);
        this.remarks = new SimpleStringProperty(rem);
    }

    public String getAdminUsername() { return adminUsername.get(); }
    public int getRequestId() { return requestId.get(); }
    public String getType() { return type.get(); }
    public String getOldStatus() { return oldStatus.get(); }
    public String getNewStatus() { return newStatus.get(); }
    public String getTimestamp() { return timestamp.get(); }
    public String getRemarks() { return remarks.get(); }

    public String getDisplayId() {
        String t = type.get();
        if (t == null) return String.valueOf(requestId.get());
        String prefix;
        switch (t.toUpperCase()) {
            case "ATTENDANCE":         prefix = "ATT"; break;
            case "COURSE_ISSUE":       prefix = "CRI"; break;
            case "ADD_DROP":           prefix = "ADR"; break;
            case "RECORD_CORRECTION":  prefix = "REC"; break;
            case "TRANSCRIPT":         prefix = "TRN"; break;
            case "ID_CARD":            prefix = "IDC"; break;
            case "ENROLL_LETTER":      prefix = "ENR"; break;
            case "DEGREE_VER":         prefix = "DEG"; break;
            case "FEE_SUPPORT":        prefix = "FEE"; break;
            case "LOST_FOUND":         prefix = "LF";  break;
            case "SCHOLARSHIP":        prefix = "SCH"; break;
            case "COMPLAINT":          prefix = "CMP"; break;
            default:                   prefix = "REQ";
        }
        return prefix + "-" + requestId.get();
    }

    public String getDateOnly() {
        String t = timestamp.get();
        if (t == null || t.isBlank()) return "";
        int sep = t.indexOf(' ');
        if (sep < 0) sep = t.indexOf('T');
        return sep > 0 ? t.substring(0, sep) : t;
    }
}