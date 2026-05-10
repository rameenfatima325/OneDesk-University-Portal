package business.shared;

import business.shared.exceptions.EligibilityException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class EligibilityService {

    private EligibilityService() {}

    public static StudentStatus loadStatus(int studentId) {
        String sql = "SELECT enrollment_status, financial_hold, disciplinary_hold "
                   + "FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("enrollment_status");
                    if (status == null) status = "ACTIVE";
                    boolean fin = readBoolFlag(rs, "financial_hold");
                    boolean dis = readBoolFlag(rs, "disciplinary_hold");
                    return new StudentStatus(status.toUpperCase(), fin, dis);
                }
            }
        } catch (SQLException e) {
        }
        return new StudentStatus("ACTIVE", false, false);
    }

    private static boolean readBoolFlag(ResultSet rs, String col) {
        try { return rs.getBoolean(col); } catch (SQLException e) { return false; }
    }

    public static void requireStandardEligibility(int studentId) throws EligibilityException {
        StudentStatus s = loadStatus(studentId);
        if (!s.canSubmitStandard()) {
            throw new EligibilityException(
                    "Your enrollment status (" + s.enrollmentStatus + ") does not permit "
                  + "submitting this type of request. Please contact the Academic Office.");
        }
    }

    public static void requireDocumentEligibility(int studentId) throws EligibilityException {
        StudentStatus s = loadStatus(studentId);
        if (!s.canSubmitDocument()) {
            throw new EligibilityException(
                    "Your enrollment status (" + s.enrollmentStatus + ") does not permit "
                  + "submitting this request. Inactive or suspended accounts can only view "
                  + "past requests. Please contact the Documentation Department.");
        }
    }

    public static void requireDocumentEligibilityNoHolds(int studentId) throws EligibilityException {
        StudentStatus s = loadStatus(studentId);
        if (!s.canSubmitDocument()) {
            throw new EligibilityException(
                    "Your enrollment status (" + s.enrollmentStatus + ") does not permit "
                  + "submitting this request.");
        }
        if (s.financialHold) {
            throw new EligibilityException(
                    "Your account has an active Financial Hold. Transcript and degree "
                  + "verification requests cannot be processed until the hold is cleared. "
                  + "Please visit the Finance Office.");
        }
        if (s.disciplinaryHold) {
            throw new EligibilityException(
                    "Your account has an active Disciplinary Hold. Transcript and degree "
                  + "verification requests cannot be processed until the hold is cleared. "
                  + "Please contact Student Affairs.");
        }
    }

    public static final class StudentStatus {
        public final String  enrollmentStatus;     // ACTIVE / ENROLLED / GRADUATED / ALUMNI / INACTIVE / SUSPENDED
        public final boolean financialHold;
        public final boolean disciplinaryHold;

        public StudentStatus(String enrollmentStatus, boolean fin, boolean dis) {
            this.enrollmentStatus = enrollmentStatus;
            this.financialHold = fin;
            this.disciplinaryHold = dis;
        }

        public boolean isActiveOrEnrolled() {
            return "ACTIVE".equals(enrollmentStatus) || "ENROLLED".equals(enrollmentStatus);
        }

        public boolean isGraduatedOrAlumni() {
            return "GRADUATED".equals(enrollmentStatus) || "ALUMNI".equals(enrollmentStatus);
        }

        public boolean canSubmitStandard() {
            return isActiveOrEnrolled();
        }

        public boolean canSubmitDocument() {
            return isActiveOrEnrolled() || isGraduatedOrAlumni();
        }
    }

    public static void requireGraduatedStatus(int studentId) throws EligibilityException {
        StudentStatus s = loadStatus(studentId);
        if (!"GRADUATED".equals(s.enrollmentStatus)) {
            throw new EligibilityException("DEGREE NOT COMPLETE YET! REQUEST DENIED.");
        }
    }

    public static void requireActiveEnrollment(int studentId) throws EligibilityException {
        StudentStatus s = loadStatus(studentId);

        if (!s.isActiveOrEnrolled()) {
            throw new EligibilityException(
                    "GRADUATED STUDENTS CANNOT REQUEST ENROLLMENT LETTERS! REQUEST DENIED."
            );
        }
    }
    public static void requireActiveForIDCard(int studentId) throws EligibilityException {
        StudentStatus s = loadStatus(studentId);

        if (!s.isActiveOrEnrolled()) {
            throw new EligibilityException(
                    "ID CARD REQUEST DENIED: ONLY ACTIVE STUDENTS CAN REQUEST NEW CARDS. "

            );
        }
    }
}
