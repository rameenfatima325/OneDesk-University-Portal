package business.academic;

import business.shared.AcademicRequest;
import business.shared.NotificationService;
import business.shared.TrackingService;
import db.academic.AttendanceDAO;

import java.time.LocalDate;


public class AttendanceCorrectionRequest extends AcademicRequest {

    private String  courseName;
    private String  semester;
    private LocalDate classDate;
    private String  reason;
    private String  supportingDocPath;
    private String  trackingId;

    public AttendanceCorrectionRequest(int studentId) {
        super(studentId);
    }

    @Override
    public boolean validate() {
        if (courseName == null || courseName.isBlank()) return false;
        if (semester   == null || semester.isBlank())   return false;
        if (classDate  == null)                         return false;
        if (classDate.isAfter(LocalDate.now()))         return false; // date cannot be in the future
        if (reason     == null || reason.isBlank())     return false;
        return true;
    }

    @Override
    public boolean submit() throws business.shared.exceptions.EligibilityException {
        if (!validate()) return false;


        business.shared.EligibilityService.requireStandardEligibility(this.studentId);
        AttendanceDAO dao = new AttendanceDAO();
        if (!dao.verifyStudentExists(this.studentId)) return false;

        this.trackingId = TrackingService.generate("ATT");
        if (dao.saveAttendanceRequest(this)) {
            NotificationService.sendToAcademicOffice("Attendance Correction", this.trackingId);
            return true;
        }
        return false;
    }

    public String    getCourseName()         { return courseName; }
    public void      setCourseName(String c) { this.courseName = c; }
    public String    getSemester()           { return semester; }
    public void      setSemester(String s)   { this.semester = s; }
    public LocalDate getClassDate()          { return classDate; }
    public void      setClassDate(LocalDate d) { this.classDate = d; }
    public String    getReason()             { return reason; }
    public void      setReason(String r)     { this.reason = r; }
    public String    getSupportingDocPath()  { return supportingDocPath; }
    public void      setSupportingDocPath(String p) { this.supportingDocPath = p; }
    public String    getTrackingId()         { return trackingId; }
}
