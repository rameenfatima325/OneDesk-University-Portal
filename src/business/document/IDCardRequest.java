package business.document;

import business.shared.*;
import db.document.IDCardDAO;

public class IDCardRequest extends DocumentRequest {
    private String requestType, replacementReason, deliveryAddress, trackingId;
    private double fee;

    public IDCardRequest(int studentId) { super(studentId); }

    public double calculateFee() throws business.shared.exceptions.EligibilityException {
        business.shared.EligibilityService.requireActiveForIDCard(studentId);

        this.fee = "REPLACEMENT".equals(requestType) ? 5000.0 : 1000.0;
        return this.fee;
    }

    @Override
    public boolean submit() throws business.shared.exceptions.EligibilityException {
        if (!validate()) return false;

        business.shared.EligibilityService.requireDocumentEligibility(studentId);
        business.shared.EligibilityService.requireActiveForIDCard(studentId);
        IDCardDAO dao = new IDCardDAO();

        if ("REPLACEMENT".equalsIgnoreCase(this.requestType)
                || "DUPLICATE".equalsIgnoreCase(this.requestType)) {
            this.status = business.shared.RequestStatus.PENDING_VERIFICATION;
        }

        this.trackingId = TrackingService.generate("IDC");
        if (dao.saveIDCardRequest(this)) {
            NotificationService.sendToDocumentationDept(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean validate() { return requestType != null && deliveryAddress != null; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String t) { this.requestType = t; }
    public String getReplacementReason() { return replacementReason; }
    public void setReplacementReason(String r) { this.replacementReason = r; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String a) { this.deliveryAddress = a; }
    public String getTrackingId() { return trackingId; }
}