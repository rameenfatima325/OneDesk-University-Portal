package business.support;

import business.shared.SupportRequest;
import business.shared.RequestStatus;
import business.shared.exceptions.InvalidAmountException;

import java.util.Date;

public class FeeSupportRequest extends SupportRequest {

    private FeeType feeType;
    private double outstandingAmount;
    private FeeRequestType requestType;
    private String justification;
    private String docPath;

    public FeeSupportRequest(int studentId) {
        super(studentId);
        this.status = RequestStatus.PENDING;
        this.submissionDate = new Date();
    }

    @Override
    public boolean validate() {
        if (feeType == null) return false;
        if (outstandingAmount <= 0) return false;
        if (requestType == null) return false;
        if (justification == null || justification.trim().isEmpty()) return false;
        return true;
    }

    @Override
    public boolean submit() throws business.shared.exceptions.EligibilityException {
        if (!validate()) return false;

        business.shared.EligibilityService.requireStandardEligibility(this.studentId);
        this.status = RequestStatus.PENDING;
        return true;
    }

    public double calculateDueAmount() {
        switch (requestType) {
            case WAIVER:      return outstandingAmount;
            case INSTALLMENT:  return outstandingAmount / 3.0;
            case EXTENSION:   return outstandingAmount;
            default:          return outstandingAmount;
        }
    }

    public void resolveRequest(String adminRemarks) {
        this.adminRemarks = adminRemarks;
        updateStatus(RequestStatus.RESOLVED);
    }

    public void setOutstandingAmount(double amount) throws InvalidAmountException {
        if (amount <= 0)
            throw new InvalidAmountException("Outstanding amount must be positive. Got: " + amount);
        this.outstandingAmount = amount;
    }

    // Getters & Setters
    public FeeType getFeeType() { return feeType; }
    public void setFeeType(FeeType feeType) { this.feeType = feeType; }

    public double getOutstandingAmount() { return outstandingAmount; }

    public FeeRequestType getRequestType() { return requestType; }
    public void setRequestType(FeeRequestType requestType) { this.requestType = requestType; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public String getDocPath() { return docPath; }
    public void setDocPath(String docPath) { this.docPath = docPath; }
}
