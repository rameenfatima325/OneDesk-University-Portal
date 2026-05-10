package business.document;

import business.shared.*;
import db.document.TranscriptDAO;

public class TranscriptRequest extends DocumentRequest {
    private String transcriptType, deliveryMode, mailingAddress, trackingId;

    public TranscriptRequest(int studentId) { super(studentId); }

    @Override
    public boolean submit() throws business.shared.exceptions.EligibilityException {
        if (!validate()) return false;

        business.shared.EligibilityService.requireDocumentEligibilityNoHolds(this.studentId);

        TranscriptDAO dao = new TranscriptDAO();

        if ("OFFICIAL_SEALED".equalsIgnoreCase(this.transcriptType)) {
            this.status = business.shared.RequestStatus.PENDING_VERIFICATION;
        }

        this.trackingId = TrackingService.generate("TRN");
        if (dao.saveTranscriptRequest(this)) {
            NotificationService.sendToExaminationDept(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean validate() {
        return transcriptType != null && purpose != null && copies > 0 && deliveryMode != null;
    }

    public String getTranscriptType() { return transcriptType; }
    public void setTranscriptType(String t) { this.transcriptType = t; }
    public String getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(String m) { this.deliveryMode = m; }
    public String getMailingAddress() { return mailingAddress; }
    public void setMailingAddress(String a) { this.mailingAddress = a; }
    public String getTrackingId() { return trackingId; }
}