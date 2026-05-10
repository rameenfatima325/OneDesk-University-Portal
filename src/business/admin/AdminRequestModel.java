package business.admin;

import javafx.beans.property.*;

public class AdminRequestModel {
    private final IntegerProperty requestId;
    private final IntegerProperty studentId;
    private final StringProperty type;
    private final StringProperty status;
    private final StringProperty date;
    private final IntegerProperty collected;

    private String deliveryMode = "";
    private String purpose      = "";
    private String address      = "";
    private String copies       = "";
    private String remarks      = "";   // cached admin_remarks — loaded once in UNION query

    public AdminRequestModel(int id, int sId, String type, String date, String status, int collected) {
        this.requestId = new SimpleIntegerProperty(id);
        this.studentId = new SimpleIntegerProperty(sId);
        this.type      = new SimpleStringProperty(type);
        this.date      = new SimpleStringProperty(date);
        this.status    = new SimpleStringProperty(status);
        this.collected = new SimpleIntegerProperty(collected);
    }

    public int    getRequestId() { return requestId.get(); }


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

    public int    getStudentId() { return studentId.get(); }
    public String getType()      { return type.get(); }
    public String getStatus()    { return status.get(); }
    public String getDate()      { return date.get(); }

    public String getDateOnly() {
        String t = date.get();
        if (t == null || t.isBlank()) return "";
        int sep = t.indexOf(' ');
        if (sep < 0) sep = t.indexOf('T');
        return sep > 0 ? t.substring(0, sep) : t;
    }
    public int    getCollected() { return collected.get(); }

    public String getDeliveryMode() { return deliveryMode == null ? "" : deliveryMode; }
    public void   setDeliveryMode(String deliveryMode) { this.deliveryMode = deliveryMode; }

    public String getPurpose() { return purpose == null ? "" : purpose; }
    public void   setPurpose(String purpose) { this.purpose = purpose; }

    public String getAddress() { return address == null ? "" : address; }
    public void   setAddress(String address) { this.address = address; }

    public String getCopies() { return copies == null ? "" : copies; }
    public void   setCopies(String copies) { this.copies = copies; }

    /** Cached admin_remarks from the UNION query — avoids per-row DB round-trips. */
    public String getRemarks() { return remarks == null ? "" : remarks; }
    public void   setRemarks(String remarks) { this.remarks = remarks; }


    private final java.util.LinkedHashMap<String, String> extras = new java.util.LinkedHashMap<>();
    public void putExtra(String label, String value) {
        if (label != null && value != null && !value.isBlank()) {
            extras.put(label, value);
        }
    }
    public java.util.Map<String, String> getExtras() { return extras; }

    public boolean isPostal() {
        return "POSTAL".equalsIgnoreCase(deliveryMode) || "COURIER".equalsIgnoreCase(deliveryMode);
    }

    public boolean isAcademic() {
        String t = getType();
        if (t == null) return false;
        return t.equals("ATTENDANCE")
                || t.equals("COURSE_ISSUE")
                || t.equals("ADD_DROP")
                || t.equals("RECORD_CORRECTION");
    }
}