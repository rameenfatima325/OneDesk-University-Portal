package business.document;

import business.shared.*;
import db.document.DegreeVerificationDAO;

public class DegreeVerificationRequest extends DocumentRequest {
    private int graduationYear;
    private String degreeProgram, verificationType, trackingId;
    private boolean isUrgent;

    public DegreeVerificationRequest(int studentId) { super(studentId); }

    @Override
    public boolean submit() throws business.shared.exceptions.EligibilityException {
        if (!validate()) return false;

        business.shared.EligibilityService.requireDocumentEligibilityNoHolds(studentId);

        DegreeVerificationDAO dao = new DegreeVerificationDAO();

        EligibilityService.requireGraduatedStatus(studentId);

        this.trackingId = TrackingService.generate("DEG");
        if (dao.saveDegreeRequest(this)) {
            NotificationService.sendToDocumentationDept(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean validate() { return degreeProgram != null && copies > 0; }

    public int getGraduationYear() { return graduationYear; }
    public void setGraduationYear(int y) { this.graduationYear = y; }
    public String getDegreeProgram() { return degreeProgram; }
    public void setDegreeProgram(String p) { this.degreeProgram = p; }
    public String getVerificationType() { return verificationType; }
    public void setVerificationType(String v) { this.verificationType = v; }
    public boolean isUrgent() { return isUrgent; }
    public void setUrgent(boolean u) { this.isUrgent = u; }
    public String getTrackingId() { return trackingId; }
}