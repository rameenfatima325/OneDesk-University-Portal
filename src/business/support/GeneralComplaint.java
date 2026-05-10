package business.support;

import business.shared.SupportRequest;
import business.shared.RequestStatus;

import java.util.Date;

public class GeneralComplaint extends SupportRequest {

    private ComplaintCategory category;
    private Severity severity;
    private String subject;
    private boolean isAnonymous;

    public GeneralComplaint(int studentId) {
        super(studentId);
        this.status = RequestStatus.PENDING;
        this.submissionDate = new Date();
    }

    @Override
    public boolean validate() {
        if (category == null) return false;
        if (severity == null) return false;
        if (subject == null || subject.trim().isEmpty()) return false;
        if (description == null || description.trim().isEmpty()) return false;
        return true;
    }

    @Override
    public boolean submit() throws business.shared.exceptions.EligibilityException {
        if (!validate()) return false;

        business.shared.EligibilityService.requireStandardEligibility(this.studentId);
        if (isAnonymous) anonymize();
        this.status = RequestStatus.PENDING;
        return true;
    }


    public void anonymize() {
        this.studentId = 0; // zero signals anonymous — admin cannot trace back
    }

    // Getters & Setters
    public String getCategory() { return category.name(); }
    public void setCategory(ComplaintCategory category) { this.category = category; }

    public String getSeverity() { return severity.name(); }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }
}
