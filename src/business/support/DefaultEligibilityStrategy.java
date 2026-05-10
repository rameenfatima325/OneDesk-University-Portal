package business.support;

public class DefaultEligibilityStrategy implements EligibilityStrategy {

    @Override
    public boolean isEligible(double cgpa, double familyIncome) {
        return cgpa >= 2.0; // Minimum passing CGPA
    }

    @Override
    public String getCriteriaDescription() {
        return "External/Government scholarship requires CGPA ≥ 2.0. Check specific scheme for full criteria.";
    }
}
