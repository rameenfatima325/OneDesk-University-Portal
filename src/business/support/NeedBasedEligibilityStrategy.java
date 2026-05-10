package business.support;

public class NeedBasedEligibilityStrategy implements EligibilityStrategy {

    private static final double INCOME_THRESHOLD   = 50000.0; // PKR/month
    private static final double MIN_CGPA           = 2.5;

    @Override
    public boolean isEligible(double cgpa, double familyIncome) {
        return cgpa >= MIN_CGPA && familyIncome <= INCOME_THRESHOLD;
    }

    @Override
    public String getCriteriaDescription() {
        return "Need-based scholarship requires CGPA ≥ 2.5 and family income ≤ PKR 50,000/month";
    }
}
