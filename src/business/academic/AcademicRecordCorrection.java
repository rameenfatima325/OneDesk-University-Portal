package business.academic;

import business.shared.AcademicRequest;
import business.shared.NotificationService;
import business.shared.Priority;
import business.shared.Request;
import business.shared.RequestStatus;
import business.shared.StatusChangeObserver;
import business.shared.TrackingService;
import db.academic.RecordCorrectionDAO;


public class AcademicRecordCorrection extends AcademicRequest {

    public enum RecordType { GRADE, CGPA, TRANSCRIPT_ERROR, NAME_SPELLING, OTHER }

    private RecordType recordType;
    private String     incorrectValue;
    private String     correctValue;
    private String     justification;
    private String     evidencePath;
    private String     trackingId;

    public AcademicRecordCorrection(int studentId) {
        super(studentId);
    }

    public Priority getPriorityLevel() {
        if (recordType == null) return Priority.MEDIUM;
        return switch (recordType) {
            case GRADE, CGPA               -> Priority.HIGH;   // directly affects student record
            case TRANSCRIPT_ERROR          -> Priority.HIGH;
            case NAME_SPELLING             -> Priority.MEDIUM;
            case OTHER                     -> Priority.LOW;
        };
    }

    public boolean attachEvidence(String filePath) {
        if (filePath == null || filePath.isBlank()) return false;
        this.evidencePath = filePath;
        return true;
    }

    @Override
    public boolean validate() {
        if (recordType     == null)                                return false;
        if (incorrectValue == null || incorrectValue.isBlank())    return false;
        if (correctValue   == null || correctValue.isBlank())      return false;
        if (justification  == null || justification.isBlank())     return false;
        return true;
    }

    @Override
    public boolean submit() throws business.shared.exceptions.EligibilityException {
        if (!validate()) return false;


        business.shared.EligibilityService.requireStandardEligibility(this.studentId);
        this.setPriority(getPriorityLevel());

        if (this.getPriority() == Priority.HIGH) {
            this.addObserver(new AdminAlertObserver());
        }

        RecordCorrectionDAO dao = new RecordCorrectionDAO();
        if (!dao.verifyStudentExists(this.studentId)) return false;

        this.trackingId = TrackingService.generate("REC");
        if (dao.saveRecordCorrection(this)) {
            this.updateStatus(RequestStatus.PENDING);
            NotificationService.sendToAcademicOffice("Academic Record Correction", this.trackingId);
            return true;
        }
        return false;
    }

    private static class AdminAlertObserver implements StatusChangeObserver {
        @Override
        public void onStatusChange(Request request, RequestStatus oldStatus, RequestStatus newStatus) {
            System.out.println("[ADMIN ALERT] HIGH-priority record correction "
                    + ((AcademicRecordCorrection) request).getTrackingId()
                    + " requires immediate attention (status = " + newStatus + ").");
        }
    }

    public RecordType getRecordType()         { return recordType; }
    public void       setRecordType(RecordType r) { this.recordType = r; }
    public String     getIncorrectValue()     { return incorrectValue; }
    public void       setIncorrectValue(String v) { this.incorrectValue = v; }
    public String     getCorrectValue()       { return correctValue; }
    public void       setCorrectValue(String v)   { this.correctValue = v; }
    public String     getJustification()      { return justification; }
    public void       setJustification(String j)  { this.justification = j; }
    public String     getEvidencePath()       { return evidencePath; }
    public void       setEvidencePath(String p)   { this.evidencePath = p; }
    public String     getTrackingId()         { return trackingId; }
}
