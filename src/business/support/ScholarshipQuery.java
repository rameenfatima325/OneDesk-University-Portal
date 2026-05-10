package business.support;

import business.shared.SupportRequest;
import business.shared.RequestStatus;
import business.shared.exceptions.EligibilityException;

import java.util.Date;

public class ScholarshipQuery extends SupportRequest {

    private ScholarshipType scholarshipType;
    private double cgpa;
    private double familyIncome;
    private String queryText;
    private String docPath;
    private boolean eligibilityFlag;

    // Strategy Pattern — set based on scholarshipType
    private EligibilityStrategy eligibilityStrategy;

    public ScholarshipQuery(int studentId) {
        super(studentId);
        this.status = RequestStatus.PENDING;
        this.submissionDate = new Date();
    }

    @Override
    public boolean validate() {
        if (scholarshipType == null) return false;
        if (cgpa < 0.0 || cgpa > 4.0) return false;
        if (familyIncome < 0) return false;
        if (queryText == null || queryText.trim().isEmpty()) return false;
        return true;
    }

    @Override
    public boolean submit() throws business.shared.exceptions.EligibilityException {
        if (!validate()) return false;

        business.shared.EligibilityService.requireStandardEligibility(this.studentId);
        this.eligibilityFlag = checkEligibility();
        this.status = RequestStatus.PENDING;
        return true;
    }

    /**
     * Checks preliminary eligibility using the Strategy Pattern.
     * The correct strategy is selected based on scholarshipType.
     */
    public boolean checkEligibility() {
        resolveStrategy();
        return eligibilityStrategy.isEligible(cgpa, familyIncome);
    }

    /**
     * Throws EligibilityException if student does not meet criteria.
     */
    public void assertEligible() throws EligibilityException {
        resolveStrategy();
        if (!eligibilityStrategy.isEligible(cgpa, familyIncome)) {
            throw new EligibilityException(
                "Not eligible: " + eligibilityStrategy.getCriteriaDescription()
            );
        }
    }

    private void resolveStrategy() {
        if (eligibilityStrategy != null) return;
        if (scholarshipType == null) {
            eligibilityStrategy = new DefaultEligibilityStrategy();
            return;
        }
        switch (scholarshipType) {
            case MERIT:      eligibilityStrategy = new MeritEligibilityStrategy();    break;
            case NEED_BASED: eligibilityStrategy = new NeedBasedEligibilityStrategy(); break;
            default:         eligibilityStrategy = new DefaultEligibilityStrategy();   break;
        }
    }

    // Validated setters
    public void setCgpa(double cgpa) {
        if (cgpa < 0.0 || cgpa > 4.0)
            throw new IllegalArgumentException("CGPA must be between 0.0 and 4.0");
        this.cgpa = cgpa;
    }

    public void setFamilyIncome(double familyIncome) {
        if (familyIncome < 0)
            throw new IllegalArgumentException("Family income cannot be negative");
        this.familyIncome = familyIncome;
    }

    // Getters & Setters
    public ScholarshipType getScholarshipType() { return scholarshipType; }
    public void setScholarshipType(ScholarshipType scholarshipType) {
        this.scholarshipType = scholarshipType;
        this.eligibilityStrategy = null; // reset so it re-resolves
    }

    public double getCgpa() { return cgpa; }
    public double getFamilyIncome() { return familyIncome; }

    public String getQueryText() { return queryText; }
    public void setQueryText(String queryText) { this.queryText = queryText; }

    public String getDocPath() { return docPath; }
    public void setDocPath(String docPath) { this.docPath = docPath; }

    public boolean isEligibilityFlag() { return eligibilityFlag; }

    public String getEligibilityCriteria() {
        resolveStrategy();
        return eligibilityStrategy.getCriteriaDescription();
    }
}
