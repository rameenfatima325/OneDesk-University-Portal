package business.document;

import business.shared.*;
import db.document.EnrollmentLetterDAO;

public class EnrollmentLetterRequest extends DocumentRequest {
    private String letterType, addressedTo, language, trackingId;

    public EnrollmentLetterRequest(int studentId) { super(studentId); }

    @Override
    public boolean submit() throws business.shared.exceptions.EligibilityException {
        if (!validate()) return false;

        business.shared.EligibilityService.requireDocumentEligibility(studentId);
        business.shared.EligibilityService.requireActiveEnrollment(studentId);
        EnrollmentLetterDAO dao = new EnrollmentLetterDAO();

        this.trackingId = TrackingService.generate("ENR");
        if (dao.saveEnrollmentLetterRequest(this)) {
            NotificationService.sendToDocumentationDept(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean validate() { return letterType != null && addressedTo != null; }

    public String getLetterType() { return letterType; }
    public void setLetterType(String t) { this.letterType = t; }
    public String getAddressedTo() { return addressedTo; }
    public void setAddressedTo(String a) { this.addressedTo = a; }
    public String getLanguage() { return language; }
    public void setLanguage(String l) { this.language = l; }
    public String getTrackingId() { return trackingId; }
}